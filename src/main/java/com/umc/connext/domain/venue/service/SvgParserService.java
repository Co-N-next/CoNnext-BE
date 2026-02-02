package com.umc.connext.domain.venue.service;

import com.umc.connext.common.enums.FacilityType;
import com.umc.connext.common.exception.GeneralException;
import com.umc.connext.domain.venue.dto.Coordinate;
import com.umc.connext.domain.venue.entity.VenueFacility;
import com.umc.connext.domain.venue.entity.VenueFloorConfig;
import com.umc.connext.domain.venue.entity.VenueSection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class SvgParserService {

    private final FloorMappingService floorMappingService;

    private static final Pattern COORDINATE_PATTERN = Pattern.compile("([ML])\\s*([\\d.]+)\\s+([\\d.]+)");
    private static final Pattern FLOOR_PREFIX_PATTERN = Pattern.compile("^([12])F[_-]?(.+)$", Pattern.CASE_INSENSITIVE);

    public static class ParseResult {
        public List<VenueSection> sections = new ArrayList<>();
        public List<VenueFacility> facilities = new ArrayList<>();
        public List<VenueFloorConfig> floorConfigs = new ArrayList<>();
        public int svgWidth;
        public int svgHeight;
    }

    public ParseResult parseAll(InputStream svgInputStream, Long venueId) {
        validateInput(svgInputStream, venueId);
        ParseResult result = new ParseResult();
        try {
            Document doc = parseXml(svgInputStream);

            Element svgElement = doc.getDocumentElement();
            result.svgWidth = parseIntOrDefault(svgElement.getAttribute("width"), 0);
            result.svgHeight = parseIntOrDefault(svgElement.getAttribute("height"), 0);

            result.sections = parseSectionsFromDoc(doc, venueId, result);
            result.facilities = parseFacilitiesFromDoc(doc, venueId, result.svgWidth, result.svgHeight);

        } catch (Exception e) {
            log.error("SVG 파싱 실패 - VenueId: {}", venueId, e);
            throw GeneralException.notFound("SVG 파싱에 실패했습니다: " + e.getMessage());
        }
        return result;
    }

    public List<VenueSection> parseSections(InputStream svgInputStream, Long venueId) {
        return parseAll(svgInputStream, venueId).sections;
    }

    private List<VenueSection> parseSectionsFromDoc(Document doc, Long venueId, ParseResult result) {
        List<VenueSection> sections = new ArrayList<>();
        NodeList paths = doc.getElementsByTagName("path");

        for (int i = 0; i < paths.getLength(); i++) {
            Element path = (Element) paths.item(i);
            String id = path.getAttribute("id");
            String d = path.getAttribute("d");

            if (id.isEmpty() || id.startsWith("Vector") || id.equals("floor")) continue;

            List<Coordinate> vertices = extractCoordinates(d);
            if (vertices.size() < 3) continue;

            Coordinate center = calculateCenter(vertices);
            int floor = determineFloor(
                    path, id, venueId,
                    center.getX().doubleValue(), center.getY().doubleValue(),
                    result.svgWidth, result.svgHeight
            );

            String cleanSectionId = cleanSectionId(id);

            VenueSection section = VenueSection.builder()
                    .venueId(venueId)
                    .sectionId(cleanSectionId)
                    .floor(floor)
                    .fullPath(d)
                    .centerX(center.getX())
                    .centerY(center.getY())
                    .build();
            section.setVerticesList(vertices);

            sections.add(section);
            result.floorConfigs.add(VenueFloorConfig.builder()
                    .venueId(venueId)
                    .sectionId(cleanSectionId)
                    .floor(floor)
                    .build());
        }
        return sections;
    }

    public List<VenueFacility> parseFacilities(InputStream svgInputStream, Long venueId) {
        ParseResult result = parseAll(svgInputStream, venueId);
        return result.facilities;
    }

    private List<VenueFacility> parseFacilitiesFromDoc(Document doc, Long venueId, int svgWidth, int svgHeight) {
        List<VenueFacility> facilities = new ArrayList<>();
        NodeList circles = doc.getElementsByTagName("circle");

        for (int i = 0; i < circles.getLength(); i++) {
            Element circle = (Element) circles.item(i);

            String rawId = circle.getAttribute("id");
            String fixedId = new String(rawId.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);

            String fill = circle.getAttribute("fill");
            double cx = parseDouble(circle.getAttribute("cx"));
            double cy = parseDouble(circle.getAttribute("cy"));

            FacilityType type = determineFacilityType(circle, fixedId, fill);
            String name = decodeKoreanId(fixedId);

            int floor = determineFloor(circle, fixedId, venueId, cx, cy, svgWidth, svgHeight);

            String connectedFloors = circle.getAttribute("data-connects");
            if (connectedFloors.isEmpty() && type == FacilityType.STAIRS) {
                connectedFloors = "1,2";
            }

            VenueFacility facility = VenueFacility.builder()
                    .venueId(venueId)
                    .name(name)
                    .type(type.name())
                    .floor(floor)
                    .x(BigDecimal.valueOf(cx).setScale(1, RoundingMode.HALF_UP))
                    .y(BigDecimal.valueOf(cy).setScale(1, RoundingMode.HALF_UP))
                    .connectedFloors(connectedFloors.isEmpty() ? null : connectedFloors)
                    .build();

            facilities.add(facility);
        }
        return facilities;
    }

    private int determineFloor(Element element, String id, Long venueId, double x, double y, int svgWidth, int svgHeight) {
        String dataFloor = element.getAttribute("data-floor");
        if (!dataFloor.isEmpty()) return parseIntOrDefault(dataFloor, 1);

        Node parent = element.getParentNode();
        while (parent instanceof Element parentEl) {
            String parentId = parentEl.getAttribute("id");
            if (!parentId.isEmpty()) {
                if (parentId.toLowerCase().contains("floor-2") || parentId.equals("2f")) return 2;
                if (parentId.toLowerCase().contains("floor-1") || parentId.equals("1f")) return 1;
            }
            parent = parentEl.getParentNode();
        }

        Matcher matcher = FLOOR_PREFIX_PATTERN.matcher(id);
        if (matcher.matches()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                log.warn("Failed to parse floor from id '{}' using FLOOR_PREFIX_PATTERN, group(1)='{}'", id, matcher.group(1), e);
            }
        }

        if (floorMappingService.hasFloorConfig(venueId)) {
            String cleanId = cleanSectionId(id);
            int dbFloor = floorMappingService.getFloor(venueId, cleanId);
            if (dbFloor > 0) return dbFloor;
        }


        if (svgWidth > 0 && svgHeight > 0) {
            return floorMappingService.estimateFloorByCoordinate(venueId, x, y, svgWidth, svgHeight);
        }
        return 1;
    }

    private String cleanSectionId(String id) {
        Matcher matcher = FLOOR_PREFIX_PATTERN.matcher(id);
        if (matcher.matches()) return matcher.group(2);
        return id;
    }

    private FacilityType determineFacilityType(Element element, String id, String fill) {
        String dataType = element.getAttribute("data-type");
        if (!dataType.isEmpty()) {
            try {
                return FacilityType.valueOf(dataType.toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
        }

        if ("#006AFF".equalsIgnoreCase(fill)) return FacilityType.STAIRS;

        if ("#EC0000".equalsIgnoreCase(fill)) {
            if (containsKorean(id, "화장실")) {
                if (containsKorean(id, "VIP") || containsKorean(id, "공용")) return FacilityType.VIP_TOILET;
                return FacilityType.TOILET;
            }
            if (containsKorean(id, "운영") || containsKorean(id, "사무")) return FacilityType.OFFICE;
            if (containsKorean(id, "판매") || containsKorean(id, "기념품")) return FacilityType.STORE;
        }

        return FacilityType.ETC;
    }

    private List<Coordinate> extractCoordinates(String pathData) {
        List<Coordinate> coords = new ArrayList<>();
        Matcher matcher = COORDINATE_PATTERN.matcher(pathData);

        while (matcher.find()) {
            double x = parseDouble(matcher.group(2));
            double y = parseDouble(matcher.group(3));
            coords.add(new Coordinate(
                    BigDecimal.valueOf(x).setScale(1, RoundingMode.HALF_UP),
                    BigDecimal.valueOf(y).setScale(1, RoundingMode.HALF_UP)
            ));
        }

        if (pathData.toUpperCase().contains("Z") && coords.size() > 1) {
            Coordinate first = coords.get(0);
            Coordinate last = coords.get(coords.size() - 1);
            if (first.getX().equals(last.getX()) && first.getY().equals(last.getY())) {
                coords.remove(coords.size() - 1);
            }
        }

        return coords;
    }

    private Coordinate calculateCenter(List<Coordinate> vertices) {
        if (vertices.isEmpty()) return new Coordinate(BigDecimal.ZERO, BigDecimal.ZERO);

        double sumX = 0, sumY = 0;
        for (Coordinate v : vertices) {
            sumX += v.getX().doubleValue();
            sumY += v.getY().doubleValue();
        }

        return new Coordinate(
                BigDecimal.valueOf(sumX / vertices.size()).setScale(1, RoundingMode.HALF_UP),
                BigDecimal.valueOf(sumY / vertices.size()).setScale(1, RoundingMode.HALF_UP)
        );
    }

    private boolean containsKorean(String id, String keyword) {
        try {
            String decoded = java.net.URLDecoder.decode(id.replace("&#", "%").replace(";", ""), StandardCharsets.UTF_8);
            return decoded.contains(keyword);
        } catch (Exception e) {
            return id.contains(keyword);
        }
    }

    private String decodeKoreanId(String id) {
        try {
            if (id.contains("&#")) {
                StringBuilder result = new StringBuilder();
                String[] parts = id.split("&#");

                for (String part : parts) {
                    if (part.isEmpty()) continue;

                    int semicolonIdx = part.indexOf(';');
                    if (semicolonIdx > 0) {
                        try {
                            int code = Integer.parseInt(part.substring(0, semicolonIdx));
                            result.append((char) code);
                            if (semicolonIdx < part.length() - 1) {
                                result.append(part.substring(semicolonIdx + 1));
                            }
                        } catch (NumberFormatException e) {
                            result.append(part);
                        }
                    } else {
                        result.append(part);
                    }
                }
                return result.toString().trim();
            }
            return id;
        } catch (Exception e) {
            return id;
        }
    }

    private Document parseXml(InputStream inputStream) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();

        InputSource is = new InputSource(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        is.setEncoding("UTF-8");

        return builder.parse(is);
    }

    private double parseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private int parseIntOrDefault(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    // ==================== Validation Methods ====================

    /**
     * SVG 파싱 입력값 검증
     */
    private void validateInput(InputStream svgInputStream, Long venueId) {
        if (svgInputStream == null) {
            throw GeneralException.notFound("SVG 파일이 제공되지 않았습니다.");
        }

        if (venueId == null || venueId <= 0) {
            throw GeneralException.notFound("유효하지 않은 공연장 ID입니다.");
        }
    }
}

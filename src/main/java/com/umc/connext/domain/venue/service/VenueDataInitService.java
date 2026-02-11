package com.umc.connext.domain.venue.service;

import com.umc.connext.domain.venue.entity.Venue;
import com.umc.connext.domain.venue.entity.VenueFacility;
import com.umc.connext.domain.venue.entity.VenueSection;
import com.umc.connext.domain.venue.repository.VenueFacilityRepository;
import com.umc.connext.domain.venue.repository.VenueRepository;
import com.umc.connext.domain.venue.repository.VenueSectionRepository;
import com.umc.connext.common.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class VenueDataInitService {

    private final SvgParserService svgParserService;
    private final VenueSectionRepository sectionRepository;
    private final VenueFacilityRepository facilityRepository;
    private final VenueRepository venueRepository;

    @Transactional
    public InitResult initializeFromSvg(Long venueId, MultipartFile svgFile) {
        validateInput(venueId, svgFile);

        try {
            return initializeFromSvg(venueId, svgFile.getInputStream());
        } catch (Exception e) {
            log.error("SVG 파일 처리 실패 - VenueId: {}", venueId, e);
            return InitResult.failure("SVG 파일 처리 실패: " + e.getMessage());
        }
    }

    @Transactional
    public InitResult initializeFromSvg(Long venueId, InputStream svgInputStream) {
        validateInput(venueId, svgInputStream);

        try {
            Venue venue = venueRepository.findById(venueId)
                    .orElseThrow(() -> GeneralException.notFound("공연장을 찾을 수 없습니다. ID=" + venueId));

            byte[] svgBytes = svgInputStream.readAllBytes();
            ByteArrayInputStream inputStream = new ByteArrayInputStream(svgBytes);

            SvgParserService.ParseResult parseResult = svgParserService.parseAll(inputStream, venueId);

            venue.setSvgWidth(parseResult.svgWidth);
            venue.setSvgHeight(parseResult.svgHeight);
            venueRepository.save(venue);

            List<VenueSection> sections = parseResult.sections;
            List<VenueFacility> facilities = parseResult.facilities;

            List<VenueSection> savedSections = sectionRepository.saveAll(sections);
            List<VenueFacility> savedFacilities = facilityRepository.saveAll(facilities);

            log.info("공연장 초기화 완료 - VenueId: {}, 크기: {}x{}, 섹션: {}, 시설물: {}",
                    venueId, parseResult.svgWidth, parseResult.svgHeight,
                    savedSections.size(), savedFacilities.size());

            return InitResult.success(savedSections.size(), savedFacilities.size());
        } catch (GeneralException e) {
            log.error("공연장 초기화 실패 - VenueId: {}", venueId, e);
            return InitResult.failure(e.getMessage());
        } catch (Exception e) {
            log.error("예기치 않은 오류 발생 - VenueId: {}", venueId, e);
            return InitResult.failure("초기화 실패: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getVenueStats(Long venueId) {
        if (venueId == null || venueId <= 0) {
            throw GeneralException.notFound("유효하지 않은 공연장 ID입니다.");
        }

        // Venue 존재 여부 확인
        venueRepository.findById(venueId)
                .orElseThrow(() -> GeneralException.notFound("공연장을 찾을 수 없습니다. ID=" + venueId));

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalSections", sectionRepository.countByVenueId(venueId));
        stats.put("totalFacilities", facilityRepository.countByVenueId(venueId));
        stats.put("floor1Sections", sectionRepository.countByVenueIdAndFloor(venueId, 1));
        stats.put("floor2Sections", sectionRepository.countByVenueIdAndFloor(venueId, 2));
        stats.put("stairsCount", facilityRepository.findAllStairsByVenueId(venueId).size());

        return stats;
    }

    public static class InitResult {
        private final boolean success;
        private final String message;
        private final int sectionCount;
        private final int facilityCount;

        private InitResult(boolean success, String message, int sectionCount, int facilityCount) {
            this.success = success;
            this.message = message;
            this.sectionCount = sectionCount;
            this.facilityCount = facilityCount;
        }

        public static InitResult success(int sectionCount, int facilityCount) {
            return new InitResult(
                    true,
                    String.format("초기화 완료: %d개 섹션, %d개 시설물 저장됨", sectionCount, facilityCount),
                    sectionCount,
                    facilityCount
            );
        }

        public static InitResult failure(String message) {
            return new InitResult(false, message, 0, 0);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public int getSectionCount() {
            return sectionCount;
        }

        public int getFacilityCount() {
            return facilityCount;
        }
    }

    // ==================== Validation Methods ====================

    /**
     * VenueId와 MultipartFile 입력값 검증
     */
    private void validateInput(Long venueId, MultipartFile svgFile) {
        if (venueId == null || venueId <= 0) {
            throw GeneralException.notFound("유효하지 않은 공연장 ID입니다.");
        }

        if (svgFile == null || svgFile.isEmpty()) {
            throw GeneralException.notFound("SVG 파일이 제공되지 않았습니다.");
        }

        String fileName = svgFile.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase().endsWith(".svg")) {
            throw GeneralException.notFound("SVG 파일만 허용됩니다. 파일명: " + fileName);
        }
    }

    /**
     * VenueId와 InputStream 입력값 검증
     */
    private void validateInput(Long venueId, InputStream svgInputStream) {
        if (venueId == null || venueId <= 0) {
            throw GeneralException.notFound("유효하지 않은 공연장 ID입니다.");
        }

        if (svgInputStream == null) {
            throw GeneralException.notFound("SVG 파일이 제공되지 않았습니다.");
        }
    }
}

package com.umc.connext.domain.mate.projection;

import java.time.LocalDateTime;

public interface MateReservationProjection {
    String getConcertName();
    String getConcertPosterImage();
    String getConcertArtist();
    LocalDateTime getStartAt();
    String getConcertVenue();
    Integer getFloor();
    String getSection();
    String getRow();
    Integer getSeat();
}

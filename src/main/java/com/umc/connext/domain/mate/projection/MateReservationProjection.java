package com.umc.connext.domain.mate.projection;

import java.time.LocalDateTime;

public interface MateReservationProjection {
    Long getConcertId();
    String getConcertName();
    String getConcertPosterImage();
    String getConcertArtist();
    LocalDateTime getStartAt();
    String getConcertVenue();
}

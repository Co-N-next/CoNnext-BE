package com.umc.connext.common.enums;

public enum SectionType {
    WALL("벽"),
    SEAT("좌석"),
    STAGE("무대"),
    ENTRANCE("입구"),
    EXIT("출구"),
    CORRIDOR("통로"),
    UNKNOWN("기타");

    private final String description;

    SectionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
package com.umc.connext.domain.searchhistory.dto;

import com.umc.connext.domain.searchhistory.entity.SearchType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SearchHistoryCreateRequestDTO {
    @NotNull
    private String keyword;

    @NotNull
    private SearchType searchType;
}

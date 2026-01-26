package com.umc.connext.domain.searchhistory.dto;

import com.umc.connext.domain.searchhistory.entity.SearchType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SearchHistoryResponse {
    private Long id;
    private String keyword;
    private SearchType searchType;
}

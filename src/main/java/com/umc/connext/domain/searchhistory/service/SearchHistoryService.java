package com.umc.connext.domain.searchhistory.service;

import com.umc.connext.domain.searchhistory.dto.SearchHistoryCreateRequest;
import com.umc.connext.domain.searchhistory.dto.SearchHistoryResponse;
import com.umc.connext.domain.searchhistory.entity.SearchHistory;
import com.umc.connext.domain.searchhistory.entity.SearchType;
import com.umc.connext.domain.searchhistory.repository.SearchHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchHistoryService {
    private final SearchHistoryRepository searchHistoryRepository;

    // 최근 검색어 추가
    public void addSearchHistory(Long memberId, SearchHistoryCreateRequest request) {
        SearchHistory history = SearchHistory.create(
                request.getKeyword(),
                memberId,
                request.getSearchType()
        );
    }

    // 최근 검색어 가져오기
    public List<SearchHistoryResponse> getSearchHistory(
            Long memberId,
            SearchType type
    ){
        return searchHistoryRepository
                .findByMemberIdAndTypeOrderByCreatedAtDesc(memberId, type)
                .stream()
                .map(h -> new SearchHistoryResponse(
                        h.getId(),
                        h.getKeyword(),
                        h.getType()
                ))
                .toList();
    }

    // 최근 검색어 하나 삭제
    public void deleteSearchHistory(Long memberId, Long searchHistoryId) {
        SearchHistory history = searchHistoryRepository.findById(searchHistoryId)
                .orElseThrow(() -> new RuntimeException("검색 기록 없음"));

        if (!history.getMemberId().equals(memberId)) {
            throw new RuntimeException("삭제 권한 없음");
        }

        searchHistoryRepository.delete(history);
    }

    // 검색어 전체 삭제
    public void deleteAllSearchHistory(Long memberId, SearchType type) {
        searchHistoryRepository.deleteByMemberIdAndType(memberId, type);
    }
}

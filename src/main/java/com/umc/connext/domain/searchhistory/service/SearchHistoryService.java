package com.umc.connext.domain.searchhistory.service;

import com.umc.connext.domain.searchhistory.dto.SearchHistoryCreateRequestDTO;
import com.umc.connext.domain.searchhistory.dto.SearchHistoryResponseDTO;
import com.umc.connext.domain.searchhistory.entity.SearchHistory;
import com.umc.connext.domain.searchhistory.entity.SearchType;
import com.umc.connext.domain.searchhistory.repository.SearchHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.umc.connext.common.exception.GeneralException;
import com.umc.connext.common.code.ErrorCode;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchHistoryService {
    private final SearchHistoryRepository searchHistoryRepository;

    // 최근 검색어 추가
    @Transactional
    public void addSearchHistory(Long memberId, SearchHistoryCreateRequestDTO request) {
        SearchHistory history = SearchHistory.create(
                request.getKeyword(),
                memberId,
                request.getSearchType()
        );

        searchHistoryRepository.save(history);
    }

    // 최근 검색어 가져오기
    @Transactional(readOnly = true)
    public List<SearchHistoryResponseDTO> getSearchHistory(
            Long memberId,
            SearchType type
    ) {
        return searchHistoryRepository
                .findTop7ByMemberIdAndTypeOrderByCreatedAtDesc(memberId, type)
                .stream()
                .map(h -> new SearchHistoryResponseDTO(
                        h.getId(),
                        h.getKeyword(),
                        h.getType()
                ))
                .toList();
    }

    // 최근 검색어 하나 삭제
    @Transactional
    public void deleteSearchHistory(Long memberId, Long searchHistoryId) {
        SearchHistory history = searchHistoryRepository.findById(searchHistoryId)
                .orElseThrow(() -> GeneralException.notFound("검색 기록 없음"));

        if (!history.getMemberId().equals(memberId)) {
            throw new GeneralException(ErrorCode.FORBIDDEN,"삭제 권한 없음");
        }

        searchHistoryRepository.delete(history);
    }

    // 검색어 전체 삭제
    @Transactional
    public void deleteAllSearchHistory(Long memberId, SearchType type) {
        searchHistoryRepository.deleteByMemberIdAndType(memberId, type);
    }
}

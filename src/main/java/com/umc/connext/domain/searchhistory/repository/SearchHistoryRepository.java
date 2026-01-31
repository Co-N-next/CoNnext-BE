package com.umc.connext.domain.searchhistory.repository;

import com.umc.connext.domain.searchhistory.entity.SearchHistory;
import com.umc.connext.domain.searchhistory.entity.SearchType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SearchHistoryRepository
        extends JpaRepository<SearchHistory, Long> {

    List<SearchHistory> findTop7ByMemberIdAndTypeOrderByCreatedAtDesc(
            Long memberId,
            SearchType type
    );
    @Modifying
    @Query("DELETE FROM SearchHistory sh WHERE sh.memberId = :memberId AND sh.type = :type")
    void deleteByMemberIdAndType(Long memberId, SearchType type);
}

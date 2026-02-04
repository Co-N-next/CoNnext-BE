package com.umc.connext.domain.searchhistory.entity;

import com.umc.connext.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "search_history",
        indexes = {
                @Index(
                        name = "idx_search_history_member_type_created",
                        columnList = "member_id, type, created_at DESC"
                )
        })
public class SearchHistory extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "keyword", nullable = false)
    private String keyword;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(name = "search_type", nullable = false)
    private SearchType type;

    public static SearchHistory create(
            String keyword,
            Long memberId,
            SearchType type
    ) {
        SearchHistory history = new SearchHistory();
        history.keyword = keyword;
        history.memberId = memberId;
        history.type = type;
        return history;
    }
}

package com.umc.connext.domain.searchhistory.entity;

import com.umc.connext.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "searchHistory")
public class SearchHistory extends BaseEntity {
    @Id
    @Column(name = "searchHistory_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String keyword;

    @Column(nullable = false)
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
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

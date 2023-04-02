package com.matzip.server.domain.search.repository;

import com.matzip.server.domain.search.dto.SearchDto.ReviewSearch;
import com.matzip.server.domain.search.model.ReviewDocument;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ReviewSearchQueryRepository {
    private final ElasticsearchOperations operations;

    public ReviewSearchQueryRepository(ElasticsearchOperations operations) {
        this.operations = operations;
    }

    List<ReviewDocument> findByContent(ReviewSearch request) {
        CriteriaQuery query = new CriteriaQuery(Criteria.where("content").contains(request.keyword()));
        query.setPageable(PageRequest.of(request.page(), request.size()));

        return operations.search(query, ReviewDocument.class)
                .stream().map(SearchHit::getContent)
                .toList();
    }
}

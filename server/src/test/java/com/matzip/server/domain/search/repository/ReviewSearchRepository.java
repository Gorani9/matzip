package com.matzip.server.domain.search.repository;

import com.matzip.server.domain.search.model.ReviewDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ReviewSearchRepository extends ElasticsearchRepository<ReviewDocument, Long> {
}

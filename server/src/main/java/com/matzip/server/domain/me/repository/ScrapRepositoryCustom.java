package com.matzip.server.domain.me.repository;

import com.matzip.server.domain.me.dto.ScrapDto;
import com.matzip.server.domain.me.model.Scrap;
import org.springframework.data.domain.Slice;

public interface ScrapRepositoryCustom {
    Slice<Scrap> searchMyScrapsByKeyword(ScrapDto.SearchRequest searchRequest, Long myId);
}

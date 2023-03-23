package com.matzip.server.domain.record.repository;

import com.matzip.server.domain.record.model.ReviewRecord;
import org.springframework.data.repository.CrudRepository;

public interface ReviewRecordRepository extends CrudRepository<ReviewRecord, String> {
}

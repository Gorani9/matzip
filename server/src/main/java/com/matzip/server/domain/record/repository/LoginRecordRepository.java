package com.matzip.server.domain.record.repository;

import com.matzip.server.domain.record.model.LoginRecord;
import org.springframework.data.repository.CrudRepository;

public interface LoginRecordRepository extends CrudRepository<LoginRecord, Long> {
}

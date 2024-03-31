package com.tutran.libraryeventsconsumer.jpa;

import com.tutran.libraryeventsconsumer.entity.FailureRecord;
import org.springframework.data.repository.CrudRepository;

public interface FailureRecordRepository extends CrudRepository<FailureRecord, Integer> {
}

package com.tutran.libraryeventsconsumer.service;

import com.tutran.libraryeventsconsumer.entity.FailureRecord;
import com.tutran.libraryeventsconsumer.jpa.FailureRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class FailureService {
    private final FailureRecordRepository failureRecordRepository;

    public void saveFailedRecord(ConsumerRecord<Integer, String> consumerRecord, Exception e, String status) {
        var failureRecord = new FailureRecord(
                null,
                consumerRecord.topic(),
                consumerRecord.key(),
                consumerRecord.value(),
                consumerRecord.partition(),
                consumerRecord.offset(),
                e.getMessage(),
                status
        );
        failureRecordRepository.save(failureRecord);
    }
}

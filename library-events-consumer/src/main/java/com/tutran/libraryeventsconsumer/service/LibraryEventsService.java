package com.tutran.libraryeventsconsumer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tutran.libraryeventsconsumer.entity.LibraryEvent;
import com.tutran.libraryeventsconsumer.jpa.LibraryEventsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class LibraryEventsService {
    private final LibraryEventsRepository libraryEventsRepository;
    private final ObjectMapper objectMapper;

    public void processLibraryEvent(ConsumerRecord<Integer, String> consumerRecord) throws JsonProcessingException {
        var libraryEvent = objectMapper.readValue(consumerRecord.value(), LibraryEvent.class);
        if (libraryEvent != null && libraryEvent.getLibraryEventId() == 999) {
            throw new RecoverableDataAccessException("Temporary Network Issue");
        }

        switch (libraryEvent.getLibraryEventType()) {
            case NEW -> save(libraryEvent);
            case UPDATE -> {
                validate(libraryEvent);
                save(libraryEvent);
            }
            default -> log.info("Invalid Library Event Type");
        }
    }

    private void validate(LibraryEvent libraryEvent) {
        if (libraryEvent.getLibraryEventId() == null) {
            throw new IllegalArgumentException("Library Event Id is missing");
        }

        Optional<LibraryEvent> libraryEventOptional = libraryEventsRepository.findById(libraryEvent.getLibraryEventId());
        if (libraryEventOptional.isEmpty()) {
            throw new IllegalArgumentException("Not a valid library event");
        }
    }

    private void save(LibraryEvent libraryEvent) {
        libraryEvent.getBook().setLibraryEvent(libraryEvent);
        libraryEventsRepository.save(libraryEvent);
    }
}

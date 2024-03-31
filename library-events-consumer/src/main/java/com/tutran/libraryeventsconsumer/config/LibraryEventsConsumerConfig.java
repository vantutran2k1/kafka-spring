package com.tutran.libraryeventsconsumer.config;

import com.tutran.libraryeventsconsumer.service.FailureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConsumerRecordRecoverer;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;

import java.util.List;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class LibraryEventsConsumerConfig {
    private static final String RETRY = "RETRY";
    private static final String DEAD = "DEAD";

    private final KafkaTemplate<?, ?> kafkaTemplate;
    private final FailureService failureService;

    @Value("${topics.retry}")
    private String retryTopic;

    @Value("${topics.dlt}")
    private String deadLetterTopic;

    public DeadLetterPublishingRecoverer publishingRecoverer() {
        return new DeadLetterPublishingRecoverer(kafkaTemplate, (r, e) -> {
            if (e.getCause() instanceof RecoverableDataAccessException) {
                return new TopicPartition(retryTopic, r.partition());
            } else {
                return new TopicPartition(deadLetterTopic, r.partition());
            }
        });
    }

    ConsumerRecordRecoverer consumerRecordRecoverer = (consumerRecord, e) -> {
        var castRecord = (ConsumerRecord<Integer, String>) consumerRecord;
        if (e.getCause() instanceof RecoverableDataAccessException) {
            failureService.saveFailedRecord(castRecord, e, RETRY);
        } else {
            failureService.saveFailedRecord(castRecord, e, DEAD);
        }
    };

    public DefaultErrorHandler errorHandler() {
        var backOff = new ExponentialBackOffWithMaxRetries(2);
        backOff.setInitialInterval(1000L);
        backOff.setMultiplier(2.0);
        backOff.setMaxInterval(2000L);

        var errorHandler = new DefaultErrorHandler(publishingRecoverer(), backOff);
        errorHandler.setRetryListeners(((record, ex, deliveryAttempt) -> {
            log.info("Failed Record in Retry Listener, exception: {}, deliveryAttempt: {}", ex.getMessage(), deliveryAttempt);
        }));

        var ignoredExceptions = List.of(
                IllegalArgumentException.class
        );
        ignoredExceptions.forEach(errorHandler::addNotRetryableExceptions);

        return errorHandler;
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<?, ?> kafkaListenerContainerFactory(
            ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
            ConsumerFactory<Object, Object> kafkaConsumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<Object, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        configurer.configure(factory, kafkaConsumerFactory);
        factory.setConcurrency(3);
        factory.setCommonErrorHandler(errorHandler());
        return factory;
    }
}

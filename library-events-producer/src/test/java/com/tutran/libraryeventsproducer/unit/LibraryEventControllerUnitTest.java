package com.tutran.libraryeventsproducer.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tutran.libraryeventsproducer.controller.LibraryEventController;
import com.tutran.libraryeventsproducer.domain.LibraryEvent;
import com.tutran.libraryeventsproducer.producer.LibraryEventsProducer;
import com.tutran.libraryeventsproducer.utils.TestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LibraryEventController.class)
class LibraryEventControllerUnitTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    LibraryEventsProducer libraryEventsProducer;

    @Test
    void createLibraryEvent() throws Exception {
        var body = objectMapper.writeValueAsString(TestUtils.libraryEventRecord());
        when(libraryEventsProducer.sendLibraryEvent(isA(LibraryEvent.class))).thenReturn(null);

        mockMvc.perform(MockMvcRequestBuilders.post("/v1/libraryevent")
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    void createLibraryEvent_4xx() throws Exception {
        var body = objectMapper.writeValueAsString(TestUtils.libraryEventRecordWithInvalidBook());
        when(libraryEventsProducer.sendLibraryEvent(isA(LibraryEvent.class))).thenReturn(null);

        var expectedErrorMessage = "book.bookId - must not be null, book.bookName - must not be blank";

        mockMvc.perform(MockMvcRequestBuilders.post("/v1/libraryevent")
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string(expectedErrorMessage));
    }
}
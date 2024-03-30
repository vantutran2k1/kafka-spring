package com.tutran.libraryeventsproducer.domain;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record LibraryEvent(
        @NotNull
        Integer libraryEventId,
        LibraryEventType libraryEventType,

        @NotNull
        @Valid
        Book book
) {
}

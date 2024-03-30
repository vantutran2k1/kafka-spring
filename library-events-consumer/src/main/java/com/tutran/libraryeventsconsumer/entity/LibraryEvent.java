package com.tutran.libraryeventsconsumer.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class LibraryEvent {
    @Id
    private Integer libraryEventId;

    @Enumerated(EnumType.STRING)
    LibraryEventType libraryEventType;

    @OneToOne(mappedBy = "libraryEvent", cascade = {CascadeType.ALL})
    @ToString.Exclude
    private Book book;
}

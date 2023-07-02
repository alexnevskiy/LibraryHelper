package com.poly.domains.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "event")
public class EventEntity {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_reader")
    private ReaderEntity reader;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_book")
    private BookEntity book;

    @Column(name = "event_type")
    private String eventType;

    @Column(name = "event_datetime")
    private LocalDateTime eventDatetime;

    public EventEntity(int id, ReaderEntity reader, BookEntity book, String eventType, LocalDateTime eventDatetime) {
        this.id = id;
        this.reader = reader;
        this.book = book;
        this.eventType = eventType;
        this.eventDatetime = eventDatetime;
    }

    public EventEntity() {}

    public int getId() {
        return id;
    }

    public ReaderEntity getReader() {
        return reader;
    }

    public BookEntity getBook() {
        return book;
    }

    public String getEventType() {
        return eventType;
    }

    public LocalDateTime getEventDatetime() {
        return eventDatetime;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setReader(ReaderEntity reader) {
        this.reader = reader;
    }

    public void setBook(BookEntity book) {
        this.book = book;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public void setEventDatetime(LocalDateTime eventDatetime) {
        this.eventDatetime = eventDatetime;
    }
}

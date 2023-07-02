package com.poly.services;

import com.poly.domains.dto.EventDto;
import com.poly.domains.dto.EventTypeDto;
import com.poly.domains.entities.BookEntity;
import com.poly.domains.entities.EventEntity;
import com.poly.domains.entities.ReaderEntity;
import com.poly.exceptions.NotFoundException;
import com.poly.exceptions.UnsupportedArgumentException;
import com.poly.repositories.BookRepository;
import com.poly.repositories.EventRepository;
import com.poly.repositories.ReaderRepository;
import com.poly.utils.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class EventService {

    private final static Logger logger = LoggerFactory.getLogger(EventService.class);

    private final EventRepository eventRepository;
    private final ReaderRepository readerRepository;
    private final BookRepository bookRepository;

    public EventService(EventRepository eventRepository,
                        ReaderRepository readerRepository,
                        BookRepository bookRepository) {
        this.eventRepository = eventRepository;
        this.readerRepository = readerRepository;
        this.bookRepository = bookRepository;
    }

    public EventDto createEvent(EventDto eventDto) throws NotFoundException {
        ReaderEntity reader = readerRepository
                .findById(eventDto.idReader())
                .orElseThrow(() -> new NotFoundException("Reader not found"));
        BookEntity book = bookRepository
                .findById(eventDto.idBook())
                .orElseThrow(() -> new NotFoundException("Book not found"));
        EventEntity event = new EventEntity();
        event.setReader(reader);
        event.setBook(book);
        event.setEventType(validateEventType(eventDto.eventType()).name());
        event.setEventDatetime(LocalDateTime.parse(eventDto.eventDatetime(), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS")));
        EventEntity eventEntity = eventRepository.save(event);
        logger.info("Event created: {}", eventDto);
        return new EventDto(eventEntity.getId(),
                eventEntity.getBook().getId(),
                eventEntity.getReader().getId(),
                eventEntity.getEventType(),
                eventEntity.getEventDatetime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS")));
    }

    public List<EventTypeDto> getEventTypes() {
        List<EventTypeDto> eventTypesDto = new ArrayList<>();
        for (EventType eventType : EventType.values()) {
            eventTypesDto.add(new EventTypeDto(eventType.name()));
        }
        return eventTypesDto;
    }

    public EventType validateEventType(String type) {
        try {
            return EventType.valueOf(type);
        } catch (IllegalArgumentException e) {
            throw new UnsupportedArgumentException("Unsupported event type: " + type);
        }
    }
}

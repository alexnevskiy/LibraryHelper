package com.poly.services;

import com.poly.domains.dto.BookDto;
import com.poly.domains.entities.BookEntity;
import com.poly.domains.entities.EventEntity;
import com.poly.exceptions.NotFoundException;
import com.poly.repositories.BookRepository;
import com.poly.repositories.EventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BookService {

    private final static Logger logger = LoggerFactory.getLogger(BookService.class);

    private final BookRepository bookRepository;
    private final EventRepository eventRepository;

    public BookService(BookRepository bookRepository, EventRepository eventRepository) {
        this.bookRepository = bookRepository;
        this.eventRepository = eventRepository;
    }

    public List<BookDto> getBooks() {
        List<BookEntity> books = bookRepository.findAll();
        List<BookDto> booksDto = new ArrayList<>();
        for (BookEntity book: books) {
            booksDto.add(new BookDto(book.getId(), book.getName(), book.getAuthor()));
        }
        return booksDto;
    }

    public BookDto createBook(BookDto bookDto) {
        BookEntity book = new BookEntity();
        book.setName(bookDto.name());
        book.setAuthor(bookDto.author());
        BookEntity bookEntity = bookRepository.save(book);
        BookDto newBook = new BookDto(bookEntity.getId(), bookEntity.getName(), bookEntity.getAuthor());
        logger.info("Book created: {}", newBook);
        return newBook;
    }

    public BookDto updateBook(BookDto bookDto) throws NotFoundException {
        BookEntity book = bookRepository
                .findById(bookDto.id())
                .orElseThrow(() -> new NotFoundException("Book not found"));
        book.setName(bookDto.name());
        book.setAuthor(bookDto.author());
        BookEntity bookEntity = bookRepository.save(book);
        logger.info("Book updated: {}", bookDto);
        return new BookDto(bookEntity.getId(), bookEntity.getName(), bookEntity.getAuthor());
    }

    public BookDto getMostPopularBook(String startInterval, String endInterval) throws NotFoundException {
        List<EventEntity> events = eventRepository.findAll();
        LocalDateTime startDateTime = LocalDateTime.parse(startInterval);
        LocalDateTime endDateTime = LocalDateTime.parse(endInterval);
        BookEntity book = events.stream()
                .filter(eventEntity -> eventEntity.getEventDatetime().isAfter(startDateTime) &&
                        eventEntity.getEventDatetime().isBefore(endDateTime))
                .collect(Collectors.groupingBy(EventEntity::getBook, Collectors.counting()))
                .entrySet().stream()
                .max((entry1, entry2) -> {
                    int difference = (int) (entry1.getValue() - entry2.getValue());
                    return difference != 0 ? difference : -entry1.getKey().getAuthor().compareTo(entry2.getKey().getAuthor());
                })
                .map(Map.Entry::getKey)
                .orElseThrow(() -> new NotFoundException("Most popular book not found"));
        return new BookDto(book.getId(), book.getName(), book.getAuthor());
    }
}

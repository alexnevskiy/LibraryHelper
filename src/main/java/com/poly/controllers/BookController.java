package com.poly.controllers;

import com.poly.domains.dto.BookDto;
import com.poly.exceptions.NotFoundException;
import com.poly.services.BookService;
import com.poly.utils.ApplicationConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(ApplicationConstants.API_V1 + "/book")
public class BookController {

    private final static Logger logger = LoggerFactory.getLogger(BookController.class);

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping("/")
    public ResponseEntity<List<BookDto>> getBooks() {
        List<BookDto> books = bookService.getBooks();
        return new ResponseEntity<>(books, HttpStatus.OK);
    }

    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BookDto> createBook(@RequestBody BookDto bookDto) {
        BookDto book = bookService.createBook(bookDto);
        return new ResponseEntity<>(book, HttpStatus.CREATED);
    }

    @PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BookDto> updateBook(@RequestBody BookDto bookDto) {
        try {
            BookDto book = bookService.updateBook(bookDto);
            return new ResponseEntity<>(book, HttpStatus.OK);
        } catch (NotFoundException e) {
            logger.warn(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/popular")
    public ResponseEntity<BookDto> getMostPopularBook(@RequestParam String start, @RequestParam String end) {
        if (start == null || end == null) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        try {
            BookDto book = bookService.getMostPopularBook(start, end);
            return new ResponseEntity<>(book, HttpStatus.OK);
        } catch (NotFoundException e) {
            logger.warn(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}

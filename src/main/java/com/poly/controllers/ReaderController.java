package com.poly.controllers;

import com.poly.domains.dto.ReaderDto;
import com.poly.exceptions.NotFoundException;
import com.poly.services.ReaderService;
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
@RequestMapping(ApplicationConstants.API_V1 + "/reader")
public class ReaderController {

    private final static Logger logger = LoggerFactory.getLogger(ReaderController.class);

    private final ReaderService readerService;

    public ReaderController(ReaderService readerService) {
        this.readerService = readerService;
    }

    @GetMapping("/")
    public ResponseEntity<List<ReaderDto>> getReaders() {
        List<ReaderDto> readers = readerService.getReaders();
        return new ResponseEntity<>(readers, HttpStatus.OK);
    }

    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ReaderDto> createReader(@RequestBody ReaderDto readerDto) {
        ReaderDto reader = readerService.createReader(readerDto);
        return new ResponseEntity<>(reader, HttpStatus.CREATED);
    }

    @PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ReaderDto> updateReader(@RequestBody ReaderDto readerDto) {
        try {
            ReaderDto reader = readerService.updateReader(readerDto);
            return new ResponseEntity<>(reader, HttpStatus.OK);
        } catch (NotFoundException e) {
            logger.warn(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/most")
    public ResponseEntity<ReaderDto> getMostReader(@RequestParam String start, @RequestParam String end) {
        if (start == null || end == null) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        try {
            ReaderDto reader = readerService.getMostReader(start, end);
            return new ResponseEntity<>(reader, HttpStatus.OK);
        } catch (NotFoundException e) {
            logger.warn(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}

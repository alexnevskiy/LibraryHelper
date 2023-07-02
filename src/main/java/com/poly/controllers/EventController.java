package com.poly.controllers;

import com.poly.domains.dto.EventDto;
import com.poly.domains.dto.EventTypeDto;
import com.poly.exceptions.NotFoundException;
import com.poly.services.EventService;
import com.poly.utils.ApplicationConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(ApplicationConstants.API_V1 + "/event")
public class EventController {

    private final static Logger logger = LoggerFactory.getLogger(EventController.class);

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EventDto> createEvent(@RequestBody EventDto eventDto) {
        try {
            EventDto event = eventService.createEvent(eventDto);
            return new ResponseEntity<>(event, HttpStatus.CREATED);
        } catch (NotFoundException e) {
            logger.warn(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/")
    public ResponseEntity<List<EventTypeDto>> getEventTypes() {
        List<EventTypeDto> eventTypes = eventService.getEventTypes();
        return new ResponseEntity<>(eventTypes, HttpStatus.OK);
    }
}

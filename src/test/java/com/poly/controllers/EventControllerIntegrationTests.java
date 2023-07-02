package com.poly.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.poly.domains.dto.BookDto;
import com.poly.domains.dto.EventDto;
import com.poly.domains.dto.EventTypeDto;
import com.poly.domains.dto.ReaderDto;
import com.poly.domains.entities.BookEntity;
import com.poly.domains.entities.EventEntity;
import com.poly.domains.entities.ReaderEntity;
import com.poly.exceptions.UnsupportedArgumentException;
import com.poly.repositories.BookRepository;
import com.poly.repositories.EventRepository;
import com.poly.repositories.ReaderRepository;
import com.poly.utils.ApplicationConstants;
import com.poly.utils.EventType;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import static com.poly.utils.ApplicationConstants.DATE_FORMATTER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SuppressWarnings("resource")
public class EventControllerIntegrationTests {

    private static final int LEFT_LIMIT = 97; // letter 'a'
    private static final int RIGHT_LIMIT = 122; // letter 'z'
    private static final int STRING_LENGTH = 10;
    private static final Random random = new Random();

    private final static PostgreSQLContainer<?> postgreSQLContainer =
            new PostgreSQLContainer<>("postgres:14.7-alpine").withReuse(true);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookRepository backFieldBookRepository;

    @Autowired
    private ReaderRepository backFieldReaderRepository;

    @Autowired
    private EventRepository backFieldEventRepository;

    private static BookRepository bookRepository;
    private static ReaderRepository readerRepository;
    private static EventRepository eventRepository;

    @PostConstruct
    private void initBookRepository() {
        assertNotNull(backFieldBookRepository);
        assertNotNull(backFieldReaderRepository);
        assertNotNull(backFieldEventRepository);
        bookRepository = backFieldBookRepository;
        readerRepository = backFieldReaderRepository;
        eventRepository = backFieldEventRepository;
    }

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
    }

    @BeforeAll
    static void startContainer() {
        postgreSQLContainer.start();
    }

    @AfterAll
    static void deleteData() {
        postgreSQLContainer.stop();
    }

    @Test
    @Order(1)
    void testGetEventTypes() throws Exception {
        List<EventTypeDto> eventTypesDto = new ArrayList<>();
        for (EventType eventType : EventType.values()) {
            eventTypesDto.add(new EventTypeDto(eventType.name()));
        }

        mockMvc.perform(get("/" + ApplicationConstants.API_V1 + "/event/"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(eventTypesDto)));
    }

    @Test
    @Order(10)
    void testCreateEvents() throws Exception {
        ReaderDto readerDto1 = generateReaderDto();
        ReaderDto readerDto2 = generateReaderDto();

        mockMvc.perform(post("/" + ApplicationConstants.API_V1 + "/reader/create")
                        .content(objectMapper.writeValueAsString(readerDto1))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/" + ApplicationConstants.API_V1 + "/reader/create")
                        .content(objectMapper.writeValueAsString(readerDto2))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        BookDto bookDto1 = generateBookDto();
        BookDto bookDto2 = generateBookDto();

        mockMvc.perform(post("/" + ApplicationConstants.API_V1 + "/book/create")
                        .content(objectMapper.writeValueAsString(bookDto1))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/" + ApplicationConstants.API_V1 + "/book/create")
                        .content(objectMapper.writeValueAsString(bookDto2))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        List<BookEntity> books = bookRepository.findAll();
        List<ReaderEntity> readers = readerRepository.findAll();

        assertEquals(2, readers.size());
        assertEquals(2, books.size());

        EventDto take1 = generateTakeEventDto(readers.get(0).getId(), books.get(0).getId());
        EventDto return1 = generateReturnEventDto(take1.idReader(), take1.idBook(), take1.eventDatetime());

        EventDto take2 = generateTakeEventDto(readers.get(1).getId(), books.get(1).getId());

        MvcResult mvcTake1 = mockMvc.perform(post("/" + ApplicationConstants.API_V1 + "/event/create")
                        .content(objectMapper.writeValueAsString(take1))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();
        MvcResult mvcReturn1 = mockMvc.perform(post("/" + ApplicationConstants.API_V1 + "/event/create")
                        .content(objectMapper.writeValueAsString(return1))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();
        MvcResult mvcTake2 = mockMvc.perform(post("/" + ApplicationConstants.API_V1 + "/event/create")
                        .content(objectMapper.writeValueAsString(take2))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();

        EventDto createdTake1 = objectMapper.readValue(
                mvcTake1.getResponse().getContentAsString(),
                new TypeReference<>() {}
        );
        EventDto returnedTake1 = objectMapper.readValue(
                mvcReturn1.getResponse().getContentAsString(),
                new TypeReference<>() {}
        );
        EventDto createdTake2 = objectMapper.readValue(
                mvcTake2.getResponse().getContentAsString(),
                new TypeReference<>() {}
        );

        List<EventEntity> events = eventRepository.findAll();
        assertEquals(createdTake1, new EventDto(events.get(0).getId(),
                events.get(0).getReader().getId(),
                events.get(0).getBook().getId(),
                events.get(0).getEventType(),
                events.get(0).getEventDatetime().format(DATE_FORMATTER)));
        assertEquals(returnedTake1, new EventDto(events.get(1).getId(),
                events.get(1).getReader().getId(),
                events.get(1).getBook().getId(),
                events.get(1).getEventType(),
                events.get(1).getEventDatetime().format(DATE_FORMATTER)));
        assertEquals(createdTake2, new EventDto(events.get(2).getId(),
                events.get(2).getReader().getId(),
                events.get(2).getBook().getId(),
                events.get(2).getEventType(),
                events.get(2).getEventDatetime().format(DATE_FORMATTER)));
    }

    @Test
    @Order(11)
    void testCreateEventIncorrectEventType() throws Exception {
        List<BookEntity> books = bookRepository.findAll();
        List<ReaderEntity> readers = readerRepository.findAll();

        EventDto takeBook = new EventDto(null,
                readers.get(0).getId(),
                books.get(1).getId(),
                "BAD_TYPE",
                LocalDateTime.now().format(DATE_FORMATTER));

        mockMvc.perform(post("/" + ApplicationConstants.API_V1 + "/event/create")
                        .content(objectMapper.writeValueAsString(takeBook))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof UnsupportedArgumentException))
                .andExpect(result -> assertEquals("Unsupported event type: BAD_TYPE",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));
    }

    @Test
    @Order(12)
    void testCreateEventIncorrectReaderIdType() throws Exception {
        List<BookEntity> books = bookRepository.findAll();

        EventDto takeBook = generateTakeEventDto(-1, books.get(0).getId());

        mockMvc.perform(post("/" + ApplicationConstants.API_V1 + "/event/create")
                        .content(objectMapper.writeValueAsString(takeBook))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    private BookDto generateBookDto() {
        return new BookDto(null,
                generateString(),
                generateString());
    }

    private ReaderDto generateReaderDto() {
        return new ReaderDto(null,
                generateString(),
                generateString());
    }

    private EventDto generateTakeEventDto(Integer idReader, Integer idBook) {
        return new EventDto(null,
                idReader,
                idBook,
                EventType.TAKE_BOOK.name(),
                LocalDateTime.now().format(DATE_FORMATTER));
    }

    private EventDto generateReturnEventDto(Integer idReader, Integer idBook, String datetime) {
        return new EventDto(null,
                idReader,
                idBook,
                EventType.RETURN_BOOK.name(),
                LocalDateTime.parse(datetime).plusHours(1).format(DATE_FORMATTER));
    }

    private String generateString() {
        return random.ints(LEFT_LIMIT, RIGHT_LIMIT + 1)
                .limit(STRING_LENGTH)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }
}

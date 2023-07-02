package com.poly.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.poly.domains.dto.BookDto;
import com.poly.domains.dto.EventDto;
import com.poly.domains.dto.ReaderDto;
import com.poly.domains.entities.BookEntity;
import com.poly.domains.entities.EventEntity;
import com.poly.domains.entities.ReaderEntity;
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
import java.util.Random;

import static com.poly.utils.ApplicationConstants.DATE_FORMATTER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SuppressWarnings("resource")
class ReaderControllerIntegrationTests {

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
    void testGetEmptyReaders() throws Exception {
        mockMvc.perform(get("/" + ApplicationConstants.API_V1 + "/reader/"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(new ArrayList<ReaderDto>())));
    }

    @Test
    @Order(10)
    void testUpdateNonExistentReader() throws Exception {
        ReaderDto readerDto = generateReaderDto(random.nextInt());

        mockMvc.perform(put("/" + ApplicationConstants.API_V1 + "/reader/update")
                        .content(objectMapper.writeValueAsString(readerDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(20)
    void testCreateReaders() throws Exception {
        ReaderDto readerDto1 = generateReaderDto(null);
        ReaderDto readerDto2 = generateReaderDto(null);

        mockMvc.perform(post("/" + ApplicationConstants.API_V1 + "/reader/create")
                        .content(objectMapper.writeValueAsString(readerDto1))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/" + ApplicationConstants.API_V1 + "/reader/create")
                        .content(objectMapper.writeValueAsString(readerDto2))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    @Order(30)
    void testGetReaders() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/" + ApplicationConstants.API_V1 + "/reader/"))
                .andExpect(status().isOk())
                .andReturn();
        List<ReaderDto> readers = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                new TypeReference<>() {}
        );

        List<ReaderEntity> readerEntities = readerRepository.findAll();

        assertEquals(readers.get(0).firstName(), readerEntities.get(0).getFirstName());
        assertEquals(readers.get(0).lastName(), readerEntities.get(0).getLastName());

        assertEquals(readers.get(1).firstName(), readerEntities.get(1).getFirstName());
        assertEquals(readers.get(1).lastName(), readerEntities.get(1).getLastName());
    }

    @Test
    @Order(40)
    void testUpdateReaders() throws Exception {
        List<ReaderEntity> readerEntities = readerRepository.findAll();

        ReaderDto readerDto1 = generateReaderDto(readerEntities.get(0).getId());
        ReaderDto readerDto2 = generateReaderDto(readerEntities.get(1).getId());

        MvcResult mvcResult1 = mockMvc.perform(put("/" + ApplicationConstants.API_V1 + "/reader/update")
                        .content(objectMapper.writeValueAsString(readerDto1))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        MvcResult mvcResult2 = mockMvc.perform(put("/" + ApplicationConstants.API_V1 + "/reader/update")
                        .content(objectMapper.writeValueAsString(readerDto2))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        ReaderDto updatedReaderDto1 = objectMapper.readValue(
                mvcResult1.getResponse().getContentAsString(),
                new TypeReference<>() {}
        );
        ReaderDto updatedReaderDto2 = objectMapper.readValue(
                mvcResult2.getResponse().getContentAsString(),
                new TypeReference<>() {}
        );

        List<ReaderEntity> updatedReaderEntities = readerRepository.findAll();

        assertEquals(updatedReaderEntities.get(0).getId(), updatedReaderDto1.id());
        assertEquals(updatedReaderEntities.get(0).getFirstName(), updatedReaderDto1.firstName());
        assertEquals(updatedReaderEntities.get(0).getLastName(), updatedReaderDto1.lastName());

        assertEquals(updatedReaderEntities.get(1).getId(), updatedReaderDto2.id());
        assertEquals(updatedReaderEntities.get(1).getFirstName(), updatedReaderDto2.firstName());
        assertEquals(updatedReaderEntities.get(1).getLastName(), updatedReaderDto2.lastName());
    }

    @Test
    @Order(50)
    void testGetMostReader() throws Exception {
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

        assertEquals(books.get(0).getName(), bookDto1.name());
        assertEquals(books.get(0).getAuthor(), bookDto1.author());

        assertEquals(books.get(1).getName(), bookDto2.name());
        assertEquals(books.get(1).getAuthor(), bookDto2.author());

        List<EventDto> eventDtoList = new ArrayList<>();

        EventDto take1 = generateTakeEventDto(readers.get(0).getId(), books.get(0).getId());
        eventDtoList.add(take1);
        eventDtoList.add(generateReturnEventDto(take1.idReader(), take1.idBook(), take1.eventDatetime()));

        EventDto take2 = generateTakeEventDto(readers.get(1).getId(), books.get(0).getId());
        eventDtoList.add(take2);
        eventDtoList.add(generateReturnEventDto(take2.idReader(), take2.idBook(), take2.eventDatetime()));

        EventDto take3 = generateTakeEventDto(readers.get(1).getId(), books.get(1).getId());
        eventDtoList.add(take3);
        eventDtoList.add(generateReturnEventDto(take3.idReader(), take3.idBook(), take3.eventDatetime()));

        for (EventDto eventDto : eventDtoList) {
            mockMvc.perform(post("/" + ApplicationConstants.API_V1 + "/event/create")
                            .content(objectMapper.writeValueAsString(eventDto))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated());
        }

        assertEquals(eventDtoList.size(), eventRepository.findAll().size());

        MvcResult mvcResult = mockMvc.perform(get("/" + ApplicationConstants.API_V1 + "/reader/most")
                        .param("start", LocalDateTime.now().minusDays(1).format(DATE_FORMATTER))
                        .param("end", LocalDateTime.now().plusDays(1).format(DATE_FORMATTER)))
                .andExpect(status().isOk())
                .andReturn();
        ReaderDto mostReaderDto = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                new TypeReference<>() {}
        );

        assertEquals(
                new ReaderDto(readers.get(1).getId(), readers.get(1).getFirstName(), readers.get(1).getLastName()),
                mostReaderDto
        );
    }

    @Test
    @Order(51)
    void testGetMostReaderLastOrder() throws Exception {
        List<BookEntity> books = bookRepository.findAll();
        List<ReaderEntity> readers = readerRepository.findAll();
        List<EventEntity> events = eventRepository.findAll();

        EventDto takeBook = generateTakeEventDto(readers.get(0).getId(), books.get(1).getId());
        EventDto returnBook = generateReturnEventDto(takeBook.idReader(), takeBook.idBook(), takeBook.eventDatetime());

        mockMvc.perform(post("/" + ApplicationConstants.API_V1 + "/event/create")
                        .content(objectMapper.writeValueAsString(takeBook))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/" + ApplicationConstants.API_V1 + "/event/create")
                        .content(objectMapper.writeValueAsString(returnBook))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        assertEquals(events.size() + 2, eventRepository.findAll().size());

        MvcResult mvcResult = mockMvc.perform(get("/" + ApplicationConstants.API_V1 + "/reader/most")
                        .param("start", LocalDateTime.now().minusDays(1).format(DATE_FORMATTER))
                        .param("end", LocalDateTime.now().plusDays(1).format(DATE_FORMATTER)))
                .andExpect(status().isOk())
                .andReturn();
        ReaderDto mostReaderDto = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                new TypeReference<>() {}
        );

        assertEquals(
                new ReaderDto(readers.get(0).getId(), readers.get(0).getFirstName(), readers.get(0).getLastName()),
                mostReaderDto
        );
    }

    @Test
    @Order(52)
    void testGetMostReaderWithoutArgsBadRequest() throws Exception {
        mockMvc.perform(get("/" + ApplicationConstants.API_V1 + "/reader/most"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(53)
    void testGetMostReaderNoReadersBadRequest() throws Exception {
        mockMvc.perform(get("/" + ApplicationConstants.API_V1 + "/reader/most")
                .param("start", LocalDateTime.now().minusYears(1).format(DATE_FORMATTER))
                .param("end", LocalDateTime.now().minusYears(1).format(DATE_FORMATTER)))
                .andExpect(status().isOk());
    }

    private BookDto generateBookDto() {
        return new BookDto(null,
                generateString(),
                generateString());
    }

    private ReaderDto generateReaderDto(Integer id) {
        return new ReaderDto(id,
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

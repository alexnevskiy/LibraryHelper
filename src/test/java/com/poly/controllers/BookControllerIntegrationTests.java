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
import java.util.Comparator;
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
public class BookControllerIntegrationTests {

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
    void testGetEmptyBooks() throws Exception {
        mockMvc.perform(get("/" + ApplicationConstants.API_V1 + "/book/"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(new ArrayList<BookDto>())));
    }

    @Test
    @Order(10)
    void testUpdateNonExistentBook() throws Exception {
        BookDto bookDto = generateBookDto(random.nextInt());

        mockMvc.perform(put("/" + ApplicationConstants.API_V1 + "/book/update")
                        .content(objectMapper.writeValueAsString(bookDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(20)
    void testCreateBooks() throws Exception {
        BookDto bookDto1 = generateBookDto(null);
        BookDto bookDto2 = generateBookDto(null);

        mockMvc.perform(post("/" + ApplicationConstants.API_V1 + "/book/create")
                        .content(objectMapper.writeValueAsString(bookDto1))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/" + ApplicationConstants.API_V1 + "/book/create")
                        .content(objectMapper.writeValueAsString(bookDto2))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    @Order(30)
    void testGetBooks() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/" + ApplicationConstants.API_V1 + "/book/"))
                .andExpect(status().isOk())
                .andReturn();
        List<BookDto> books = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                new TypeReference<>() {}
        );

        List<BookEntity> bookEntities = bookRepository.findAll();

        assertEquals(books.get(0).name(), bookEntities.get(0).getName());
        assertEquals(books.get(0).author(), bookEntities.get(0).getAuthor());

        assertEquals(books.get(1).name(), bookEntities.get(1).getName());
        assertEquals(books.get(1).author(), bookEntities.get(1).getAuthor());
    }

    @Test
    @Order(40)
    void testUpdateBooks() throws Exception {
        List<BookEntity> bookEntities = bookRepository.findAll();

        BookDto bookDto1 = generateBookDto(bookEntities.get(0).getId());
        BookDto bookDto2 = generateBookDto(bookEntities.get(1).getId());

        MvcResult mvcResult1 = mockMvc.perform(put("/" + ApplicationConstants.API_V1 + "/book/update")
                        .content(objectMapper.writeValueAsString(bookDto1))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        MvcResult mvcResult2 = mockMvc.perform(put("/" + ApplicationConstants.API_V1 + "/book/update")
                        .content(objectMapper.writeValueAsString(bookDto2))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        BookDto updatedBookDto1 = objectMapper.readValue(
                mvcResult1.getResponse().getContentAsString(),
                new TypeReference<>() {}
        );
        BookDto updatedBookDto2 = objectMapper.readValue(
                mvcResult2.getResponse().getContentAsString(),
                new TypeReference<>() {}
        );

        List<BookEntity> updatedBookEntities = bookRepository.findAll();

        assertEquals(updatedBookEntities.get(0).getId(), updatedBookDto1.id());
        assertEquals(updatedBookEntities.get(0).getName(), updatedBookDto1.name());
        assertEquals(updatedBookEntities.get(0).getAuthor(), updatedBookDto1.author());

        assertEquals(updatedBookEntities.get(1).getId(), updatedBookDto2.id());
        assertEquals(updatedBookEntities.get(1).getName(), updatedBookDto2.name());
        assertEquals(updatedBookEntities.get(1).getAuthor(), updatedBookDto2.author());
    }

    @Test
    @Order(50)
    void testGetMostPopularBook() throws Exception {
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

        List<BookEntity> books = bookRepository.findAll();
        List<ReaderEntity> readers = readerRepository.findAll();

        assertEquals(readers.get(0).getFirstName(), readerDto1.firstName());
        assertEquals(readers.get(0).getLastName(), readerDto1.lastName());

        assertEquals(readers.get(1).getFirstName(), readerDto2.firstName());
        assertEquals(readers.get(1).getLastName(), readerDto2.lastName());

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

        MvcResult mvcResult = mockMvc.perform(get("/" + ApplicationConstants.API_V1 + "/book/popular")
                        .param("start", LocalDateTime.now().minusDays(1).format(DATE_FORMATTER))
                        .param("end", LocalDateTime.now().plusDays(1).format(DATE_FORMATTER)))
                .andExpect(status().isOk())
                .andReturn();
        BookDto mostPopularBookDto = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                new TypeReference<>() {}
        );

        assertEquals(
                new BookDto(books.get(0).getId(), books.get(0).getName(), books.get(0).getAuthor()),
                mostPopularBookDto
        );
    }

    @Test
    @Order(51)
    void testGetMostPopularBookAlphabeticOrder() throws Exception {
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

        MvcResult mvcResult = mockMvc.perform(get("/" + ApplicationConstants.API_V1 + "/book/popular")
                        .param("start", LocalDateTime.now().minusDays(1).format(DATE_FORMATTER))
                        .param("end", LocalDateTime.now().plusDays(1).format(DATE_FORMATTER)))
                .andExpect(status().isOk())
                .andReturn();
        BookDto mostPopularBookDto = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                new TypeReference<>() {}
        );
        books.sort(Comparator.comparing(BookEntity::getAuthor));

        assertEquals(
                new BookDto(books.get(0).getId(), books.get(0).getName(), books.get(0).getAuthor()),
                mostPopularBookDto
        );
    }

    @Test
    @Order(52)
    void testGetMostPopularBookBadRequest() throws Exception {
        mockMvc.perform(get("/" + ApplicationConstants.API_V1 + "/book/popular"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(53)
    void testGetMostPopularBookNoBooksBadRequest() throws Exception {
        mockMvc.perform(get("/" + ApplicationConstants.API_V1 + "/reader/most")
                        .param("start", LocalDateTime.now().minusYears(1).format(DATE_FORMATTER))
                        .param("end", LocalDateTime.now().minusYears(1).format(DATE_FORMATTER)))
                .andExpect(status().isOk());
    }

    private BookDto generateBookDto(Integer id) {
        return new BookDto(id,
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

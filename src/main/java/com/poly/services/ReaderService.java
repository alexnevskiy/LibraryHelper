package com.poly.services;

import com.poly.domains.dto.ReaderDto;
import com.poly.domains.entities.EventEntity;
import com.poly.domains.entities.ReaderEntity;
import com.poly.exceptions.NotFoundException;
import com.poly.repositories.ReaderRepository;
import com.poly.utils.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ReaderService {

    private final static Logger logger = LoggerFactory.getLogger(ReaderService.class);

    private final ReaderRepository readerRepository;

    public ReaderService(ReaderRepository readerRepository) {
        this.readerRepository = readerRepository;
    }

    public List<ReaderDto> getReaders() {
        List<ReaderEntity> readers = readerRepository.findAll();
        List<ReaderDto> readersDto = new ArrayList<>();
        for (ReaderEntity reader: readers) {
            readersDto.add(new ReaderDto(reader.getId(), reader.getFirstName(), reader.getLastName()));
        }
        return readersDto;
    }

    public ReaderDto createReader(ReaderDto readerDto) {
        ReaderEntity reader = new ReaderEntity();
        reader.setFirstName(readerDto.firstName());
        reader.setLastName(readerDto.lastName());
        ReaderEntity readerEntity = readerRepository.save(reader);
        ReaderDto newReader = new ReaderDto(readerEntity.getId(),
                readerEntity.getFirstName(),
                readerEntity.getLastName());
        logger.info("Reader created: {}", newReader);
        return newReader;
    }

    public ReaderDto updateReader(ReaderDto readerDto) throws NotFoundException {
        ReaderEntity reader = readerRepository
                .findById(readerDto.id())
                .orElseThrow(() -> new NotFoundException("Reader not found"));
        reader.setFirstName(readerDto.firstName());
        reader.setLastName(readerDto.lastName());
        ReaderEntity readerEntity = readerRepository.save(reader);
        logger.info("Reader updated: {}", readerDto);
        return new ReaderDto(readerEntity.getId(), readerEntity.getFirstName(), readerEntity.getLastName());
    }

    public ReaderDto getMostReader(String startInterval, String endInterval) throws NotFoundException {
        List<ReaderEntity> readers = readerRepository.findAll();
        LocalDateTime startDateTime = LocalDateTime.parse(startInterval);
        LocalDateTime endDateTime = LocalDateTime.parse(endInterval);
        ReaderEntity reader = readers.stream()
                .max((reader1, reader2) -> {
                    int valueReader1 = filteredEvents(reader1.getEvents(), startDateTime, endDateTime).size();
                    int valueReader2 = filteredEvents(reader2.getEvents(), startDateTime, endDateTime).size();
                    return valueReader1 - valueReader2;
                }).orElseThrow(() -> new NotFoundException("Most reader not found"));
        return new ReaderDto(reader.getId(), reader.getFirstName(), reader.getLastName());
    }

    private Set<EventEntity> filteredEvents(Set<EventEntity> events,
                                            LocalDateTime startDateTime,
                                            LocalDateTime endDateTime) {
        return events.stream().filter(eventEntity -> {
            boolean isReturnedBook = eventEntity.getEventType().equals(EventType.RETURN_BOOK.name());
            boolean isAfter = eventEntity.getEventDatetime().isAfter(startDateTime);
            boolean isBefore = eventEntity.getEventDatetime().isBefore(endDateTime);
            return isReturnedBook && isAfter && isBefore;
        }).collect(Collectors.toSet());
    }
}

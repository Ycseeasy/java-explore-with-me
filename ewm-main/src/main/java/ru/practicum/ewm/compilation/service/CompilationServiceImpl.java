package ru.practicum.ewm.compilation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.compilation.dto.UpdateCompilationRequest;
import ru.practicum.ewm.compilation.mapper.CompilationMapper;
import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.compilation.repository.CompilationRepository;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.DuplicateNameException;
import ru.practicum.ewm.exception.NotFoundException;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;


    @Transactional
    @Override
    public List<CompilationDto> getCompilationsPublic(Boolean pinned, Integer from, Integer size) {

        int pageNumber = from / size;
        Pageable pageable = PageRequest.of(pageNumber, size);
        if (pinned != null) {
            return compilationRepository.findByPinnedIs(pinned, pageable).stream()
                    .map(CompilationMapper::toCompilationDto)
                    .collect(Collectors.toList());
        }

        return compilationRepository.findAll(pageable).stream()
                .map(CompilationMapper::toCompilationDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public CompilationDto getCompilationByIdPublic(Long compId) {
        Optional<Compilation> result = compilationRepository.findById(compId);
        if (result.isPresent()) {
            return CompilationMapper.toCompilationDto(result.get());
        } else {
            throw new NotFoundException("Compilation with ID " + compId + "not found");

        }
    }


    @Transactional
    @Override
    public CompilationDto addCompilationAdmin(NewCompilationDto newCompilationDto) {
        Set<Event> listEvent = new HashSet<>();
        if (newCompilationDto.getEvents() != null && !newCompilationDto.getEvents().isEmpty()) {
            listEvent = eventRepository.getEventsByIdIn(newCompilationDto.getEvents());
        }

        Compilation compilation = CompilationMapper.toCompilation(newCompilationDto, listEvent);
        CompilationDto compilationDto;
        try {
            compilationDto = CompilationMapper.toCompilationDto(compilationRepository.save(compilation));
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateNameException("Compilation name already exist");
        }
        return compilationDto;
    }


    @Transactional
    @Override
    public void deleteCompilationByIdAdmin(Long compId) {
        if (compilationRepository.findById(compId).isEmpty()) {
            throw new NotFoundException("Compilation with ID " + compId + "not found");
        }
        compilationRepository.removeCompilationById(compId);
    }


    @Transactional
    @Override
    public CompilationDto updateCompilationByIdAdmin(Long compId, UpdateCompilationRequest updateCompilationRequest) {
        Optional<Compilation> oldCompilation = compilationRepository.findById(compId);
        if (oldCompilation.isEmpty()) {
            throw new NotFoundException("Compilation with ID " + compId + "not found");
        } else {
            Set<Event> listEvent = new HashSet<>();
            if (updateCompilationRequest.getEvents() != null) {
                listEvent = eventRepository.getEventsByIdIn(updateCompilationRequest.getEvents());
            }
            Compilation compilation = CompilationMapper.toCompilation(updateCompilationRequest, listEvent);
            compilation.setTitle(updateCompilationRequest
                    .getTitle() == null ? oldCompilation.get().getTitle() : updateCompilationRequest.getTitle());
            compilation.setId(compId);
            return CompilationMapper.toCompilationDto(compilationRepository.save(compilation));
        }
    }
}
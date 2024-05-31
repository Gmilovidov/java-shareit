package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.DataNotFoundException;
import ru.practicum.shareit.item.dto.ItemDtoForRequest;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoRequestor;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ItemRequestMapper itemRequestMapper;
    private final ItemMapper itemMapper;

    @Override
    public ItemRequestDto createRequest(Long requestorId, ItemRequestDto itemRequestDto) {
        User user = userRepository.findById(requestorId)
                .orElseThrow(() -> new DataNotFoundException("Пользователь с id=" + requestorId + " не найден"));
        itemRequestDto.setCreated(LocalDateTime.now());
        ItemRequest itemRequest = itemRequestMapper.createRequestFromDto(itemRequestDto, user);
        return itemRequestMapper.getRequestDto(itemRequestRepository.save(itemRequest));
    }

    @Override
    public List<ItemRequestDtoRequestor> getRequests(Long requestorId) {
        userRepository.findById(requestorId)
                .orElseThrow(() -> new DataNotFoundException("Пользователь с id=" + requestorId + " не найден"));
        return itemRequestRepository.findAllByRequestorId(requestorId).stream()
                .map(itemRequest -> itemRequestMapper
                        .getRequestDtoForRequestor(itemRequest, getItemDtoListRequest(itemRequest)))
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestDtoRequestor> getRequestsByPage(Long userId, Integer start, Integer size) {
        userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("Пользователь с id=" + userId + " не найден"));
        Pageable pageable = PageRequest.of(start / size, size, Sort.by("created").descending());
        return itemRequestRepository.findAllByRequestorIdNot(userId, pageable).stream()
                .map(itemRequest -> itemRequestMapper.getRequestDtoForRequestor(itemRequest,
                        getItemDtoListRequest(itemRequest)))
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequestDtoRequestor getRequestById(Long userId, Long requestId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("Пользователь с id=" + userId + " не найден"));
        ItemRequest itemRequest = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new DataNotFoundException("Не найден запрос на предмет с id = " + requestId));
        return itemRequestMapper.getRequestDtoForRequestor(itemRequest, getItemDtoListRequest(itemRequest));
    }

    private List<ItemDtoForRequest> getItemDtoListRequest(ItemRequest itemRequest) {
        return itemRepository.findAllByRequestId(itemRequest.getId()).stream()
                .map(itemMapper::getItemDtoForRequest)
                .collect(Collectors.toList());
    }
}

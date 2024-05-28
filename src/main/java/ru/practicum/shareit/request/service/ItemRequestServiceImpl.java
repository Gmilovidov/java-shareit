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
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final UserService userService;
    private final ItemRepository itemRepository;
    private final ItemRequestMapper itemRequestMapper;
    private final ItemMapper itemMapper;
    @Override
    public ItemRequestDto createRequest(Long requestorId, ItemRequestDto itemRequestDto) {
        User user = userService.getUserByIdWithoutDto(requestorId);
        itemRequestDto.setCreated(LocalDateTime.now());
        ItemRequest itemRequest = itemRequestMapper.createRequestFromDto(itemRequestDto, user);
        return itemRequestMapper.getRequestDto(itemRequestRepository.save(itemRequest));
    }

    @Override
    public List<ItemRequestDtoRequestor> getRequests(Long requestorId) {
        userService.getUserByIdWithoutDto(requestorId);
        return itemRequestRepository.findAllByRequestorId(requestorId).stream()
                .map(itemRequest -> itemRequestMapper
                        .getRequestDtoForRequestor(itemRequest, getItemDtoListRequest(itemRequest)))
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestDtoRequestor> getRequestsByPage(Long userId, Integer start, Integer size) {
        userService.getUserByIdWithoutDto(userId);
        Pageable pageable = PageRequest.of(start / size, size, Sort.by("created").descending());
        return itemRequestRepository.findAllByRequestorIdNot(userId, pageable).stream()
                .map(itemRequest -> itemRequestMapper.getRequestDtoForRequestor(itemRequest,
                        getItemDtoListRequest(itemRequest)))
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequestDtoRequestor getRequestById(Long userId, Long requestId) {
        userService.getUserByIdWithoutDto(userId);
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

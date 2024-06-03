package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoRequestor;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto createRequest(Long requestorId, ItemRequestDto itemRequestDto);

    List<ItemRequestDtoRequestor> getRequests(Long requestorId);

    List<ItemRequestDtoRequestor> getRequestsByPage(Long userId, Integer start, Integer size);

    ItemRequestDtoRequestor getRequestById(Long userId, Long requestId);

}

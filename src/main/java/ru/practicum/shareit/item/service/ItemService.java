package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {

    ItemDto createItem(Long userId, ItemDto itemDto);

    List<ItemDto> getItems(Long userId);

    ItemDto getItemById(Long userId, Long id);

    ItemDto update(Long userId, Long id, ItemDto itemDto);

    List<ItemDto> getItemsByText(Long userId, String text);
}

package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {

    ItemDto createItem(int userId, ItemDto itemDto);

    List<ItemDto> getItems(int userId);

    ItemDto getItemById(int userId, int id);

    ItemDto update(int userId, int id, ItemDto itemDto);

    List<ItemDto> getItemsByText(int userId, String text);
}

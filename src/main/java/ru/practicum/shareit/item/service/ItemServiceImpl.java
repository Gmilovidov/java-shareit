package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.DataNotFoundException;
import ru.practicum.shareit.exceptions.WrongIdException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemStorage itemStorage;
    private final ItemMapper itemMapper;
    private final UserService userService;

    @Override
    public ItemDto createItem(int userId, ItemDto itemDto) {
        checkUserInData(userId);
        Item item = itemMapper.createItemFromDto(itemDto);
        return itemMapper.getItemDto(itemStorage.createItem(userId, item));
    }

    @Override
    public List<ItemDto> getItems(int userId) {
        checkUserInData(userId);
        return itemStorage.getItems(userId).stream()
                .map(itemMapper::getItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemDto getItemById(int userId, int id) {
        checkUserInData(userId);
        return itemMapper.getItemDto(itemStorage.getItemById(id)
                .orElseThrow(() -> new DataNotFoundException("Вещь с id=" + id + " не найдена")));
    }

    @Override
    public ItemDto update(int userId, int id, ItemDto itemDto) {
        checkUserInData(userId);
        Item item = Item.buildItem(itemStorage.getItemById(id)
                .orElseThrow(() -> new DataNotFoundException("Вещь с id=" + id + " не найдена")));
        if (item.getOwnerId() != userId) {
            throw new WrongIdException("У вещи с id=" + id + " владелец");
        }
        updateItemFromDto(item, itemDto);
        return itemMapper.getItemDto(itemStorage.update(userId, id, item));
    }

    @Override
    public List<ItemDto> getItemsByText(int userId, String text) {
        userService.getUserById(userId);
        if (text.isBlank()) {
            return List.of();
        }
        return itemStorage.getItemsByText(text).stream()
                .map(itemMapper::getItemDto)
                .collect(Collectors.toList());
    }

    private void checkUserInData(int userId) {
        userService.getUserById(userId);
    }

    private void updateItemFromDto(Item item, ItemDto itemDto) {
        if (itemDto.getName() != null && !itemDto.getName().isBlank()) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank()) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }
    }
}

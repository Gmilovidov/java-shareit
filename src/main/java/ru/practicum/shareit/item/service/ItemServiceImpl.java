package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.DataNotFoundException;
import ru.practicum.shareit.exceptions.WrongIdException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;
    private final UserService userService;

    @Override
    public ItemDto createItem(Long userId, ItemDto itemDto) {
        User user = userService.getUserByIdWithoutDto(userId);
        Item item = itemMapper.createItemFromDto(itemDto);
        item.setOwner(user);
        return itemMapper.getItemDto(itemRepository.save(item));
    }

    @Override
    public List<ItemDto> getItems(Long userId) {
        userService.getUserByIdWithoutDto(userId);
        return itemRepository.findAllByOwnerId(userId).stream()
                .map(itemMapper::getItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemDto getItemById(Long userId, Long id) {
        userService.getUserByIdWithoutDto(userId);
        return itemMapper.getItemDto(itemRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Вещь с id=" + id + " не найдена")));
    }

    @Override
    public ItemDto update(Long userId, Long id, ItemDto itemDto) {
        userService.getUserByIdWithoutDto(userId);
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Вещь с id=" + id + " не найдена"));
        if (!userId.equals(item.getOwner().getId())) {
            throw new WrongIdException("Вещь с id=" + id + " не принадлежит пользователю с id = " + userId);
        }
        updateItemFromDto(item, itemDto);
        return itemMapper.getItemDto(itemRepository.save(item));
    }

    @Override
    public List<ItemDto> getItemsByText(Long userId, String text) {
        userService.getUserById(userId);
        if (text.isBlank()) {
            return List.of();
        }
        return itemRepository.findItemByText(text).stream()
                .map(itemMapper::getItemDto)
                .collect(Collectors.toList());
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

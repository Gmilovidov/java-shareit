package ru.practicum.shareit.item.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.model.Item;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class InMemoryItemStorage implements ItemStorage {
    private final Map<Integer, Item> dataItems;
    private final Map<Integer, Map<Integer, Item>> itemIndex;
    private int id = 0;

    @Override
    public Item createItem(int userId, Item item) {
        item.setId(generatedId());
        item.setOwnerId(userId);
        dataItems.put(item.getId(), item);
        itemIndex.computeIfAbsent(userId, k -> new HashMap<>()).put(id, item);
        return item;
    }

    @Override
    public List<Item> getItems(int userId) {
        return new ArrayList<>(itemIndex.getOrDefault(userId, Map.of()).values());
    }

    @Override
    public Optional<Item> getItemById(int id) {
        return Optional.ofNullable(dataItems.get(id));
    }

    @Override
    public Item update(int userId, int id, Item item) {
        dataItems.put(id, item);
        itemIndex.get(userId).put(id, item);
        return item;
    }

    @Override
    public List<Item> getItemsByText(String text) {
        String textLC = text.toLowerCase();
        return dataItems.values().stream()
                .filter(item -> (item.getDescription().toLowerCase().contains(textLC)
                || item.getName().toLowerCase().contains(textLC))
                && item.getAvailable())
        .collect(Collectors.toList());
    }

    private int generatedId() {
        return ++id;
    }
}

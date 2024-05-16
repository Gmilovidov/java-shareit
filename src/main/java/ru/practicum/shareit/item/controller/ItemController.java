package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.validateGroup.Create;
import ru.practicum.shareit.validateGroup.Update;

import java.util.List;

import static ru.practicum.shareit.constants.CustomHeaders.USER_ID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    public ResponseEntity<ItemDto> createItem(@RequestHeader(USER_ID) Long userId, @Validated(Create.class) @RequestBody ItemDto itemDto) {
        return ResponseEntity.ok().body(itemService.createItem(userId, itemDto));
    }

    @GetMapping
    public ResponseEntity<List<ItemDto>> getItems(@RequestHeader(USER_ID) Long userId) {
        return ResponseEntity.ok().body(itemService.getItems(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemDto> getItemById(@RequestHeader(USER_ID) Long userId, @PathVariable Long id) {
        return ResponseEntity.ok().body(itemService.getItemById(userId, id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ItemDto> update(@RequestHeader(USER_ID) Long userId, @PathVariable Long id,
                                          @Validated(Update.class) @RequestBody ItemDto itemDto) {
        return ResponseEntity.ok().body(itemService.update(userId, id, itemDto));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ItemDto>> getItemsByText(@RequestHeader(USER_ID) Long userId,
                                        @RequestParam (value = "text") String text) {
        return ResponseEntity.ok().body(itemService.getItemsByText(userId, text));
    }
}

package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.validateGroup.Create;
import ru.practicum.shareit.validateGroup.Update;

import javax.validation.constraints.Min;
import java.util.List;

import static ru.practicum.shareit.constants.CustomHeaders.USER_ID;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    public ResponseEntity<ItemDto> createItem(@RequestHeader(USER_ID) Long userId,
                                              @Validated(Create.class) @RequestBody ItemDto itemDto) {
        return ResponseEntity.ok().body(itemService.createItem(userId, itemDto));
    }

    @GetMapping
    public ResponseEntity<List<ItemDto>> getItems(@RequestHeader(USER_ID) Long userId,
                                                  @RequestParam(value = "from", defaultValue = "0") @Min(0) Integer start,
                                                  @RequestParam(value = "size", defaultValue = "10") @Min(1) Integer size) {
        return ResponseEntity.ok().body(itemService.getItems(userId, start, size));
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
                                                        @RequestParam (value = "text") String text,
                                                        @RequestParam(value = "from", defaultValue = "0") @Min(0) Integer start,
                                                        @RequestParam(value = "size", defaultValue = "10") @Min(1) Integer size) {
        return ResponseEntity.ok().body(itemService.getItemsByText(userId, text, start, size));
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<CommentDto> createComment(@RequestHeader(USER_ID) Long userId,
                                                    @PathVariable Long itemId,
                                                    @Validated(Create.class) @RequestBody CommentDto commentDto) {
        return ResponseEntity.ok().body(itemService.createComment(userId, itemId, commentDto));
    }
}

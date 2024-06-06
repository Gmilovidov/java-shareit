package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.validateGroup.Create;
import ru.practicum.shareit.validateGroup.Update;

import javax.validation.constraints.Min;

import static ru.practicum.shareit.constants.CustomHeaders.USER_ID;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/items")
public class ItemController {
    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> createItem(@RequestHeader(USER_ID) Long userId,
                                              @Validated(Create.class) @RequestBody ItemDto itemDto) {
        return itemClient.createItem(userId, itemDto);
    }

    @GetMapping
    public ResponseEntity<Object> getItems(@RequestHeader(USER_ID) Long userId,
                                                  @RequestParam(value = "from", defaultValue = "0") @Min(0) Integer start,
                                                  @RequestParam(value = "size", defaultValue = "10") @Min(1) Integer size) {
        return itemClient.getItems(userId, start, size);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getItemById(@RequestHeader(USER_ID) Long userId, @PathVariable Long id) {
        return itemClient.getItemById(userId, id);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> update(@RequestHeader(USER_ID) Long userId, @PathVariable Long id,
                                          @Validated(Update.class) @RequestBody ItemDto itemDto) {
        return itemClient.update(userId, id, itemDto);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> getItemsByText(@RequestHeader(USER_ID) Long userId,
                                                        @RequestParam (value = "text") String text,
                                                        @RequestParam(value = "from", defaultValue = "0") @Min(0) Integer start,
                                                        @RequestParam(value = "size", defaultValue = "10") @Min(1) Integer size) {
        return itemClient.getItemsByText(userId, text, start, size);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> createComment(@RequestHeader(USER_ID) Long userId,
                                                    @PathVariable Long itemId,
                                                    @Validated(Create.class) @RequestBody CommentDto commentDto) {
        return itemClient.createComment(userId, itemId, commentDto);
    }
}

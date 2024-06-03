package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoRequestor;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.validateGroup.Create;

import javax.validation.constraints.Min;
import java.util.List;

import static ru.practicum.shareit.constants.CustomHeaders.USER_ID;

@RestController
@RequestMapping(path = "/requests")
@Validated
@RequiredArgsConstructor
public class ItemRequestController {
    private  final ItemRequestService itemRequestService;

    @PostMapping
    public ResponseEntity<ItemRequestDto> createRequest(@RequestHeader(USER_ID) Long userId,
                                                        @Validated(Create.class)
                                                        @RequestBody ItemRequestDto itemRequestDto) {
        return ResponseEntity.ok().body(itemRequestService.createRequest(userId, itemRequestDto));
    }

    @GetMapping
    public ResponseEntity<List<ItemRequestDtoRequestor>> getRequests(@RequestHeader(USER_ID) Long userId) {
        return ResponseEntity.ok().body(itemRequestService.getRequests(userId));
    }

    @GetMapping("/all")
    public ResponseEntity<List<ItemRequestDtoRequestor>> getRequestsByPage(@RequestHeader(USER_ID) Long userId,
                                                                           @RequestParam(value = "from",
                                                                                   defaultValue = "0")
                                                                           @Min(0) Integer start,
                                                                           @RequestParam(value = "size",
                                                                                   defaultValue = "10")
                                                                               @Min(1) Integer size) {
        return ResponseEntity.ok().body(itemRequestService.getRequestsByPage(userId, start, size));
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<ItemRequestDtoRequestor> getRequestById(@RequestHeader(USER_ID) Long userId,
                                                                  @PathVariable Long requestId) {
        return ResponseEntity.ok().body(itemRequestService.getRequestById(userId, requestId));
    }
}

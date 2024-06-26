package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDtoIn;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;

import static ru.practicum.shareit.constants.CustomHeaders.USER_ID;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/bookings")
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingDtoOut> create(@RequestHeader(USER_ID) Long bookerId,
                                               @RequestBody BookingDtoIn bookingDtoIn) {
        return ResponseEntity.ok().body(bookingService.create(bookerId, bookingDtoIn));
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<BookingDtoOut> updateStatus(@RequestHeader(USER_ID) Long ownerId,
                                                      @PathVariable Long bookingId,
                                                      @RequestParam(value = "approved") Boolean isApproved) {
        return ResponseEntity.ok().body(bookingService.updateStatus(ownerId, bookingId, isApproved));
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingDtoOut> getBooking(@RequestHeader(USER_ID) Long userId,
                                                    @PathVariable Long bookingId) {
        return ResponseEntity.ok().body(bookingService.getBooking(userId, bookingId));
    }

    @GetMapping
    public ResponseEntity<List<BookingDtoOut>> getAllBooker(@RequestHeader(USER_ID) Long userId,
                                                            @RequestParam(value = "state",
                                                                    defaultValue = "ALL") String state,
                                                            @RequestParam(value = "from", defaultValue = "0")
                                                                 Integer from,
                                                            @RequestParam(value = "size", defaultValue = "10")
                                                                 Integer size) {
        return ResponseEntity.ok().body(bookingService.getAllBooker(userId, state, from, size));
    }

    @GetMapping("/owner")
    public ResponseEntity<List<BookingDtoOut>> getAllOwnerItem(@RequestHeader(USER_ID) Long ownerId,
                                                               @RequestParam(defaultValue = "ALL") String state,
                                                               @RequestParam(value = "from", defaultValue = "0") Integer from,
                                                               @RequestParam(value = "size", defaultValue = "10") Integer size) {
        return ResponseEntity.ok().body(bookingService.getAllOwnerItem(ownerId, state, from, size));
    }
}

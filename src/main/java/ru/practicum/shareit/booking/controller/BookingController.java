package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDtoIn;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.service.BookingService;

import javax.validation.Valid;
import java.util.List;

import static ru.practicum.shareit.constants.CustomHeaders.USER_ID;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/bookings")
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingDtoOut> create(@RequestHeader(USER_ID) Long bookerId,
                                               @Valid @RequestBody BookingDtoIn bookingDtoIn) {
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
                                                                    defaultValue = "ALL") String state) {
        return ResponseEntity.ok().body(bookingService.getAllBooker(userId, state));
    }

    @GetMapping("/owner")
    public ResponseEntity<List<BookingDtoOut>> getAllOwnerItem(@RequestHeader(USER_ID) Long ownerId,
                                                               @RequestParam(defaultValue = "ALL") String state) {
        return ResponseEntity.ok().body(bookingService.getAllOwnerItem(ownerId, state));
    }
}

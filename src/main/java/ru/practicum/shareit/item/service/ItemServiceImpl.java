package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exceptions.AvailableException;
import ru.practicum.shareit.exceptions.DataNotFoundException;
import ru.practicum.shareit.exceptions.WrongIdException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;
    private final BookingMapper bookingMapper;
    private final CommentMapper commentMapper;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository itemRequestRepository;

    @Override
    public ItemDto createItem(Long userId, ItemDto itemDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("Пользователь с id=" + userId + " не найден"));
        ItemRequest itemRequest = itemDto.getRequestId() != null ?
                itemRequestRepository.findById(itemDto.getRequestId()).orElse(null) : null;
        Item item = itemMapper.createItemFromDto(itemDto, itemRequest);
        item.setOwner(user);
        return itemMapper.getItemDto(itemRepository.save(item), null, null, null);
    }

    @Override
    public List<ItemDto> getItems(Long userId, Integer start, Integer size) {
        userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("Пользователь с id=" + userId + " не найден"));
        Pageable pageable = PageRequest.of(start / size, size);
        return itemRepository.findAllByOwnerId(userId, pageable).stream()
                .map(item -> {
                    Booking lastBooking = findLastBooking(item.getId());
                    Booking nextBooking = findNextBooking(item.getId());
                    return itemMapper.getItemDto(item,
                            bookingMapper.getBookingDto(lastBooking),
                            bookingMapper.getBookingDto(nextBooking),
                            findComments(item.getId()));
                })
                .sorted(Comparator.comparingLong(ItemDto::getId))
                .collect(Collectors.toList());
    }

    @Override
    public ItemDto getItemById(Long userId, Long id) {
        userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("Пользователь с id=" + userId + " не найден"));
        Item item = getItemByIdWithoutDto(id);
        Booking lastBooking = findLastBooking(id);
        Booking nextBookings = findNextBooking(id);

        if (userId.equals(item.getOwner().getId())) {
           return itemMapper.getItemDto(item,
                   bookingMapper.getBookingDto(lastBooking),
                   bookingMapper.getBookingDto(nextBookings),
                   findComments(id));
        }

        return itemMapper.getItemDto(item,
                null,
                null,
                findComments(id));
    }

    @Override
    public ItemDto update(Long userId, Long id, ItemDto itemDto) {
        userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("Пользователь с id=" + userId + " не найден"));
        Item item = getItemByIdWithoutDto(id);
        if (!userId.equals(item.getOwner().getId())) {
            throw new WrongIdException("Вещь с id=" + id + " не принадлежит пользователю с id = " + userId);
        }
        item = itemMapper.updateItemFromDto(item, itemDto);
        return itemMapper.getItemDto(itemRepository.save(item),
                bookingMapper.getBookingDto(findLastBooking(id)),
                bookingMapper.getBookingDto(findNextBooking(id)),
                findComments(id));
    }

    @Override
    public List<ItemDto> getItemsByText(Long userId, String text, Integer start, Integer size) {
        userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("Пользователь с id=" + userId + " не найден"));
        if (text.isBlank()) {
            return List.of();
        }
        Pageable pageable = PageRequest.of(start / size, size);
        return itemRepository.findItemByText(text, pageable).stream()
                .filter(Item::getAvailable)
                .map(item -> itemMapper.getItemDto(item,
                        null,
                        null,
                        findComments(item.getId())))
                .collect(Collectors.toList());
    }

    @Override
    public Item getItemByIdWithoutDto(Long id) {
        return  itemRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Предмет с id=" + id + " не найден"));
    }

    @Override
    public CommentDto createComment(Long userId, Long itemId, CommentDto commentDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("Пользователь с id=" + userId + " не найден"));

        List<Booking> itemBookings = bookingRepository
                .findAllByItem_IdAndBooker_IdAndStatus(itemId, userId, BookingStatus.APPROVED)
                .stream()
                .filter(booking -> booking.getEndTime().isBefore(LocalDateTime.now()))
                .collect(Collectors.toList());
        if (itemBookings.isEmpty()) {
            throw new AvailableException("Пользователь не бронировал вещь с id = "
                    + userId + ", " + itemId + " соответствено");
        }

        Item item = getItemByIdWithoutDto(itemId);
        commentDto.setCreated(LocalDateTime.now());
        Comment comment = commentMapper.getCommentFromDto(commentDto, item, user);
        return commentMapper.getCommentDto(commentRepository.save(comment));
    }

    private Booking findLastBooking(Long itemId) {
        List<Booking> itemBookings = bookingRepository.findAllByItemId(itemId);
        return itemBookings.stream()
                .filter(booking -> booking.getStartTime().isBefore(LocalDateTime.now()))
                .filter(booking -> booking.getStatus().equals(BookingStatus.APPROVED))
                .max(Comparator.comparing(Booking::getEndTime))
                .orElse(null);

    }

    private Booking findNextBooking(Long itemId) {
        List<Booking> itemBookings =bookingRepository.findAllByItemId(itemId);
        return itemBookings.stream()
                .filter(booking -> booking.getStartTime().isAfter(LocalDateTime.now()))
                .filter(booking -> booking.getStatus().equals(BookingStatus.APPROVED))
                .min(Comparator.comparing(Booking::getStartTime))
                .orElse(null);
    }

    private List<CommentDto> findComments(Long itemId) {
        return commentRepository.findAllByItemId(itemId).stream()
                .map(commentMapper::getCommentDto)
                .collect(Collectors.toList());
    }
}


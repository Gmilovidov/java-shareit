package ru.practicum.shareit.item.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ItemMapper {

    @Mapping(target = "id", source = "item.id")
    ItemDto getItemDto(Item item, BookingDto lastBooking, BookingDto nextBooking, List<CommentDto> comments);

    @Mapping(target = "owner", ignore = true)
    Item createItemFromDto(ItemDto itemDto);

    @Mapping(target = "owner", ignore = true)
    void updateItemFromDto(@MappingTarget Item item, ItemDto itemDto);
}

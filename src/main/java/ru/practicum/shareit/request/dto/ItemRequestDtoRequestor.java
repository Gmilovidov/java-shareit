package ru.practicum.shareit.request.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.item.dto.ItemDtoForRequest;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
@Setter
public class ItemRequestDtoRequestor {
    private Long id;
    private String description;
    private LocalDateTime created;
    private List<ItemDtoForRequest> items;
}

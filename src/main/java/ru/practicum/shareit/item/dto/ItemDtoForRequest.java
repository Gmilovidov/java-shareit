package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class ItemDtoForRequest {
    private Long id;
    private String name;
    private String description;
    private Long requestId;
    private Boolean available;
}

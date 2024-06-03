package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
@EqualsAndHashCode
public class ItemDtoForRequest {
    private Long id;
    private String name;
    private String description;
    private Long requestId;
    private Boolean available;
}

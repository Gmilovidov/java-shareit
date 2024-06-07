package ru.practicum.shareit.request.dto;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@EqualsAndHashCode
public class ItemRequestDto {
    private Long id;
    private String description;
    private LocalDateTime created;
}

package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.validateGroup.Create;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@Builder
public class ItemDto {
    private int id;
    @NotBlank(groups = Create.class)
    private String name;
    @NotBlank(groups = Create.class)
    private String description;
    @NotNull(groups = Create.class)
    private Boolean available;
}

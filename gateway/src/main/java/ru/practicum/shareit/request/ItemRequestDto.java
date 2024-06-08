package ru.practicum.shareit.request;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.validateGroup.Create;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode
@Builder
public class ItemRequestDto {
    private Long id;
    @NotNull(groups = Create.class)
    private String description;
    private LocalDateTime created;
}
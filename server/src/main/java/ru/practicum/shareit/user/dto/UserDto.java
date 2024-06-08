package ru.practicum.shareit.user.dto;

import lombok.*;

@Getter
@Setter
@EqualsAndHashCode
@Builder
public class UserDto {
    private Long id;
    private String email;
    private String name;
}

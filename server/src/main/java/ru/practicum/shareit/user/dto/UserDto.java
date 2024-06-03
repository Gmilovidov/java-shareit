package ru.practicum.shareit.user.dto;

import lombok.*;
import ru.practicum.shareit.validateGroup.Create;
import ru.practicum.shareit.validateGroup.Update;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Getter
@Setter
@EqualsAndHashCode
@Builder
public class UserDto {
    private Long id;
    @Email(groups = {Create.class, Update.class})
    @NotBlank(groups = Create.class)
    private String email;
    @NotBlank(groups = Create.class)
    private String name;
}

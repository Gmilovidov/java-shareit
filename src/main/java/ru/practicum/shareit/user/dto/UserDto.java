package ru.practicum.shareit.user.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.validateGroup.Create;
import ru.practicum.shareit.validateGroup.Update;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Getter
@Setter
@Builder
public class UserDto {
    private int id;
    @Email(groups = {Create.class, Update.class})
    @NotBlank(groups = Create.class)
    private String email;
    @NotBlank(groups = Create.class)
    private String name;
}

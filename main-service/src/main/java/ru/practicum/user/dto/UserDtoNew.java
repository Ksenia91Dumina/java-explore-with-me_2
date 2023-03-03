package ru.practicum.user.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
public class UserDtoNew {

    @NotBlank
    private String name;

    @NotBlank
    @Email
    private String email;
}

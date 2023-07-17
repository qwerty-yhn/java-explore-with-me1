package ru.practicum.users.dto;

import lombok.Builder;
import lombok.Value;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Value
@Builder
public class NewUserRequest { //Данные нового пользователя
    @Email
    @NotBlank
    @Size(min = 6, max = 254)
    String email;
    @NotBlank
    @Size(min = 2, max = 250)
    String name;
}

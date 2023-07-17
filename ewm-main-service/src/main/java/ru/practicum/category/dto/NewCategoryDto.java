package ru.practicum.category.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;


@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NewCategoryDto {
    @Size(max = 50)
    @NotBlank
    private String name;
}

package ru.practicum.category.dto;

import lombok.Builder;
import lombok.Value;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Value
@Builder
public class CategoryDto {
    Long id;
    @Size(max = 50)
    @NotBlank
    String name;

    public CategoryDto(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}

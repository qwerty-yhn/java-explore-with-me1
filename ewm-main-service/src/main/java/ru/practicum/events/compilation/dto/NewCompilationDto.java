package ru.practicum.events.compilation.dto;

import lombok.Builder;
import lombok.Value;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

@Value
@Builder
public class NewCompilationDto {
    List<Long> events;
    boolean pinned;
    @Size(max = 50)
    @NotBlank
    String title;
}

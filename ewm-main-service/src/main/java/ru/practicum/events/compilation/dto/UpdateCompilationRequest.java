package ru.practicum.events.compilation.dto;

import lombok.Builder;
import lombok.Value;

import javax.validation.constraints.Size;
import java.util.List;

@Builder
@Value
public class UpdateCompilationRequest {
    List<Long> events;
    Boolean pinned;
    @Size(max = 50)
    String title;
}

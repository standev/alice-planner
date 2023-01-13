package com.alicetechnologies.planner.task.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
@Schema
public class TaskResponseDto {
    @JsonUnwrapped
    private Task task;

    private long startInterval;
    private long endInterval;
}

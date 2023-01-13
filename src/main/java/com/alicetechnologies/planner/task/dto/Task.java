package com.alicetechnologies.planner.task.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema
public class Task {

    @Schema(description = "unique task identifier")
    private String taskCode;
    private String operationName;
    private String elementName;
    @Schema(description = "the duration this task takes to complete, in time units")
    private int duration;

    private @Nullable Crew crew;
    private Set<Equipment> equipment;
    @ArraySchema(arraySchema = @Schema(description = "all task codes of tasks that need to be completed before this task can start"))
    @Builder.Default
    private Set<String> dependencies = Set.of();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema
    public static class Crew {
        @Schema(description = "the type of the crew this task needs")
        private String name;
        @Schema(description = "the number of crew members this task needs to be allocated to it for the whole task duration")
        private int assignment;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema
    static class Equipment {
        private String name;
        private int quantity;
    }
}

package com.alicetechnologies.planner.task;

import com.alicetechnologies.planner.CriticalPathEngine;
import com.alicetechnologies.planner.PlannerApplication;
import com.alicetechnologies.planner.task.dto.TaskEvaluated;
import com.alicetechnologies.planner.task.dto.TaskResponseDto;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = PlannerApplication.API_TASK, produces = MediaType.APPLICATION_JSON_VALUE)
public class TaskController {
    private final CriticalPathEngine criticalPathEngine;

    public TaskController(final CriticalPathEngine criticalPathEngine) {
        this.criticalPathEngine = criticalPathEngine;
    }

    @GetMapping
    public List<TaskResponseDto> getAllTasks() {
        return criticalPathEngine.getTasks()
            .stream()
            .map(TaskController::toDto)
            .collect(Collectors.toList());
    }

    private static TaskResponseDto toDto(final TaskEvaluated task) {
        return TaskResponseDto.builder()
            .task(task.getTask())
            .startInterval(task.getEarlyStart())
            .endInterval(task.getEarlyFinish())
            .build();
    }
}

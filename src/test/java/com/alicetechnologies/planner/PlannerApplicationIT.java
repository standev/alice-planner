package com.alicetechnologies.planner;

import com.alicetechnologies.planner.plan.PlanResponseDto;
import com.alicetechnologies.planner.plan.PlannerController;
import com.alicetechnologies.planner.task.TaskController;
import com.alicetechnologies.planner.task.dto.TaskResponseDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class PlannerApplicationIT {

    @Autowired
    PlannerController plannerController;

    @Autowired
    TaskController taskController;

    @Test
    void contextLoads() {
    }

    @Test
    void planner() {
        final PlanResponseDto plan = plannerController.getPlan();
        Assertions.assertAll(
            () -> assertEquals(1069, plan.getTotalDuration()),
            () -> assertEquals(242, plan.getMaxCrewMembers())
        );
    }

    @Test
    void tasksEvaluated() {
        final List<TaskResponseDto> tasks = taskController.getAllTasks();
        final TaskResponseDto first = tasks.stream()
            .findFirst()
            .orElseThrow();

        final long startInterval = first.getStartInterval();
        final int duration = first.getTask().getDuration();
        final long endInterval = first.getEndInterval();

        Assertions.assertAll(
            () -> assertEquals(429, startInterval),
            () -> assertEquals(445, endInterval),
            () -> assertEquals(16, duration),
            () -> assertEquals(duration, endInterval - startInterval) // sanity check
        );
    }

}

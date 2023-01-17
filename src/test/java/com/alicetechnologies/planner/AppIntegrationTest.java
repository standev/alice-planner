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
class AppIntegrationTest {

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
            () -> assertEquals(139, plan.getMaxCrewMembers())
        );
    }

    @Test
    void tasksEvaluated() {
        final List<TaskResponseDto> tasks = taskController.getAllTasks();
        assertEquals(1304, tasks.size());

        final TaskResponseDto first = tasks.get(0);

        final long firstStart = first.getStartInterval();
        final int firstDuration = first.getTask().getDuration();
        final long firstEnd = first.getEndInterval();

        Assertions.assertAll(
            () -> assertEquals("A1487806042", first.getTask().getTaskCode()),
            () -> assertEquals("Excavate", first.getTask().getOperationName()),
            () -> assertEquals("B1_A_Excavation", first.getTask().getElementName()),
            () -> assertEquals(0, firstStart),
            () -> assertEquals(16, firstEnd),
            () -> assertEquals(16, firstDuration),
            () -> assertEquals(firstDuration, firstEnd - firstStart) // sanity check
        );

        final TaskResponseDto last = tasks.get(tasks.size() - 1);
        final long lastStart = last.getStartInterval();
        final int lastDuration = last.getTask().getDuration();
        final long lastEnd = last.getEndInterval();

        Assertions.assertAll(
            () -> assertEquals("A1669049637", last.getTask().getTaskCode()),
            () -> assertEquals("Inspect & Sign-off", last.getTask().getOperationName()),
            () -> assertEquals("B1_C_L8_Interior", last.getTask().getElementName()),
            () -> assertEquals(1045, lastStart),
            () -> assertEquals(1069, lastEnd),
            () -> assertEquals(24, lastDuration),
            () -> assertEquals(lastDuration, lastEnd - lastStart) // sanity check
        );
    }

}

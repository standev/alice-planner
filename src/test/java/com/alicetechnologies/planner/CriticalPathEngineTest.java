package com.alicetechnologies.planner;

import com.alicetechnologies.planner.task.dto.Task;
import com.alicetechnologies.planner.task.dto.TaskEvaluated;
import com.alicetechnologies.planner.task.TaskRepository;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CriticalPathEngineTest {

    /**
     * C -> B -> A
     * D -> E
     */
    final List<Task> sourceTasks = List.of(
        task("B", "build walls", 20, "A"),
        task("A", "lay foundation", 5),
        task("C", "build roof", 4, "B"),
        task("D", "eat lunch", 3, "E"),
        task("E", "drink coffee", 2)
    );

    @Test
    void tinyDataset() {
        CriticalPathEngine engine = new CriticalPathEngine(sourceTasks);

        final String taskOrder = engine.getTasks().stream()
            .sorted(Comparator.comparingInt(TaskEvaluated::getEarlyStart))
            .map(TaskEvaluated::getTaskCode)
            .collect(Collectors.joining(", "));

        final String taskStarts = engine.getTasks().stream()
            .sorted(Comparator.comparingInt(TaskEvaluated::getEarlyStart))
            .map(task -> String.format("%s=%d", task.getTaskCode(), task.getEarlyStart()))
            .collect(Collectors.joining(", "));

        assertAll(
            () -> assertEquals(29, engine.getTotalDuration()),
            () -> assertEquals("C, D, E, B, A", taskOrder), // this doesn't look right, I'd expect the opposite order
            () -> assertEquals("C=0, D=0, E=3, B=4, A=24", taskStarts),
            () -> assertEquals(2, engine.getMaxCrewMembers()) // flow D -> E intersects with flow C -> B -> A in duration of task C
        );
    }

    @Test
    void largerDataset() {
        final List<Task> moreTasks = new TaskRepository("tasks.json").getTasks();
        CriticalPathEngine engine = new CriticalPathEngine(moreTasks);

        assertAll(
            () -> assertEquals(1069, engine.getTotalDuration()),
            () -> assertEquals(242, engine.getMaxCrewMembers())
        );
    }

    private Task task(String code, String name, int duration, String... dependencies) {
        return Task.builder()
            .taskCode(code)
            .operationName(name)
            .duration(duration)
            .crew(Task.Crew.builder()
                .name("team")
                .assignment(1)
                .build())
            .dependencies(Arrays.stream(dependencies).collect(Collectors.toSet()))
            .build();
    }
}

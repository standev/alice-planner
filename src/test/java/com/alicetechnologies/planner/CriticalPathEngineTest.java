package com.alicetechnologies.planner;

import com.alicetechnologies.planner.task.dto.Task;
import com.alicetechnologies.planner.task.dto.TaskEvaluated;
import com.alicetechnologies.planner.task.TaskRepository;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CriticalPathEngineTest {


    /**
     * dependency relationship is {@code C -> B -> A}
     * <p>
     * so expected order of execution is {@code A -> B -> C}
     */
    @Test
    void oneFlow() {
        final List<Task> sourceTasks = List.of(
            task("C", "build roof", 4, "B"),
            task("B", "build walls", 3, "A"),
            task("A", "lay foundation", 5)
        );
        CriticalPathEngine engine = new CriticalPathEngine(sourceTasks);

        final List<TaskEvaluated> tasks = engine.getTasks();
        final List<String> initialNodeCodes = engine.findInitialNodes(tasks).stream()
            .map(TaskEvaluated::getTaskCode)
            .collect(Collectors.toList());
        assertEquals(initialNodeCodes, List.of("A"));

        assertAll(
            () -> assertEquals(12, engine.getTotalDuration()),
            () -> assertEquals("A, B, C", sequence(tasks)),
            () -> assertEquals("A[0-5], B[5-8], C[8-12]", timedSequence(tasks)),
            () -> assertEquals(1, engine.getMaxCrewMembers()) // one flow with 1 member at any time
        );
    }

    /**
     * <ul>
     * Two dependency chains here:
     * <li>{@code C -> B -> A}</li>
     * <li>{@code D -> E}</li>
     * </ul>
     * <ul>
     * The expected order of execution is therefore the opposite:
     * <li>{@code A -> B -> C}</li>
     * <li>{@code E -> D}</li>
     * </ul>
     */
    @Test
    void twoTaskFlows() {
        //
        // D -> E
        final List<Task> sourceTasks = List.of(
            task("C", "build roof", 4, "B"),
            task("B", "build walls", 20, "A"),
            task("A", "lay foundation", 5),
            task("D", "drink coffee", 2, "E"),
            task("E", "eat lunch", 3)
        );
        CriticalPathEngine engine = new CriticalPathEngine(sourceTasks);

        final List<TaskEvaluated> tasks = engine.getTasks();

        assertAll(
            () -> assertEquals(29, engine.getTotalDuration()),
            () -> assertEquals("A, E, D, B, C", sequence(tasks)),
            () -> assertEquals("A[0-5], E[0-3], D[3-5], B[5-25], C[25-29]", timedSequence(tasks)),

            // flow E -> D intersects with flow A -> B -> C in duration of task A
            () -> assertEquals(2, engine.getMaxCrewMembers())
        );
    }

    @Test
    void largerDataset() {
        final List<Task> moreTasks = new TaskRepository("tasks.json").getTasks();
        CriticalPathEngine engine = new CriticalPathEngine(moreTasks);

        assertAll(
            () -> assertEquals(1069, engine.getTotalDuration()),
            () -> assertEquals(139, engine.getMaxCrewMembers())
        );
    }

    private static Task task(String code, String name, int duration, String... dependencies) {
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

    private static String sequence(final Collection<TaskEvaluated> tasks) {
        return tasks.stream()
            .map(TaskEvaluated::getTaskCode)
            .collect(Collectors.joining(", "));
    }

    private static String timedSequence(final Collection<TaskEvaluated> tasks) {
        return tasks.stream()
            .map(task -> String.format("%s[%d-%d]", task.getTaskCode(), task.getEarlyStart(), task.getEarlyFinish()))
            .collect(Collectors.joining(", "));
    }

}

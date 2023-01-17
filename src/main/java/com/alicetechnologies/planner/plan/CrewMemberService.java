package com.alicetechnologies.planner.plan;

import com.alicetechnologies.planner.task.dto.Task;
import com.alicetechnologies.planner.task.dto.TaskEvaluated;
import com.google.common.collect.Range;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Component
public class CrewMemberService {

    /**
     * Estimate how many crew members are needed at any given point of the project.
     *
     * @return max number of crew members needed at some point
     */
    public int getMaxCrewMembers(final Collection<TaskEvaluated> tasks) {
        // A[0-10], B[5-10] would result in points [0, 5, 10]
        final List<Integer> timePoints = getAllTimePoints(tasks);

        // [0, 5, 10] -> [[0, 5], [5, 10]]
        final List<Range<Integer>> ranges = createConsecutiveRanges(timePoints);

        final Map<Range<Integer>, Integer> crewMembers = new HashMap<>();

        tasks.forEach(task -> {
            final Range<Integer> taskRange = asRange(task);
            ranges.stream()
                .filter(taskRange::encloses)
                .forEach(range -> {
                    // aggregate total number of crew members needed simultaneously
                    final Integer currentMembers = crewMembers.getOrDefault(range, 0);
                    final int assignment = Optional.ofNullable(task.getTask().getCrew())
                        .map(Task.Crew::getAssignment)
                        .orElse(0);
                    crewMembers.put(range, currentMembers + assignment);
                });
        });

        return crewMembers.values().stream()
            .mapToInt(value -> value)
            .max().orElse(0);
    }

    private static List<Integer> getAllTimePoints(final Collection<TaskEvaluated> tasks) {
        return tasks.stream()
            .flatMap(task -> Stream.of(task.getEarlyStart(), task.getEarlyFinish()))
            .distinct()
            .sorted()
            .collect(Collectors.toList());
    }

    private static List<Range<Integer>> createConsecutiveRanges(final List<Integer> timePoints) {
        return IntStream.range(0, timePoints.size() - 1)
            .mapToObj(index -> {
                final Integer from = timePoints.get(index);
                final Integer to = timePoints.get(index + 1);
                return Range.closedOpen(from, to);
            })
            .collect(Collectors.toList());
    }

    private static Range<Integer> asRange(final TaskEvaluated task) {
        return Range.closedOpen(task.getEarlyStart(), task.getEarlyFinish());
    }

}

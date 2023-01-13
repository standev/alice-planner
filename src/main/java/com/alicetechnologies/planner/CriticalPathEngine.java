package com.alicetechnologies.planner;

import com.alicetechnologies.planner.task.dto.Task;
import com.alicetechnologies.planner.task.dto.TaskEvaluated;
import com.google.common.collect.Range;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Engine responsible for evaluating critical path, start/end intervals, total duration
 * and peak number of crew members required simultaneously
 * <p>
 * Based on implementation suggested at https://stackoverflow.com/a/3022314
 */
public class CriticalPathEngine {
    private List<TaskEvaluated> tasks;
    private Map<String, TaskEvaluated> taskMap;
    private int maxCost;

    public CriticalPathEngine(final Collection<Task> sourceTasks) {
        evaluateTasks(sourceTasks);
    }

    /**
     * Evaluate the tasks, calculating the critical path and suggested start/end intervals
     *
     * @param sourceTasks original dataset
     */
    public void evaluateTasks(final Collection<Task> sourceTasks) {
        tasks = sourceTasks.stream()
            .map(TaskEvaluated::new)
            .collect(Collectors.toList());
        taskMap = tasks.stream()
            .collect(Collectors.toMap(TaskEvaluated::getTaskCode, task -> task));
        tasks.forEach(this::loadDependencies);
        calculateCriticalPath();
    }

    public List<TaskEvaluated> getTasks() {
        return tasks;
    }

    public int getMaxCost() {
        return maxCost;
    }

    public int getMaxCrewMembers() {
        // find every single point of change within the total duration of planned tasks
        List<Integer> boundaries = tasks.stream()
            .flatMap(task -> Stream.of(task.getEarlyStart(), task.getEarlyFinish()))
            .distinct()
            .sorted()
            .collect(Collectors.toList());

        // create consecutive ranges based on the points of change
        Map<Range<Integer>, Integer> crewMembers = new HashMap<>();
        List<Range<Integer>> ranges = IntStream.range(0, boundaries.size() - 1)
            .mapToObj(index -> {
                final Integer from = boundaries.get(index);
                final Integer to = boundaries.get(index + 1);
                return Range.closedOpen(from, to);
            })
            .collect(Collectors.toList());

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

    /**
     * Load actual {@link TaskEvaluated} instances based on task codes listed as dependencies.
     */
    private void loadDependencies(final TaskEvaluated task) {
        final Set<String> dependentTaskCodes = task.getTask().getDependencies();
        final Set<TaskEvaluated> dependencies = dependentTaskCodes.stream()
            .map(taskMap::get)
            .collect(Collectors.toSet());

        task.setDependencies(dependencies);
    }

    private void calculateCriticalPath() {
        // tasks whose critical cost has been calculated
        Set<TaskEvaluated> completed = new HashSet<>();
        // tasks whose critical cost needs to be calculated
        Set<TaskEvaluated> remaining = new HashSet<>(tasks);

        // Backflow algorithm
        // while there are tasks whose critical cost isn't calculated.
        while (!remaining.isEmpty()) {
            boolean progress = false;

            // find a new task to calculate
            final Iterator<TaskEvaluated> it = remaining.iterator();
            while (it.hasNext()) {
                TaskEvaluated task = it.next();
                if (completed.containsAll(task.getDependencies())) {
                    // all dependencies calculated, critical cost is max dependency critical cost, plus our cost
                    final int maxCriticalCost = task.getDependencies().stream()
                        .mapToInt(TaskEvaluated::getCriticalCost)
                        .max().orElse(0);
                    task.setCriticalCost(maxCriticalCost + task.getCost());
                    // set task as calculated and remove
                    completed.add(task);
                    it.remove();
                    // note we are making progress
                    progress = true;
                }
            }
            // If we haven't made any progress then a cycle must exist in
            // the graph, and we won't be able to calculate the critical path
            if (!progress)
                throw new RuntimeException("Cyclic dependency, algorithm stopped!");
        }

        // get the cost
        setLatestForMaxCost();
        Set<TaskEvaluated> initialNodes = initials(tasks);
        calculateEarly(initialNodes);

        assert completed.size() == tasks.size();
    }

    private void calculateEarly(Set<TaskEvaluated> initials) {
        for (TaskEvaluated initial : initials) {
            initial.setEarlyStart(0);
            initial.setEarlyFinish(initial.getCost());
            setEarly(initial);
        }
    }

    private void setEarly(TaskEvaluated initial) {
        int completionTime = initial.getEarlyFinish();
        for (TaskEvaluated task : initial.getDependencies()) {
            if (completionTime >= task.getEarlyStart()) {
                task.setEarlyStart(completionTime);
                task.setEarlyFinish(completionTime + task.getCost());
            }
            setEarly(task);
        }
    }

    private Set<TaskEvaluated> initials(List<TaskEvaluated> tasks) {
        Set<TaskEvaluated> remaining = new HashSet<>(tasks);
        for (TaskEvaluated task : tasks) {
            for (TaskEvaluated dependency : task.getDependencies()) {
                remaining.remove(dependency);
            }
        }

        return remaining;
    }

    private void setLatestForMaxCost() {
        maxCost = tasks.stream()
            .mapToInt(TaskEvaluated::getCriticalCost)
            .max().orElse(0);

        System.out.println("Critical path length (cost): " + maxCost);
        tasks.forEach(task -> task.setLatestFor(maxCost));
    }

    private Range<Integer> asRange(final TaskEvaluated task) {
        return Range.closedOpen(task.getEarlyStart(), task.getEarlyFinish());
    }
}

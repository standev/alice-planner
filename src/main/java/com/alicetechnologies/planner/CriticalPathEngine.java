package com.alicetechnologies.planner;

import com.alicetechnologies.planner.task.dto.Task;
import com.alicetechnologies.planner.task.dto.TaskEvaluated;
import com.google.common.collect.Range;

import java.util.Collection;
import java.util.Comparator;
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

    /**
     * Internal task repository for quickly finding task by code
     */
    private Map<String, TaskEvaluated> taskMap;

    /**
     * Stateful field which is calculated after the tasks are loaded and processed.
     */
    private int maxCost = Integer.MIN_VALUE;

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

    /**
     * Estimate the total duration of the project. It is equal to max cost in the critical path.
     *
     * @return total duration / max cost of the project
     */
    public int getTotalDuration() {
        return maxCost;
    }


    /**
     * Load actual {@link TaskEvaluated} instances based on task codes listed as dependencies.
     *
     */
    private void loadDependencies(final TaskEvaluated task) {
        final Set<String> dependentTaskCodes = task.getTask().getDependencies();
        final Set<TaskEvaluated> dependencies = dependentTaskCodes.stream()
            .map(taskMap::get)
            .collect(Collectors.toSet());

        task.setDependencies(dependencies);
        for (TaskEvaluated dependency : dependencies) {
            dependency.getBlocked().add(task);
        }
    }

    /**
     * Estimate max cost of the project, also start/end intervals for the tasks.
     * Using <a href="https://en.wikipedia.org/wiki/Critical_path_method">Critical path method</a>
     */
    private void calculateCriticalPath() {
        // tasks whose critical cost has been calculated
        Set<TaskEvaluated> completed = new HashSet<>();
        // tasks whose critical cost still needs to be calculated
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

        maxCost = calculateMaxCost();
        setLatestIntervalFor(maxCost);

        Set<TaskEvaluated> initialNodes = findInitialNodes(tasks);
        calculateEarlyIntervals(initialNodes);

        assert completed.size() == tasks.size();

        tasks = sortByExecutionOrder(tasks);
    }

    /**
     * Find nodes (tasks) which are not blocked by any other task.
     */
    protected Set<TaskEvaluated> findInitialNodes(List<TaskEvaluated> tasks) {
        Set<TaskEvaluated> remaining = new HashSet<>(tasks);
        for (TaskEvaluated task : tasks) {
            for (TaskEvaluated blocked : task.getBlocked()) {
                remaining.remove(blocked);
            }
        }

        return remaining;
    }

    private void calculateEarlyIntervals(Set<TaskEvaluated> initials) {
        for (TaskEvaluated initial : initials) {
            initial.setEarlyStart(0);
            initial.setEarlyFinish(initial.getCost());
            setEarlyInterval(initial);
        }
    }

    private void setEarlyInterval(TaskEvaluated initial) {
        int completionTime = initial.getEarlyFinish();
        for (TaskEvaluated task : initial.getBlocked()) {
            if (completionTime >= task.getEarlyStart()) {
                task.setEarlyStart(completionTime);
                task.setEarlyFinish(completionTime + task.getCost());
            }
            setEarlyInterval(task);
        }
    }

    /**
     * Modify {@code latestStart} and {@code latestFinish} for every task based on the max cost.
     */
    private void setLatestIntervalFor(final int maxCost) {
        tasks.forEach(task -> task.setLatestIntervalFor(maxCost));
    }

    private int calculateMaxCost() {
        return tasks.stream()
            .mapToInt(TaskEvaluated::getCriticalCost)
            .max().orElse(0);
    }

    private List<TaskEvaluated> sortByExecutionOrder(final Collection<TaskEvaluated> tasks) {
        return tasks.stream()
            .sorted(Comparator.comparingInt(TaskEvaluated::getEarlyStart))
            .collect(Collectors.toList());
    }
}

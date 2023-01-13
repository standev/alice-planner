package com.alicetechnologies.planner.task.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashSet;
import java.util.Set;

@Data
public class TaskEvaluated {
    // the actual cost of the task
    private int cost;
    // the cost of the task along the critical path
    private int criticalCost;
    private int earlyStart;
    private int earlyFinish;
    private int latestStart;
    private int latestFinish;

    private Task task;

    /**
     * A list of tasks that need to be done before this one can start.
     * The current task is blocked by all these tasks.
     */
    private Set<TaskEvaluated> dependencies = new HashSet<>();

    /**
     * A list of tasks that cannot start until this one is done.
     * These tasks are blocked until the current one is finished.
     */
    @EqualsAndHashCode.Exclude
    private Set<TaskEvaluated> blocked = new HashSet<>();

    public TaskEvaluated(final Task task) {
        this.task = task;
        cost = task.getDuration();
    }

    public String getTaskCode() {
        return task.getTaskCode();
    }

    public void setLatestIntervalFor(final int maxCost) {
        latestStart = maxCost - criticalCost;
        latestFinish = latestStart + cost;
    }
}

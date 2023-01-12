package com.alicetechnologies.planner.task.dto;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class TaskEvaluated implements Comparable<TaskEvaluated> {
    // the actual cost of the task
    public int cost;
    // the cost of the task along the critical path
    public int criticalCost;
    // a name for the task for printing
    public String name;
    public int earlyStart;
    public int earlyFinish;
    public int latestStart;
    public int latestFinish;

    private Task task;

    private Set<TaskEvaluated> dependencies = new HashSet<>();

    public TaskEvaluated(final Task task) {
        this.task = task;
        cost = task.getDuration();
        name = String.join(":", task.getOperationName(), task.getElementName());
    }

    public String getTaskCode() {
        return task.getTaskCode();
    }

    public void setLatestFor(final int maxCost) {
        latestStart = maxCost - criticalCost;
        latestFinish = latestStart + cost;
    }

    // this relies on recursion which might be expensive with big dataset.
    // TODO for optimization, consider adding flattened list of all dependencies.
    public boolean isDependent(TaskEvaluated other) {
        // is other a direct dependency?
        if (dependencies.contains(other)) {
            return true;
        }

        // is other an indirect dependency
        for (TaskEvaluated dependency : dependencies) {
            if (dependency.isDependent(other)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int compareTo(final TaskEvaluated other) {
        // sort by cost
        if (other.criticalCost != this.criticalCost) {
            return this.criticalCost - other.criticalCost;
        }

        // using dependency as a tie breaker
        // note if a is dependent on b then
        // critical cost a must be >= critical cost of b
        if (this.isDependent(other)) {
            return -1;
        }

        if (other.isDependent(this)) {
            return 1;
        }

        return 0;
    }
}

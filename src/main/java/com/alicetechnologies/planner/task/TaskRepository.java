package com.alicetechnologies.planner.task;

import com.alicetechnologies.planner.task.dto.Task;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * Read-only repository based on provided JSON file. Configurable by `planner.input.file` property.
 */
@Service
public class TaskRepository {

    private static final TypeReference<List<Task>> TASK_LIST_TYPE = new TypeReference<>() {
    };

    private List<Task> tasks;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public TaskRepository(@Value("${planner.input.file:tasks.json}") final String inputFilePath) {
        loadTasks(inputFilePath);
    }

    public List<Task> getTasks() {
        return tasks;
    }

    /**
     * @param inputFilePath path of JSON file containing task list, must be available in `resources` directory
     */
    @SneakyThrows(IOException.class)
    public List<Task> loadTasks(final String inputFilePath) {
        this.tasks = objectMapper.readValue(getClass().getClassLoader().getResourceAsStream(inputFilePath), TASK_LIST_TYPE);
        return getTasks();
    }

}

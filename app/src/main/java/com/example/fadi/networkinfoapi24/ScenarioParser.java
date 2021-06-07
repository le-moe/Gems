package com.example.fadi.networkinfoapi24;

import com.example.fadi.networkinfoapi24.tasks.TaskType;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Parse a file and create a scenario.
 */
public class ScenarioParser {


    public Scenario parse(File file) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ParsedScenario parsedScenario = mapper.readValue(file, ParsedScenario.class);
        Scenario scenario = new Scenario();

        scenario.setRepeat(parsedScenario.repeat);

        if (parsedScenario.duration != null) {
            scenario.setDuration(parsedScenario.duration);
        }

        for (ParsedTask parsedTask : parsedScenario.tasks) {
            scenario.addTask(mapper.convertValue(parsedTask.task, parsedTask.type.getTaskClass()));
        }

        return scenario;
    }

    /**
     * Represents a task from the JSON file.
     */
    public static class ParsedTask {
        @JsonProperty("type")
        public TaskType type;

        @JsonProperty("task")
        public JsonNode task;
    }

    /**
     * Represents a scenario from the JSON file.
     */
    public static class ParsedScenario {
        @JsonProperty("repeat")
        public int repeat = 1;

        @JsonProperty("duration")
        public Duration duration;

        @JsonProperty("tasks")
        public List<ParsedTask> tasks;
    }
}

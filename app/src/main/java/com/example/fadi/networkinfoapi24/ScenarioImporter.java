package com.example.fadi.networkinfoapi24;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Import scenarios from files
 */
public class ScenarioImporter {
    public static final String SCENARIO_DIR = "/storage/emulated/0/Scenarios/";
    private static final String TAG = "ScenarioImporter";

    /**
     * Import all the scenarios available.
     *
     * @return The scenarios
     * @throws IOException if an issue happened when opening files
     */
    public static List<Scenario> importScenarios() throws IOException {
        List<Scenario> scenarios = new ArrayList<>();

        File scenarioDir = new File(SCENARIO_DIR);
        boolean wasCreated = scenarioDir.mkdirs();

        if (wasCreated)
            return scenarios;

        // TODO: Handle exceptions (and invalid formats)

        File[] files = scenarioDir.listFiles();

        if (files == null || files.length == 0)
            return scenarios;

        for (File file : files) {
            if (!file.isDirectory() && file.getName().endsWith(".json")) {
                Scenario scenario = new ScenarioParser().parse(file);
                scenario.setName(file.getName());
                scenarios.add(scenario);
            }
        }

        return scenarios;
    }

    /**
     * Import a scenario.
     *
     * @param fileName The file to open
     * @return The scenario
     * @throws IOException if an issue happened when opening the file.
     */
    public static Scenario importScenario(String fileName) throws IOException {
        Scenario scenario = new ScenarioParser().parse(new File(SCENARIO_DIR + fileName));
        scenario.setName(fileName);
        return scenario;
    }
}

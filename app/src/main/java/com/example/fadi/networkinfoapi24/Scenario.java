package com.example.fadi.networkinfoapi24;

import com.example.fadi.networkinfoapi24.tasks.AbstractTask;

import java.security.InvalidParameterException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * A scenario is a sequence of tasks to be executed.
 */
public class Scenario {
    private final List<AbstractTask> tasks = new ArrayList<>();
    private int repeat = 1;
    private Duration duration;

    private int phoneOrder;
    private int totalNumberOfPhones;

    private String name;
    private String directoryName = null;

    /**
     * Add a task in the scenario.
     *
     * @param task The task to be added.
     */
    public void addTask(AbstractTask task) {
        tasks.add(task);
    }

    public List<AbstractTask> getTasks() {
        return tasks;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name + " (" + getRepeat() + "x)";
    }

    public int getRepeat() {
        return repeat;
    }

    /**
     * Set the number of times the scenario has to be executed.
     *
     * @param repeat The number of times the scenario should be executed.
     */
    public void setRepeat(int repeat) {
        this.repeat = repeat;
    }


    public void setOrderAndTotal(int order, int total) {
        if (order <= 0 || total <= 0) {
            throw new InvalidParameterException("Order and total must be strictly positive");
        }
        if (order > total)
            throw new InvalidParameterException("Order should not be larger than total");

        totalNumberOfPhones = total;
        phoneOrder = order;
    }

    public int getTotalNumberOfPhones() {
        return totalNumberOfPhones;
    }

    public int getPhoneOrder() {
        return phoneOrder;
    }

    public String getDirectoryName() {
        if (directoryName == null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault());
            directoryName = sdf.format(new Date());
        }
        return directoryName;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }
}

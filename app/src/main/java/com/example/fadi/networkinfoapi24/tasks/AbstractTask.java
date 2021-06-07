package com.example.fadi.networkinfoapi24.tasks;

import android.content.Context;

import com.example.fadi.networkinfoapi24.Duration;
import com.example.fadi.networkinfoapi24.NetworkOperator;
import com.example.fadi.networkinfoapi24.NetworkTechnology;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents a base task in a Scenario. Provides context to its subclasses.
 */
@JsonIgnoreProperties(value = {"context"}, ignoreUnknown = true)
public abstract class AbstractTask {
    private Duration delay;
    private Context context;
    private NetworkOperator operator;
    private NetworkTechnology technology;

    public Duration getDelay() {
        return delay;
    }

    public void setDelay(Duration delay) {
        this.delay = delay;
    }

    @JsonIgnore
    public Context getContext() {
        return context;
    }

    @JsonIgnore
    public void setContext(Context context) {
        this.context = context;
    }

    public NetworkOperator getOperator() {
        return operator;
    }

    public void setOperator(NetworkOperator operator) {
        this.operator = operator;
    }

    public NetworkTechnology getTechnology() {
        return technology;
    }

    public void setTechnology(NetworkTechnology technology) {
        this.technology = technology;
    }
}

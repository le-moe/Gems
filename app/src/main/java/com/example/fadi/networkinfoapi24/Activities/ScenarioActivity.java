package com.example.fadi.networkinfoapi24.Activities;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.fadi.networkinfoapi24.R;
import com.example.fadi.networkinfoapi24.Scenario;
import com.example.fadi.networkinfoapi24.ScenarioImporter;
import com.example.fadi.networkinfoapi24.TaskIntentService;
import com.example.fadi.networkinfoapi24.Utilities;
import com.example.fadi.networkinfoapi24.tasks.AbstractTask;
import com.example.fadi.networkinfoapi24.tasks.IndividualSpeedTask;
import com.example.fadi.networkinfoapi24.tasks.StressTask;
import com.hypertrack.hyperlog.HyperLog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Activity that displays the list of scenarios and allows to start a new scenario.
 */
public class ScenarioActivity extends AppCompatActivity {

    private Scenario selectedScenario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scenario);

        final List<Scenario> scenarios = new ArrayList<>();

        // Try to import scenarios
        try {
            scenarios.addAll(ScenarioImporter.importScenarios());
        } catch (Exception e) {
            new AlertDialog.Builder(this)
                    .setTitle("Error while importing scenarios")
                    .setMessage(e.getLocalizedMessage())
                    .show();
        }


        // ListView
        ListView listView = findViewById(R.id.taskList);
        TextView noItemTextView = new TextView(this);
        noItemTextView.setText("No scenarios available.");
        listView.setEmptyView(noItemTextView);

        // Adapter
        final ArrayAdapter<AbstractTask> adapter =
                new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        listView.setAdapter(adapter);

        // Spinner
        if (!scenarios.isEmpty()) {
            Spinner spinner = findViewById(R.id.spinner);
            ArrayAdapter<Scenario> spinnerAdapter = new ArrayAdapter<>(
                    this, android.R.layout.simple_spinner_dropdown_item, scenarios);

            spinner.setAdapter(spinnerAdapter);

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    adapter.clear();
                    selectedScenario = scenarios.get(position);
                    adapter.addAll(selectedScenario.getTasks());
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }

        // Start Button
        Button startButton = findViewById(R.id.startButton);
        startButton.setOnClickListener(v -> showTimePicker());

    }

    private void showTimePicker() {
        if (selectedScenario == null) {
            new AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage("No scenario selected.")
                    .show();
            return;
        }

        Calendar currentTime = Calendar.getInstance();
        int hour = currentTime.get(Calendar.HOUR_OF_DAY);
        int minute = currentTime.get(Calendar.MINUTE);

        TimePickerDialog timePicker = new TimePickerDialog(
                this, (view, hourOfDay, minute1) ->
                startScenarioAt(hourOfDay, minute1), hour, minute, true);
        timePicker.setTitle("Start at");
        timePicker.show();
    }

    private void startScenarioAt(int hour, int minute) {
        try {
            initializeOrderAndTotal();
        } catch (Exception e) {
            new AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage(e.getMessage())
                    .show();
            return;
        }

        TaskIntentService.startAt(this, selectedScenario, hour, minute);

        Utilities.startNotification(
                this,
                String.format(Locale.getDefault(),
                        "Scenario %s will start at %02d:%02d", selectedScenario.toString(), hour, minute),
                selectedScenario.getTasks().size() + " tasks");
    }

    private void initializeOrderAndTotal() throws Exception {
        String phoneOrder = ((EditText) findViewById(R.id.phoneOrder)).getText().toString();
        String totalNumberOfPhones = ((EditText) findViewById(R.id.totalNumberOfPhones)).getText().toString();

        if (!phoneOrder.isEmpty() && !totalNumberOfPhones.isEmpty()) {
            int order = Integer.parseInt(phoneOrder);
            int total = Integer.parseInt(totalNumberOfPhones);

            selectedScenario.setOrderAndTotal(order, total);

            HyperLog.i("ScenarioActivity", "Order " + order + " Total " + total);
        } else if (selectedScenario.getTasks().stream().anyMatch(t ->
                t instanceof StressTask || t instanceof IndividualSpeedTask)) {
            throw new Exception("This scenario contains a stress task or an individual speed task, " +
                    "order and total number of phones cannot be empty.");
        }
    }
}

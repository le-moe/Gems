package com.example.fadi.networkinfoapi24.Activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.fadi.networkinfoapi24.R;
import com.hypertrack.hyperlog.HyperLog;

import java.util.ArrayList;
import java.util.List;

/**
 * An activity that displays logs from {@link HyperLog}.
 */
public class LogActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        // RecyclerView
        RecyclerView recyclerView = findViewById(R.id.logRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        LogAdapter adapter = new LogAdapter(fetchLogs(LogLevel.ALL));
        recyclerView.setAdapter(adapter);

        // Spinner
        Spinner spinner = findViewById(R.id.logSpinner);
        ArrayAdapter<LogLevel> spinnerAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, LogLevel.values());
        spinner.setAdapter(spinnerAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                recyclerView.swapAdapter(
                        new LogAdapter(fetchLogs(LogLevel.values()[position])), true
                );
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Clear Logs
        Button clearButton = findViewById(R.id.clearLogsButton);

        clearButton.setOnClickListener(v ->
                new AlertDialog.Builder(this)
                        .setTitle("Are you sure?")
                        .setMessage("Are you sure to clear ALL logs? This action is irreversible.")
                        .setPositiveButton("Yes", ((dialog, which) -> {
                            HyperLog.deleteLogs();
                            recyclerView.swapAdapter(new LogAdapter(new ArrayList<>()), true);
                        }))
                        .setNegativeButton("Cancel", null)
                        .show()
        );
    }

    private List<String> fetchLogs(LogLevel level) {
        List<String> logs = HyperLog.getDeviceLogsAsStringList(false);

        if (level != LogLevel.ALL)
            logs.removeIf(s -> !s.contains(level.name()));

        return logs;
    }

    /**
     * Filters for logs according to their levels.
     */
    private enum LogLevel {
        ALL,
        INFO,
        WARN,
        ERROR
    }

    /**
     * An adapter for the {@link RecyclerView} that displays logs.
     */
    private static class LogAdapter extends RecyclerView.Adapter<LogAdapter.ViewHolder> {

        private final List<String> logs;

        LogAdapter(List<String> logs) {
            this.logs = logs;
        }

        @NonNull
        @Override
        public LogAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            TextView textView = (TextView) LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_1, parent, false);

            return new ViewHolder(textView);
        }

        @Override
        public void onBindViewHolder(@NonNull LogAdapter.ViewHolder holder, int position) {
            holder.textView.setText(logs.get(getItemCount() - 1 - position));
        }

        @Override
        public int getItemCount() {
            return logs.size();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            public final TextView textView;

            ViewHolder(TextView itemView) {
                super(itemView);
                textView = itemView;
            }
        }


    }
}

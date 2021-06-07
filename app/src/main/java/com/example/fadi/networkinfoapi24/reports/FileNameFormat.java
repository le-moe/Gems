package com.example.fadi.networkinfoapi24.reports;

import com.example.fadi.networkinfoapi24.tasks.TaskType;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Class used to generate a file name for reports from parameters
 * <p>
 * Call {@link #toString()} to get the formatted file name.
 */
public class FileNameFormat {
    private final TaskType type;
    private final Date date;
    private final String[] others;

    /**
     * @param type   The type of the task linked to the report.
     * @param date   The date of the report (when the measurements were done).
     * @param others Other parameters that will be appended at the end of the file name.
     */
    public FileNameFormat(TaskType type, Date date, String... others) {
        this.type = type;
        this.date = date;
        this.others = others;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder(type.toString());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault());
        String dateString = sdf.format(date);
        s.append("_").append(dateString);

        for (String other : others) {
            s.append("_").append(other);
        }

        return s.toString();
    }
}

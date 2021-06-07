package com.example.fadi.networkinfoapi24;

import com.example.fadi.networkinfoapi24.reports.MeasurementReport;
import com.example.fadi.networkinfoapi24.reports.Report;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.hypertrack.hyperlog.HyperLog;

import java.io.File;
import java.io.IOException;

/**
 * Export a report to a JSON or CSV file.
 */
public class ReportExporter {
    public static final String TAG = "ReportExporter";
    public static final String BASE_DIR = "/storage/emulated/0/Reports/";
    private final String directoryName;

    public ReportExporter(String directoryName) {
        this.directoryName = directoryName;
    }

    public void export(Report report) {
        String dir = BASE_DIR + directoryName;
        new File(dir).mkdirs();

        try {
            if (report instanceof MeasurementReport) {
                MeasurementReport measurementReport = (MeasurementReport) report;
                exportMeasurementJson(measurementReport, dir);
                exportMeasurementCsv(measurementReport, dir);
            } else {
                exportJson(report, dir);
            }
            HyperLog.i(TAG, "Exported: " + report.fileNameFormat().toString());
        } catch (Exception e) {
            HyperLog.e(TAG, e.getMessage());
        }

    }

    private void exportJson(Report report, String dir) throws IOException {
        File file = new File(dir + "/" + report.fileNameFormat().toString() + ".json");
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(file, report);
    }

    private void exportMeasurementJson(MeasurementReport report, String dir) throws IOException {
        File file = new File(dir + "/" + report.fileNameFormat().toString() + ".json");
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(file, report.getMeasurements());
    }

    private void exportMeasurementCsv(MeasurementReport report, String dir) throws IOException {
        File file = new File(dir + "/" + report.fileNameFormat().toString() + ".csv");
        CsvMapper mapper = new CsvMapper();
        CsvSchema schema = mapper.schemaFor(report.getMeasurements().get(0).getClass()).withHeader();
        mapper.writer(schema).writeValue(file, report.getMeasurements());
    }
}

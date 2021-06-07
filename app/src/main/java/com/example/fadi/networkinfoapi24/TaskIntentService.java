package com.example.fadi.networkinfoapi24;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.example.fadi.networkinfoapi24.Activities.MainActivity;
import com.example.fadi.networkinfoapi24.reports.LocationReport;
import com.example.fadi.networkinfoapi24.reports.Report;
import com.example.fadi.networkinfoapi24.tasks.AbstractTask;
import com.example.fadi.networkinfoapi24.tasks.AsynchronousTask;
import com.example.fadi.networkinfoapi24.tasks.CallTask;
import com.example.fadi.networkinfoapi24.tasks.CellTask;
import com.example.fadi.networkinfoapi24.tasks.IndividualSpeedTask;
import com.example.fadi.networkinfoapi24.tasks.PeriodicTask;
import com.example.fadi.networkinfoapi24.tasks.SpeedTask;
import com.example.fadi.networkinfoapi24.tasks.StressTask;
import com.hypertrack.hyperlog.HyperLog;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This is the service that runs a scenario in the background.
 * <p>
 * Should be broken down into smaller classes.
 */
public class TaskIntentService extends IntentService {
    public static final String TAG = "TaskIntentService";
    private static final String SCENARIO_NAME = "SCENARIO_NAME";
    private static final String TOTAL_NUMBER_OF_PHONES = "TOTAL_NUMBER_OF_PHONES";
    private static final String PHONE_ORDER = "PHONE_ORDER";

    private static boolean phoneRegistered = false;

    private ReportExporter exporter;

    public TaskIntentService() {
        super("com.example.fadi.networkinfoapi24.TaskIntentService");
    }

    /**
     * Start a scenario at a give time of the day.
     * <p>
     * The scenario will start at the next occurrence of hour+minute. That will means it will
     * eventually start the next day.
     *
     * @param context  The application/activity context.
     * @param scenario The scenario that will be executed.
     * @param hour     The hour at which the scenario will be executed
     * @param minute   The minute at which the scenario will be executed
     */
    public static void startAt(Context context, Scenario scenario, int hour, int minute) {
        Intent intent = new Intent(context, TaskIntentService.class);
        intent.putExtra(SCENARIO_NAME, scenario.getName());
        intent.putExtra(TOTAL_NUMBER_OF_PHONES, scenario.getTotalNumberOfPhones());
        intent.putExtra(PHONE_ORDER, scenario.getPhoneOrder());

        PendingIntent pendingIntent = PendingIntent.getService(context, new Random().nextInt(), intent, 0);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        Calendar now = Calendar.getInstance();
        now.setTimeInMillis(System.currentTimeMillis());

        if (now.get(Calendar.HOUR_OF_DAY) == hour && now.get(Calendar.MINUTE) == minute) {
            // Immediately start the service
            context.startService(intent);
            return;
        }

        if (calendar.before(now))
            // That means it's due tomorrow
            calendar.add(Calendar.DAY_OF_MONTH, 1);

        // Set the alarm
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        assert alarmManager != null;
        alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                pendingIntent);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            String scenarioName = intent.getStringExtra(SCENARIO_NAME);
            int totalNumberOfPhones = intent.getIntExtra(TOTAL_NUMBER_OF_PHONES, 0);
            int phoneOrder = intent.getIntExtra(PHONE_ORDER, 0);

            try {
                PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);

                assert pm != null;

                // Acquire a WakeLock to make sur the cpu stay active.
                PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
                wakeLock.acquire(48 * 60 * 60 * 1000); // Add a timeout after 48h just to be sure.
                Scenario scenario = ScenarioImporter.importScenario(scenarioName);
                try {
                    scenario.setOrderAndTotal(phoneOrder, totalNumberOfPhones);
                } catch (Exception e) {
                    // That's okay
                }
                handleScenario(scenario);
                wakeLock.release();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        if (!phoneRegistered) {
            TelephonyManager tm = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
            tm.listen(CallTask.mainListener, PhoneStateListener.LISTEN_CALL_STATE);
            phoneRegistered = true;
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void startForegroundNotification(String title, String text) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getBaseContext(), MainActivity.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_action_name)
                .setContentTitle(title) // use something from something from
                .setContentText(text);

        startForeground(3030, builder.build());
    }

    /**
     * Handle a scenario, from start to end.
     * When this function ends, the scenario is over.
     *
     * @param scenario The scenario to be executed.
     */
    private void handleScenario(Scenario scenario) {
        startForegroundNotification("Scenario started", "");

        exporter = new ReportExporter(scenario.getDirectoryName());

        LocationProvider locationProvider = new LocationProvider(getApplicationContext());
        locationProvider.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                exporter.export(new LocationReport(location));
            } else {
                HyperLog.e(TAG, "Location is null");
            }
        });

        preProcess(scenario);

        NetworkConfig config = new NetworkConfig(getApplicationContext());

        int size = scenario.getTasks().size() * scenario.getRepeat();
        int i = 1;

        // Make sure mobile data is enabled
        Utilities.setDataEnabled(true);
        // And wifi is off
        ((WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE)).setWifiEnabled(false);


        for (int j = 0; j < scenario.getRepeat(); j++) {
            long startTime = System.currentTimeMillis();

            // Debug
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            HyperLog.i(TAG, "start time: " + sdf.format(new Date()));


            for (AbstractTask task : scenario.getTasks()) {
                task.setContext(getApplicationContext());

                handleTaskWithDelay(startTime, task);

                // It would be actually better to check if technology and operator are null.
                // If they are, just copy their value from NetworkConfig
                // It they are not, set their values to NetworkConfig.
                // The problem is that task are actually reused

                if (task instanceof CellTask) {
                    // If it's a CellTask then we change tech and operator to the given ones.
                    config.setOperator(task.getOperator());
                    config.setTechnology(task.getTechnology());

                } else {
                    task.setTechnology(config.getTechnology());
                    task.setOperator(config.getOperator());
                }

                HyperLog.i(TAG, "Starting task :" + task.toString());

                if (task instanceof PeriodicTask)
                    handlePeriodicTask((PeriodicTask) task, i, size);
                else if (task instanceof AsynchronousTask)
                    handleAsynchronousTask((AsynchronousTask) task, i, size);

                i++;
            }

            // This is the end of an iteration of a scenario.
            // If the scenario has a fixed duration we should wait,
            // but not if this is the last iteration.
            if (j != scenario.getRepeat() - 1)
                handleScenarioWithFixedDuration(startTime, scenario);
        }

        stopForeground(true);
        notifyScenarioEnd();

        HyperLog.i(TAG, "Scenario " + scenario.getName() + " ended");
    }


    /**
     * Pre-process {@link StressTask}s adequately using the order and total number of phones.
     *
     * @param scenario
     */
    private void preProcess(Scenario scenario) {
        int i = scenario.getPhoneOrder();
        int n = scenario.getTotalNumberOfPhones();

        if (i == 0 || n == 0) {
            // This means that these parameters were not set.
            return;
        }


        List<AbstractTask> tasks = scenario.getTasks();
        List<AbstractTask> shallowCopy = new ArrayList<>(tasks);

        for (AbstractTask task : shallowCopy) {
            if (task instanceof StressTask) {
                StressTask stressTask = ((StressTask) task);
                int baseDelay = 0;
                if (stressTask.getDelay() != null)
                    baseDelay = stressTask.getDelay().toMilliseconds();

                int pauseDuration = SpeedTask.PAUSE_DURATION;
                int totalDuration = stressTask.getDuration().toMilliseconds() + pauseDuration;

                List<StressTask> stressTasks = new ArrayList<>();

                for (int j = i - 1; j < n; j++) {
                    Duration delay = new Duration();
                    delay.setMilliseconds(baseDelay + totalDuration * j);
                    stressTasks.add(StressTask.fromStressTask(stressTask, delay));
                }

                // SetLast for the last task
                stressTasks.get(stressTasks.size() - 1).setLast(true);

                int index = tasks.indexOf(task);
                tasks.addAll(index, stressTasks);
                tasks.remove(task);

            } else if (task instanceof IndividualSpeedTask) {
                IndividualSpeedTask speedTask = ((IndividualSpeedTask) task);
                int baseDelay = 0;
                if (speedTask.getDelay() != null)
                    baseDelay = speedTask.getDelay().toMilliseconds();

                int pauseDuration = SpeedTask.PAUSE_DURATION;
                int totalDuration = speedTask.getDuration().toMilliseconds() + pauseDuration;

                Duration delay = new Duration();
                delay.setMilliseconds(baseDelay + totalDuration * (i - 1));
                task.setDelay(delay);
            }
        }
    }

    /**
     * Wait if a task has delay
     *
     * @param startTime the timestamp (ms) at which the scenario started
     * @param task      the task with delay
     */
    private void handleTaskWithDelay(long startTime, AbstractTask task) {
        if (task.getDelay() != null) {
            long timeElapsed = System.currentTimeMillis() - startTime;
            long delay = task.getDelay().toMilliseconds() - timeElapsed;

            if (delay <= 0) {
                // HURRY! YOU'RE LATE.
                HyperLog.w(TAG, "Task started with delay :" + (-delay) + "ms");
            } else {
                startForegroundNotification("Waiting for next task to start", task.toString());
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    // TODO: Avoid copy/paste code
    private void handleScenarioWithFixedDuration(long startTime, Scenario scenario) {
        if (scenario.getDuration() != null) {
            long timeElapsed = System.currentTimeMillis() - startTime;
            long delay = scenario.getDuration().toMilliseconds() - timeElapsed;

            if (delay <= 0) {
                // HURRY! YOU'RE LATE.
                HyperLog.w(TAG, "Scenario started with delay :" + (-delay) + "ms");
            } else {
                startForegroundNotification("Waiting for next iteration of scenario to start", scenario.getName());
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private void notifyProgress(String text, int taskNumber, int totalTask, int maxProgress, int currentProgress, boolean indeterminate) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getBaseContext(), MainActivity.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_action_name)
                .setTicker("Your Ticker") // use something from something from R.string
                .setContentTitle(String.format("Task %d/%d %s", taskNumber, totalTask, text))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setProgress(maxProgress, currentProgress, indeterminate);

        startForeground(3030, builder.build());
    }

    /**
     * Notify that the scenario ended by sending a push notification
     */
    private void notifyScenarioEnd() {
        String msg = "Over";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, MainActivity.CHANNEL_ID)
                .setContentTitle("Scenario ended")
                .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                .setContentText(msg)
                .setSmallIcon(R.drawable.ic_action_name)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManager notificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(2223, builder.build());
        }
    }

    /**
     * Execute a {@link AsynchronousTask}.
     *
     * @param task       The task to be executed.
     * @param taskNumber The number of this task.
     * @param totalTask  The number of tasks in the scenario.
     */
    private void handleAsynchronousTask(AsynchronousTask task, int taskNumber, int totalTask) {
        notifyProgress(task.toString(), taskNumber, totalTask, 0, 0, true);

        CountDownLatch doneSignal = new CountDownLatch(1);

        task.setCallBack(report -> {
            doneSignal.countDown();
            exportReport(report);
        });

        task.run();
        try {
            doneSignal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * Execute a {@link PeriodicTask}.
     *
     * @param task       The task to be executed.
     * @param taskNumber The number of this task.
     * @param totalTask  The number of tasks in the scenario.
     */
    private void handlePeriodicTask(PeriodicTask task, int taskNumber, int totalTask) {
        notifyProgress(task.toString(), taskNumber, totalTask, 0, 0, true);
        task.onStart();
        int time = 0;
        while (time < task.getDuration().toMilliseconds()) {
            task.onNewPeriod();
            notifyProgress(task.toString(), taskNumber, totalTask, task.getDuration().toMilliseconds(), time, false);
            time += task.getPeriod().toMilliseconds();
            try {
                Thread.sleep(task.getPeriod().toMilliseconds());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Report report = task.onStop();
        exportReport(report);
        notifyProgress(task.toString(), taskNumber, totalTask, task.getDuration().toMilliseconds(), task.getDuration().toMilliseconds(), false);
    }

    private void exportReport(Report report) {
        if (report == null)
            HyperLog.e(TAG, "Null report");
        if (exporter == null)
            HyperLog.e(TAG, "Null exporter");
        if (report != null && exporter != null) {
            exporter.export(report);
        }
    }
}

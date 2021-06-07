package com.example.fadi.networkinfoapi24.tasks;

import com.example.fadi.networkinfoapi24.reports.Report;

/**
 * Task that waits for a callback to end.
 */
public abstract class AsynchronousTask extends AbstractTask {
    private CallBack callBack;

    /**
     * This will be called when starting the tasks.
     */
    public abstract void run();

    /**
     * A subclass should call this function to notify the end of the task.
     */
    final void stop(Report report) {
        if (callBack != null)
            callBack.notifyTaskEnded(report);
    }

    /**
     * Set the callback that ends the task
     *
     * @param callBack The callback called when the task ends.
     */
    public void setCallBack(CallBack callBack) {
        this.callBack = callBack;
    }

    public interface CallBack {
        void notifyTaskEnded(Report report);
    }

}

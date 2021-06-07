package com.example.fadi.networkinfoapi24.tasks;

public enum TaskType {
    CELL,
    WIFI,
    CALL,
    SMS,
    SPEED,
    STRESS,
    INDIVIDUAL_SPEED,
    LOCATION;

    /**
     * Get the corresponding class to the task type.
     *
     * @return The {@link AbstractTask} corresponding to the type.
     */
    public Class<? extends AbstractTask> getTaskClass() {
        switch (this) {
            case CELL:
                return CellTask.class;
            case WIFI:
                return WifiTask.class;
            case CALL:
                return CallTask.class;
            case SMS:
                return SmsTask.class;
            case SPEED:
                return SpeedTask.class;
            case STRESS:
                return StressTask.class;
            case INDIVIDUAL_SPEED:
                return IndividualSpeedTask.class;
            default:
                throw new UnsupportedOperationException("This task is not supported");
        }
    }
}

package com.dawson.dawsonschedulerapi.common.classes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AbstractScheduleDetails {
    protected final int dayOfWeek;
    protected final int startTime;
    protected final int endTime;

    public AbstractScheduleDetails(AbstractScheduleDetails other) {
        this.dayOfWeek = other.dayOfWeek;
        this.startTime = other.startTime;
        this.endTime = other.endTime;
    }
}

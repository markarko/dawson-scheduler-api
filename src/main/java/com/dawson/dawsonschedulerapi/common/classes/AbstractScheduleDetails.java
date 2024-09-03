package com.dawson.dawsonschedulerapi.common.classes;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public abstract class AbstractScheduleDetails {
    protected int dayOfWeek;
    protected int startTime;
    protected int endTime;
}

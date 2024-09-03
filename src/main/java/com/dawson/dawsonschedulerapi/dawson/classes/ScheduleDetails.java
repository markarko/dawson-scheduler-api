package com.dawson.dawsonschedulerapi.dawson.classes;

import com.dawson.dawsonschedulerapi.common.classes.AbstractScheduleDetails;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ScheduleDetails extends AbstractScheduleDetails {
    private final String location;

    @Builder
    public ScheduleDetails(int dayOfWeek, int startTime, int endTime, String location) {
        super(dayOfWeek, startTime, endTime);
        this.location = location;
    }
}


package com.dawson.dawsonschedulerapi.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleFilter {
    private String startTime;
    private String endTime;
    private Set<String> excludedWeekDays;
}

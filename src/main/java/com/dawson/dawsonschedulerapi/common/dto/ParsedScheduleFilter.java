package com.dawson.dawsonschedulerapi.common.dto;

import java.util.Set;

public record ParsedScheduleFilter(int startTime, int endTime, Set<Integer> excludedWeekDays) {
    private static final int defaultStartTime = -1;
    private static final int defaultEndTime = -1;
    private static final Set<Integer> defaultExcludedWeekDays = Set.of();

    public ParsedScheduleFilter combineAndOverride(ParsedScheduleFilter override) {
        return new ParsedScheduleFilter(override.startTime != defaultStartTime ? override.startTime : this.startTime,
                                        override.endTime != defaultEndTime ? override.endTime : this.endTime,
                                        !override.excludedWeekDays.equals(defaultExcludedWeekDays) ? override.excludedWeekDays : this.excludedWeekDays);
    }

    public static ParsedScheduleFilter withDefaultValues() {
        return new ParsedScheduleFilter(defaultStartTime, defaultEndTime, defaultExcludedWeekDays);
    }
}

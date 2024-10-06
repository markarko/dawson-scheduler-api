package com.dawson.dawsonschedulerapi.dawson.dto;

import com.dawson.dawsonschedulerapi.common.dto.ScheduleFilter;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
public class GeneratedSchedulesBody {
    private Map<String, List<String>> selectedCourses;
    private ScheduleFilter generalFilters;
    private Map<String, ScheduleFilter> courseSpecificFilters;
}

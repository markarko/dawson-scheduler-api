package com.dawson.dawsonschedulerapi.common.classes;

import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class AbstractSection {
    protected final String section;
    protected final List<? extends AbstractScheduleDetails> schedules;

    public AbstractSection(String section, List<? extends AbstractScheduleDetails> schedules) {
        this.section = section;
        this.schedules = schedules;
    }

    public AbstractSection(AbstractSection other) {
        this.section = other.section;
        this.schedules = other.schedules.stream()
                .map(AbstractScheduleDetails::new)
                .collect(Collectors.toList());
    }
}

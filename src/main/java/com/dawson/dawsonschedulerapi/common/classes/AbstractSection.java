package com.dawson.dawsonschedulerapi.common.classes;

import lombok.Getter;

import java.util.List;

@Getter
public abstract class AbstractSection {
    protected String section;
    protected final List<? extends AbstractScheduleDetails> schedules;

    protected AbstractSection(String section, List<? extends AbstractScheduleDetails> schedules) {
        this.section = section;
        this.schedules = schedules;
    }
}

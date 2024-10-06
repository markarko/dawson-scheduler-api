package com.dawson.dawsonschedulerapi.dawson.classes;

import com.dawson.dawsonschedulerapi.common.classes.AbstractSection;
import lombok.*;

import java.util.List;

@Getter
public class Section extends AbstractSection {
    private final String teacher;

    @Builder
    public Section(String id, String teacher, List<? extends ScheduleDetails> scheduleDetails) {
        super(id, scheduleDetails);
        this.teacher = teacher;
    }
}

package com.dawson.dawsonschedulerapi.common.classes;

import lombok.*;

import java.util.List;

@Getter
public abstract class AbstractCourse {
    protected final String title;
    protected final List<? extends AbstractSection> sections;

    protected AbstractCourse(String title, List<? extends AbstractSection> sections) {
        this.title = title;
        this.sections = sections;
    }
}
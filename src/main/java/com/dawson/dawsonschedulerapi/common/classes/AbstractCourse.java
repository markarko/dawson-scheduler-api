package com.dawson.dawsonschedulerapi.common.classes;

import lombok.*;

import java.util.List;

@Getter
public class AbstractCourse {
    protected final String title;
    protected final List<? extends AbstractSection> sections;

    public AbstractCourse(String title, List<? extends AbstractSection> sections) {
        this.title = title;
        this.sections = sections;
    }
}
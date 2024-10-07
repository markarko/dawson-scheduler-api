package com.dawson.dawsonschedulerapi.dawson.classes;

import com.dawson.dawsonschedulerapi.common.classes.AbstractCourse;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class Course extends AbstractCourse {
    private final String courseNumber;
    private final String courseDescription;
    private final List<Section> sections;

    @Builder
    public Course(String title, String courseNumber, String courseDescription, List<Section> sections) {
        super(title, sections);
        this.sections = sections;
        this.courseNumber = courseNumber;
        this.courseDescription = courseDescription;
    }
}

package com.dawson.dawsonschedulerapi.dawson.classes;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CourseCache {
    private List<Course> courses;
    private long lastUpdated;

    public void setCourses(List<Course> courses) {
        this.courses = courses;
        this.lastUpdated = System.currentTimeMillis();
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public List<Course> getCourses() {
        return List.copyOf(courses);
    }
}

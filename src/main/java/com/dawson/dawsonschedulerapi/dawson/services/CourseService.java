package com.dawson.dawsonschedulerapi.dawson.services;

import com.dawson.dawsonschedulerapi.common.data.CourseDataProvider;
import com.dawson.dawsonschedulerapi.dawson.classes.Course;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CourseService {
    private final CourseDataProvider<Course> courseDataProvider;

    public CourseService(CourseDataProvider<Course> courseDataProvider) {
        this.courseDataProvider = courseDataProvider;
        this.courseDataProvider.initCache();
    }

    public List<Course> getByQuery(String query) {
        return courseDataProvider.getCourses().stream()
                .filter(course -> course.getTitle().contains(query) || course.getCourseNumber().contains(query))
                .collect(Collectors.toList());
    }

    public Optional<Course> getByCourseNumber(String courseNumber){
        return courseDataProvider.getCourses().stream()
                .filter(course -> course.getCourseNumber().equals(courseNumber))
                .findFirst();
    }
}

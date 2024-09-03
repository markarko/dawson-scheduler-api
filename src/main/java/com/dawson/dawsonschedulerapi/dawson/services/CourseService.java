package com.dawson.dawsonschedulerapi.dawson.services;

import com.dawson.dawsonschedulerapi.common.data.CourseDataProvider;
import com.dawson.dawsonschedulerapi.dawson.classes.Course;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CourseService {
    private final CourseDataProvider<Course> courseDataProvider;

    public CourseService(CourseDataProvider<Course> courseDataProvider) {
        this.courseDataProvider = courseDataProvider;
        this.courseDataProvider.initCache();
    }

    private List<Course> getCourses() {
        return courseDataProvider.getCourses();
    }

    public Optional<Course> getCourseByCourseNumber(String courseNumber){
        return courseDataProvider.getCourses().stream()
                .filter(course -> course.getCourseNumber().equals(courseNumber))
                .findFirst();
    }

    public List<Course> getCourseByPartialCourseNumber(String partialCourseNumber){
        return courseDataProvider.getCourses().stream()
                .filter(course -> course.getCourseNumber().contains(partialCourseNumber))
                .toList();
    }
}

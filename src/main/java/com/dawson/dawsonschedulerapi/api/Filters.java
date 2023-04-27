package com.dawson.dawsonschedulerapi.api;

import com.dawson.dawsonschedulerapi.entities.Course;
import com.dawson.dawsonschedulerapi.external.DawsonAPI;

import java.util.List;
import java.util.Optional;

public class Filters {

    public static Optional<Course> getCourseByCourseNumber(String courseNumber){
        List<Course> courses = DawsonAPI.getCourses();
        return courses.stream().filter(course -> course.getCourseNumber().equals(courseNumber)).findFirst();
    }

    public static List<Course> getCourseByPartialCourseNumber(String partialCourseNumber){
        List<Course> courses = DawsonAPI.getCourses();
        return courses.stream().filter(course -> course.getCourseNumber().contains(partialCourseNumber)).toList();
    }
}

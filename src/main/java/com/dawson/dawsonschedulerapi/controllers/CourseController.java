package com.dawson.dawsonschedulerapi.controllers;

import com.dawson.dawsonschedulerapi.api.CourseManager;
import com.dawson.dawsonschedulerapi.api.Filters;
import com.dawson.dawsonschedulerapi.api.ResponseHandler;
import com.dawson.dawsonschedulerapi.entities.Course;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/courses")
public class CourseController {
    @RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getCourse(@RequestParam("course-number") String courseNumber) {
        List<Course> courses = Filters.getCourseByPartialCourseNumber(courseNumber);
        if (courses.size() == 0){
            String error = "no courses found";
            HttpStatus status = HttpStatus.NOT_FOUND;
            return ResponseHandler.generateResponse(status, null, error);
        }
        HttpStatus status = HttpStatus.OK;
        return ResponseHandler.generateResponse(status, courses, null);
    }

    @RequestMapping(value = "/add", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> addChoseCourse(@RequestParam("course-number") String courseNumber) {
        Optional<Course> course = Filters.getCourseByCourseNumber(courseNumber);
        if (course.isEmpty()){
            String error = "this course doesn't exist";
            HttpStatus status = HttpStatus.NOT_FOUND;
            return ResponseHandler.generateResponse(status, null, error);
        }
        if (CourseManager.courseAlreadyChosen(course.get())){
            String error = "this course is already chosen";
            HttpStatus status = HttpStatus.CONFLICT;
            return ResponseHandler.generateResponse(status, null, error);
        }
        CourseManager.addCourse(course.get());
        HttpStatus status = HttpStatus.CREATED;
        return ResponseHandler.generateResponse(status, course, null);
    }
}

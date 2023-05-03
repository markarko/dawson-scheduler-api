package com.dawson.dawsonschedulerapi.controllers;

import com.dawson.dawsonschedulerapi.api.CourseManager;
import com.dawson.dawsonschedulerapi.api.Filters;
import com.dawson.dawsonschedulerapi.api.ResponseHandler;
import com.dawson.dawsonschedulerapi.entities.Course;
import com.dawson.dawsonschedulerapi.entities.Section;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
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
    public ResponseEntity<Object> addChosenCourse(@RequestParam("course-number") String courseNumber) {
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
        if (CourseManager.getChosenCourses().size() >= CourseManager.maxChosenCourses){
            String error = "you can only choose 8 courses at a time";
            HttpStatus status = HttpStatus.BAD_REQUEST;
            return ResponseHandler.generateResponse(status, null, error);
        }
        CourseManager.addCourse(course.get());
        HttpStatus status = HttpStatus.CREATED;
        return ResponseHandler.generateResponse(status, course, null);
    }

    @RequestMapping(value = "/chosen", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getChosenCourses() {
        List<Course> courses = CourseManager.getChosenCourses();
        HttpStatus status = HttpStatus.OK;
        return ResponseHandler.generateResponse(status, courses, null);
    }

    @RequestMapping(value = "/schedules", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getGeneratedSchedules() {
        List<List<Map.Entry<String, Section>>> schedules = CourseManager.getGeneratedSchedules();
        HttpStatus status = HttpStatus.OK;
        return ResponseHandler.generateResponse(status, schedules, null);
    }

    @RequestMapping(value = "/schedules/add", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> addGeneratedSchedule(@RequestBody List<Map.Entry<String, Section>> schedule) {
        // validate data (skip for now assuming it's correct from the client side)
        CourseManager.setChosenSchedule(schedule);
        HttpStatus status = HttpStatus.CREATED;
        return ResponseHandler.generateResponse(status, null, null);
    }
}

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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
}

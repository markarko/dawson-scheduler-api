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

    // change to post
    @RequestMapping(value = "/add", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> addChosenCourse(@RequestBody Map<String, List<Integer>> chosenCourseMap) {
        Optional<Map.Entry<String, List<Integer>>> chosenCourse = chosenCourseMap.entrySet().stream().findFirst();

        if (chosenCourse.isEmpty() || chosenCourse.get().getValue().size() == 0){
            String error = "you need to provide a course number and a list of sections";
            HttpStatus status = HttpStatus.BAD_REQUEST;
            return ResponseHandler.generateResponse(status, null, error);

        }

        Optional<Course> course = Filters.getCourseByCourseNumber(chosenCourse.get().getKey());

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

        List<Section> chosenSections = new ArrayList<>();
        List<Integer> availableSectionNumbers = course.get().getSections().stream().map(s -> s.getSection()).toList();
        List<Section> availableSections = course.get().getSections();

        for (Integer chosenSection : chosenCourse.get().getValue()){
            int sectionIndex = availableSectionNumbers.indexOf(chosenSection);
            if (sectionIndex == -1){
                String error = "one of the sections is not valid or does not exist";
                HttpStatus status = HttpStatus.BAD_REQUEST;
                return ResponseHandler.generateResponse(status, null, error);
            }
            chosenSections.add(availableSections.get(sectionIndex));
        }
        System.out.println(chosenSections);

        Map.Entry<String, List<Section>> courseToAdd = Map.entry(chosenCourse.get().getKey(), chosenSections);

        CourseManager.addCourse(courseToAdd);
        HttpStatus status = HttpStatus.CREATED;
        return ResponseHandler.generateResponse(status, null, null);
    }

    @RequestMapping(value = "/remove", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> removeChosenCourse(@RequestParam("course-number") String courseNumber) {
        Optional<Course> course = Filters.getCourseByCourseNumber(courseNumber);
        if (course.isEmpty()){
            String error = "this course doesn't exist";
            HttpStatus status = HttpStatus.NOT_FOUND;
            return ResponseHandler.generateResponse(status, null, error);
        }
        if (!CourseManager.courseAlreadyChosen(course.get())){
            String error = "this course is not chosen";
            HttpStatus status = HttpStatus.CONFLICT;
            return ResponseHandler.generateResponse(status, null, error);
        }
        CourseManager.removeCourse(course.get().getCourseNumber());
        HttpStatus status = HttpStatus.ACCEPTED;
        return ResponseHandler.generateResponse(status, course, null);
    }

    @RequestMapping(value = "/chosen", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getChosenCourses() {
        List<Course> courses = CourseManager.getOnlyCourses();
        HttpStatus status = HttpStatus.OK;
        return ResponseHandler.generateResponse(status, courses.size() == 0 ? null : courses, null);
    }
}

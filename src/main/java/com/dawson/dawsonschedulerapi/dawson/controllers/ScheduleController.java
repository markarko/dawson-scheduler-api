package com.dawson.dawsonschedulerapi.dawson.controllers;

import com.dawson.dawsonschedulerapi.common.services.ScheduleGeneratorService;
import com.dawson.dawsonschedulerapi.dawson.api.ResponseHandler;
import com.dawson.dawsonschedulerapi.dawson.classes.Course;
import com.dawson.dawsonschedulerapi.dawson.classes.Section;
import com.dawson.dawsonschedulerapi.dawson.services.CourseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/schedules")
public class ScheduleController {
    private final CourseService courseService;
    private final ScheduleGeneratorService scheduleGeneratorService;

    public ScheduleController(CourseService courseService, ScheduleGeneratorService scheduleGeneratorService) {
        this.courseService = courseService;
        this.scheduleGeneratorService = scheduleGeneratorService;
    }


    @RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
    public ResponseEntity<Object> getGeneratedSchedules(@RequestBody List<Map.Entry<String, List<String>>> chosenCoursesBody) {
        if (chosenCoursesBody == null || chosenCoursesBody.size() == 0) {
            String error = "no courses were provided";
            HttpStatus status = HttpStatus.BAD_REQUEST;
            return ResponseHandler.generateResponse(status, null, error);
        }
        if (chosenCoursesBody.size() > ScheduleGeneratorService.maxChosenCourses){
            String error = "you can only choose up to " + ScheduleGeneratorService.maxChosenCourses + " courses";
            HttpStatus status = HttpStatus.BAD_REQUEST;
            return ResponseHandler.generateResponse(status, null, error);
        }

        Map<String, List<Section>> chosenCourses = new HashMap<>();
        Map<String, List<Integer>> chosenCoursesParsed = new HashMap<>();

        for (Map.Entry<String, List<String>> chosenCourse : chosenCoursesBody) {
            String courseNumber = chosenCourse.getKey();
            List<String> sections = chosenCourse.getValue();
            List<Integer> sectionNumbers = new ArrayList<>();

            for (String section : sections) {
                try {
                    int sectionIntValue = Integer.parseInt(section);
                    sectionNumbers.add(sectionIntValue);
                } catch (NumberFormatException e) {
                    String error = "each section must be a number";
                    HttpStatus status = HttpStatus.BAD_REQUEST;
                    return ResponseHandler.generateResponse(status, null, error);
                }
            }

            chosenCoursesParsed.put(courseNumber, sectionNumbers);
        }

        List<String> courseNumbers = chosenCoursesBody.stream().map(c -> c.getKey()).toList();

        if ((new HashSet<>(courseNumbers)).size() < courseNumbers.size()){
            String error = "there are duplicate courses";
            HttpStatus status = HttpStatus.BAD_REQUEST;
            return ResponseHandler.generateResponse(status, null, error);
        }

        for (Map.Entry<String, List<Integer>> chosenCourse : chosenCoursesParsed.entrySet()){
            Optional<Course> course = courseService.getCourseByCourseNumber(chosenCourse.getKey());

            if (course.isEmpty()){
                String error = "course with course number " + chosenCourse.getKey() + " does not exist";
                HttpStatus status = HttpStatus.NOT_FOUND;
                return ResponseHandler.generateResponse(status, null, error);
            }

            // Check if all sections are correct

            List<String> availableSectionNumbers = course.get().getSections().stream().map(s -> s.getSection()).toList();
            List<Integer> chosenSectionNumbers = new ArrayList<>(chosenCourse.getValue());

            if (chosenSectionNumbers.get(0) == -1){
                chosenCourses.put(chosenCourse.getKey(), new ArrayList<>(course.get().getSections()));
                continue;
            }

            int chosenSectionsSize = chosenSectionNumbers.size();
            chosenSectionNumbers.stream().filter(s -> availableSectionNumbers.contains(s));

            if (chosenSectionsSize != chosenSectionNumbers.size()){
                String error = "course with course number " + chosenCourse.getKey() + " has invalid sections";
                HttpStatus status = HttpStatus.BAD_REQUEST;
                return ResponseHandler.generateResponse(status, null, error);
            }

            // After the data has been validated

            List<Section> validChosenSections = new ArrayList<>(course.get().getSections());
            validChosenSections = validChosenSections.stream().filter(s -> chosenSectionNumbers.contains(s.getSection())).toList();
            chosenCourses.put(chosenCourse.getKey(), validChosenSections);
        }


        List<List<Map.Entry<String, Section>>> schedules = scheduleGeneratorService.getGeneratedSchedules(chosenCourses);
        Iterator<List<Map.Entry<String, Section>>> iterator = schedules.iterator();
        while(iterator.hasNext()) {
            List<Map.Entry<String, Section>> schedule = iterator.next();
            if (schedule.size() == 0) {
                iterator.remove();
            }
        }

        HttpStatus status = HttpStatus.OK;
        return ResponseHandler.generateResponse(status, schedules.size() == 0 ? null : schedules, null);
    }

}


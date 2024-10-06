package com.dawson.dawsonschedulerapi.dawson.controllers;

import com.dawson.dawsonschedulerapi.common.classes.AbstractScheduleDetails;
import com.dawson.dawsonschedulerapi.common.classes.AbstractSection;
import com.dawson.dawsonschedulerapi.common.dto.ParsedScheduleFilter;
import com.dawson.dawsonschedulerapi.common.dto.ScheduleFilter;
import com.dawson.dawsonschedulerapi.common.services.ScheduleGeneratorService;
import com.dawson.dawsonschedulerapi.dawson.api.ResponseHandler;
import com.dawson.dawsonschedulerapi.dawson.classes.Course;
import com.dawson.dawsonschedulerapi.dawson.classes.Section;
import com.dawson.dawsonschedulerapi.dawson.dto.GeneratedSchedulesBody;
import com.dawson.dawsonschedulerapi.dawson.services.CourseService;
import com.dawson.dawsonschedulerapi.utils.TimeManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/schedules")
public class ScheduleController {
    private final CourseService courseService;
    private final ScheduleGeneratorService scheduleGeneratorService;
    private final TimeManager timeManager;

    public ScheduleController(CourseService courseService, ScheduleGeneratorService scheduleGeneratorService,
                              TimeManager timeManager) {
        this.courseService = courseService;
        this.scheduleGeneratorService = scheduleGeneratorService;
        this.timeManager = timeManager;
    }


    @RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
    public ResponseEntity<Object> getGeneratedSchedules(@RequestBody GeneratedSchedulesBody requestBody) {
        Map<String, List<String>> chosenCoursesSent = requestBody.getSelectedCourses();

        if (chosenCoursesSent == null || chosenCoursesSent.size() == 0) {
            String error = "No courses were provided";
            HttpStatus status = HttpStatus.BAD_REQUEST;
            return ResponseHandler.generateResponse(status, null, error);
        }
        if (chosenCoursesSent.size() > ScheduleGeneratorService.maxChosenCourses){
            String error = "You can only choose up to " + ScheduleGeneratorService.maxChosenCourses + " courses";
            HttpStatus status = HttpStatus.BAD_REQUEST;
            return ResponseHandler.generateResponse(status, null, error);
        }

        Map<String, List<AbstractSection>> chosenCourses = new HashMap<>();

        for (String courseNumber : chosenCoursesSent.keySet()) {
            Optional<Course> course = courseService.getByCourseNumber(courseNumber);

            if (course.isEmpty()) {
                String error = "The following course does not exist: " + courseNumber;
                HttpStatus status = HttpStatus.BAD_REQUEST;
                return ResponseHandler.generateResponse(status, null, error);
            }

            for (String sectionSent : chosenCoursesSent.get(courseNumber)) {
                // hardcoded
                if (sectionSent.equals("All")) {
                    chosenCourses.put(courseNumber, new ArrayList<>(course.get().getSections()));
                    break;
                }

                Optional<Section> existingSection = course.get().getSections().stream()
                        .filter(section -> section.getSection().equals(sectionSent))
                        .findFirst();

                if (existingSection.isEmpty()) {
                    String error = "The section " + sectionSent + " in course" + courseNumber + " does not exist";
                    HttpStatus status = HttpStatus.BAD_REQUEST;
                    return ResponseHandler.generateResponse(status, null, error);
                }

                chosenCourses.computeIfAbsent(courseNumber, k -> new ArrayList<>()).add(existingSection.get());
            }
        }

        Map<String, List<AbstractSection>> filteredChosenCourses = applyFilters(requestBody, chosenCourses);
        if (filteredChosenCourses.size() != chosenCourses.size()) {
            HttpStatus status = HttpStatus.NOT_FOUND;
            return ResponseHandler.generateResponse(status, null, "No schedules found for the chosen filters");
        }

        List<Map<String, AbstractSection>> schedules = scheduleGeneratorService.getGeneratedSchedules(filteredChosenCourses);
        schedules.removeIf(schedule -> schedule.size() == 0);

        HttpStatus status = schedules.size() == 0 ? HttpStatus.NOT_FOUND : HttpStatus.OK;
        return ResponseHandler.generateResponse(status, schedules.size() == 0 ? null : schedules, "No schedules found");
    }

    private Map<String, List<AbstractSection>> applyFilters(GeneratedSchedulesBody requestBody, Map<String, List<AbstractSection>> chosenCourses) {
        if (requestBody.getGeneralFilters() == null && requestBody.getCourseSpecificFilters() == null) {
            return chosenCourses;
        }

        ParsedScheduleFilter generalFilters = parseScheduleFilter(requestBody.getGeneralFilters());

        return chosenCourses.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> {
                    if (requestBody.getCourseSpecificFilters() == null) {
                        return applyFilters(entry.getValue(), generalFilters);
                    }
                    ParsedScheduleFilter courseSpecificFilters = parseScheduleFilter(requestBody.getCourseSpecificFilters().get(entry.getKey()));
                    ParsedScheduleFilter combinedFilters = generalFilters.combineAndOverride(courseSpecificFilters);

                    return applyFilters(entry.getValue(), combinedFilters);
                }))
                .entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry ->
                        requestBody.getCourseSpecificFilters() != null && requestBody.getCourseSpecificFilters().get(entry.getKey()) != null ?
                                applyFilters(entry.getValue(), parseScheduleFilter(requestBody.getCourseSpecificFilters().get(entry.getKey()))) :
                                entry.getValue()))
                .entrySet().stream()
                .filter(e -> !e.getValue().isEmpty())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private ParsedScheduleFilter parseScheduleFilter(ScheduleFilter scheduleFilter) {
        if (scheduleFilter == null) {
            return ParsedScheduleFilter.withDefaultValues();
        }

        int generalStartTime = timeManager.militaryTimeToMinutes(scheduleFilter.getStartTime());
        int generalEndTime = timeManager.militaryTimeToMinutes(scheduleFilter.getEndTime());
        Set<Integer> excludedWeekDays = scheduleFilter.getExcludedWeekDays() != null ?
                scheduleFilter.getExcludedWeekDays().stream()
                        .map(timeManager::weekDayToInt)
                        .filter(day -> day != -1)
                        .collect(Collectors.toSet()) :
                new HashSet<>();

        return new ParsedScheduleFilter(generalStartTime, generalEndTime, excludedWeekDays);
    }


    private List<AbstractSection> applyFilters(List<AbstractSection> sections, ParsedScheduleFilter scheduleFilter) {
        return sections.stream()
                .map(section -> {
                    List<AbstractScheduleDetails> validSchedules = section.getSchedules().stream()
                            .filter(schedule -> isValidSchedule(schedule, scheduleFilter.startTime(), scheduleFilter.endTime(), scheduleFilter.excludedWeekDays()))
                            .collect(Collectors.toList());

                    return new AbstractSection(section.getSection(), validSchedules);
                })
                .filter(section -> !section.getSchedules().isEmpty())
                .collect(Collectors.toList());
    }

    private boolean isValidSchedule(AbstractScheduleDetails schedule, int startTime, int endTime, Set<Integer> excludedWeekDays) {
        boolean validTime = (startTime == -1 || schedule.getStartTime() >= startTime) &&
                (endTime == -1 || schedule.getEndTime() <= endTime);

        boolean validDay = !excludedWeekDays.contains(schedule.getDayOfWeek());

        return validTime && validDay;
    }
}


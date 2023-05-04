package com.dawson.dawsonschedulerapi.controllers;

import com.dawson.dawsonschedulerapi.api.CourseManager;
import com.dawson.dawsonschedulerapi.api.ResponseHandler;
import com.dawson.dawsonschedulerapi.entities.Section;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/schedules")
public class ScheduleController {
    @RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getGeneratedSchedules() {
        List<List<Map.Entry<String, Section>>> schedules = CourseManager.getGeneratedSchedules();
        HttpStatus status = HttpStatus.OK;
        return ResponseHandler.generateResponse(status, schedules, null);
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> addGeneratedSchedule(@RequestBody List<Map.Entry<String, Section>> schedule) {
        // validate data (skip for now assuming it's correct from the client side)
        CourseManager.setChosenSchedule(schedule);
        HttpStatus status = HttpStatus.CREATED;
        return ResponseHandler.generateResponse(status, null, null);
    }

    @RequestMapping(value = "/get", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> addGeneratedSchedule() {
        List<Map.Entry<String, Section>> schedule = CourseManager.getChosenSchedule();
        if (schedule.size() == 0){
            HttpStatus status = HttpStatus.NOT_FOUND;
            String error = "no schedules were previously added";
            return ResponseHandler.generateResponse(status, null, error);
        }
        HttpStatus status = HttpStatus.OK;
        return ResponseHandler.generateResponse(status, schedule, null);
    }
}

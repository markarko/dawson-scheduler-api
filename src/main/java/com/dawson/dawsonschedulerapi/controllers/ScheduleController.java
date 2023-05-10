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

import java.util.Iterator;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/schedules")
public class ScheduleController {
    @RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getGeneratedSchedules() {
        List<List<Map.Entry<String, Section>>> schedules = CourseManager.getGeneratedSchedules();
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

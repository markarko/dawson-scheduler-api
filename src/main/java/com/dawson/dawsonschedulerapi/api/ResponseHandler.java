package com.dawson.dawsonschedulerapi.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

public class ResponseHandler {
    public static ResponseEntity<Object> generateResponse(HttpStatus status, Object responseObj, String error) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", status.value());
        if (error == null){
            map.put("data", responseObj);
        } else {
            map.put("error", error);
        }

        return new ResponseEntity(map,status);
    }
}
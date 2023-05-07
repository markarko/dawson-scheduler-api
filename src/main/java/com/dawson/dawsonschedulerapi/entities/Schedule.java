package com.dawson.dawsonschedulerapi.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Time;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Schedule {
    private int dayOfWeek;
    private int startTime;
    private int endTime;
    private String location;
}


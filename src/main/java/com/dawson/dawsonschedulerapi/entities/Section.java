package com.dawson.dawsonschedulerapi.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Section {
    private int section;
    private String teacher;
    private List<Schedule> schedules;
}

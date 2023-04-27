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
public class Course {
    private String courseNumber;
    private String courseTitle;
    private String courseDescription;
    private List<Section> sections;
}

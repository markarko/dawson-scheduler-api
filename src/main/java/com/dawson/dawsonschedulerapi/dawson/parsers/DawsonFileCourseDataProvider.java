package com.dawson.dawsonschedulerapi.dawson.parsers;

import com.dawson.dawsonschedulerapi.common.data.CourseDataProvider;
import com.dawson.dawsonschedulerapi.dawson.classes.Course;
import com.dawson.dawsonschedulerapi.dawson.classes.CourseCache;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Component
@Primary
public class DawsonFileCourseDataProvider implements CourseDataProvider<Course> {
    private final DawsonCourseParser parser;
    private final CourseCache cache;
    private final String path;

    public DawsonFileCourseDataProvider(DawsonCourseParser parser,
                                        CourseCache cache,
                                        @Value("C:\\Users\\marko\\Downloads\\timetable.txt") String path) {
        this.cache = cache;
        this.path = path;
        this.parser = parser;
    }

    public void initCache() {
        cache.setCourses(parser.parse(getRawData()));
    }

    private String getRawData() {
        Path filePath = Paths.get(path);
        try {
            return Files.readString(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Error reading file: " + filePath, e);
        }
    }

    @Override
    public List<Course> getCourses() {
        return cache.getCourses();
    }
}

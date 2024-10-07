package com.dawson.dawsonschedulerapi.dawson.parsers;

import com.dawson.dawsonschedulerapi.common.data.CourseDataProvider;
import com.dawson.dawsonschedulerapi.dawson.classes.Course;
import com.dawson.dawsonschedulerapi.dawson.classes.CourseCache;
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
    private final String path = System.getenv("COURSES_FILE_PATH");

    public DawsonFileCourseDataProvider(DawsonCourseParser parser,
                                        CourseCache cache) {
        this.cache = cache;
        this.parser = parser;
    }

    private String getRawData() {
        try {
            if (path == null) {
                System.out.println("Path for courses file is not specified");
                return null;
            }
            Path filePath = Paths.get(path);
            return Files.readString(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Error reading file with path: " + path, e);
        }
    }

    @Override
    public void initCache() {
        cache.setCourses(parser.parse(getRawData()));
    }

    @Override
    public List<Course> getCourses() {
        return cache.getCourses();
    }
}

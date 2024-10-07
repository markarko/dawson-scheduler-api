package com.dawson.dawsonschedulerapi.dawson;

import com.dawson.dawsonschedulerapi.common.data.CourseDataProvider;
import com.dawson.dawsonschedulerapi.dawson.classes.Course;
import com.dawson.dawsonschedulerapi.dawson.classes.CourseCache;
import com.dawson.dawsonschedulerapi.dawson.parsers.DawsonAzureCourseDataProvider;
import com.dawson.dawsonschedulerapi.dawson.parsers.DawsonCourseParser;
import com.dawson.dawsonschedulerapi.dawson.parsers.DawsonFileCourseDataProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CourseDataProviderConfig {
    private final String environment = System.getenv("ENVIRONMENT");
    private final DawsonCourseParser parser;
    private final CourseCache cache;

    public CourseDataProviderConfig(DawsonCourseParser parser, CourseCache cache) {
        this.parser = parser;
        this.cache = cache;
    }

    @Bean
    public CourseDataProvider<Course> getCourseDataProvider() {
        if (environment != null && environment.equals("PROD")) {
            return new DawsonAzureCourseDataProvider(parser, cache);
        }
        return new DawsonFileCourseDataProvider(parser, cache);
    }
}

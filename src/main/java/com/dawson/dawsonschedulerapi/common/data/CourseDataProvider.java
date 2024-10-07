package com.dawson.dawsonschedulerapi.common.data;

import com.dawson.dawsonschedulerapi.common.classes.AbstractCourse;

import java.util.List;

public interface CourseDataProvider<T extends AbstractCourse> {
     List<T> getCourses();
     void initCache();
}

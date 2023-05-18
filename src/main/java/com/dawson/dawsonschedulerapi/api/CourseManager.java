package com.dawson.dawsonschedulerapi.api;

import com.dawson.dawsonschedulerapi.entities.Course;
import com.dawson.dawsonschedulerapi.entities.Schedule;
import com.dawson.dawsonschedulerapi.entities.Section;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CourseManager {
    public static int numGeneratedSchedules = 0;
    public static int maxGeneratedSchedules = 100;

    public static int maxChosenCourses = 7;

    public static List<Course> getOnlyCourses(List<Map.Entry<String, List<Section>>> chosenCourses){
        return chosenCourses.stream().map(e -> e.getKey()).map(c -> Filters.getCourseByCourseNumber(c).get()).toList();
    }

    // Generate all possible schedules
    // Returns a list of generated schedules based on the chose courses
    public static List<List<Map.Entry<String, Section>>> getGeneratedSchedules(List<Map.Entry<String, List<Section>>> chosenCourses) {
        numGeneratedSchedules = 0;
        List<List<Map.Entry<String, Section>>> schedules = new ArrayList<>();
        generateSchedulesHelper(new ArrayList<>(), schedules, chosenCourses);
        return schedules;
    }

    // Recursive helper function to generate schedules
    private static void generateSchedulesHelper(List<Map.Entry<String, Section>> currentSections, List<List<Map.Entry<String, Section>>> schedules, List<Map.Entry<String, List<Section>>> chosenCourses) {
        if (numGeneratedSchedules >= maxGeneratedSchedules){
            return;
        }

        // If we have selected a section for each course, add the current selection to the list of schedules
        if (currentSections.size() == chosenCourses.size()) {
            schedules.add(new ArrayList<>(currentSections));
            numGeneratedSchedules++;
            return;
        }

        // Get the next course to consider
        Course currentCourse = getOnlyCourses(chosenCourses).get(currentSections.size());

        List<Section> currentCourseSections = chosenCourses.get(currentSections.size()).getValue();

        // Try each section in the current course
        for (Section section : currentCourseSections) {
            // Check if this section overlaps with any section already in the current selection
            boolean overlaps = false;

            List<Map.Entry<String, Section>> _currentSections = new ArrayList<>(currentSections);


            for (Map.Entry<String, Section> entry : _currentSections) {
                Section selectedSection = entry.getValue();
                for (Schedule schedule : section.getSchedules()) {
                    for (Schedule selectedSchedule : selectedSection.getSchedules()) {
                        if (schedulesHaveConflict(schedule, selectedSchedule) || sectionsFromSameCourse(entry.getKey(), currentCourse.getCourseNumber())) {
                            overlaps = true;
                            break;
                        }
                    }
                    if (overlaps) {
                        break;
                    }
                }
                if (overlaps) {
                    break;
                }
            }
            // If there is no overlap, add this section to the current selection and continue
            if (!overlaps) {
                currentSections.add(Map.entry(currentCourse.getCourseNumber(), section));
                generateSchedulesHelper(currentSections, schedules, chosenCourses);
                currentSections.remove(currentSections.size() - 1);
            }
        }
    }


    public static boolean sectionsFromSameCourse(String courseNumber1, String courseNumber2){
        return courseNumber1.equals(courseNumber2);
    }

    public static boolean schedulesHaveConflict(Schedule schedule1, Schedule schedule2) {
        // NOTE: Possibly change the time to int type since it these checks can be performed faster

        int start1 = schedule1.getStartTime();
        int end1 = schedule1.getEndTime();
        int start2 = schedule2.getStartTime();
        int end2 = schedule2.getEndTime();

        // If schedules aren't on the same day, no conflicts possible
        if (schedule1.getDayOfWeek() != schedule2.getDayOfWeek()){
            return false;
        }

        // Case 1: Schedule 1 starts before Schedule 2, and ends during Schedule 2
        if (start1 < start2 && end1 > start2 && (end1 < end2 || end1 == end2)) {
            return true;
        }

        // Case 2: Schedule 1 starts before Schedule 2, and ends after Schedule 2
        if ((start1 < start2 || start1 == start2) && end1 > end2) {
            return true;
        }

        // Case 3: Schedule 1 starts during Schedule 2, and ends during Schedule 2
        if ((start1 > start2 || start1 == start2) && (end1 < end2 || end1 == end2)) {
            return true;
        }

        // Case 4: Schedule 1 starts during Schedule 2, and ends after Schedule 2
        if ((start1 > start2 || start1 == start2) && start1 < end2 && end1 > end2) {
            return true;
        }

        // No conflict found
        return false;
    }
}

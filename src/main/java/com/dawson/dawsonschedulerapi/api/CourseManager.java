package com.dawson.dawsonschedulerapi.api;

import com.dawson.dawsonschedulerapi.entities.Course;
import com.dawson.dawsonschedulerapi.entities.Schedule;
import com.dawson.dawsonschedulerapi.entities.Section;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CourseManager {
    public static int numGeneratedSchedules = 0;
    public static int maxGeneratedSchedules = 500;
    private static List<Course> chosenCourses = new ArrayList<>();
    // Stores a list of generated schedules based on the chose courses
    // The map contains the course number (string) and the section chosen from the course
    // One map represent one schedule
    private static List<List<Map.Entry<String, Section>>> generatedSchedules = new ArrayList<>();

    // Generate all possible schedules
    public static List<List<Map.Entry<String, Section>>> generateSchedules() {
        numGeneratedSchedules = 0;
        List<List<Map.Entry<String, Section>>> schedules = new ArrayList<>();
        generateSchedulesHelper(new ArrayList<>(), schedules);
        return schedules;
    }

    // Recursive helper function to generate schedules
    private static void generateSchedulesHelper(List<Map.Entry<String, Section>> currentSections, List<List<Map.Entry<String, Section>>> schedules) {
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
        Course currentCourse = chosenCourses.get(currentSections.size());

        // Try each section in the current course
        for (Section section : currentCourse.getSections()) {
            // Check if this section overlaps with any section already in the current selection
            boolean overlaps = false;

            List<Map.Entry<String, Section>> _currentSections = new ArrayList<>();


            for (Map.Entry<String, Section> entry : _currentSections) {
                Section selectedSection = entry.getValue();
                for (Schedule schedule : section.getSchedules()) {
                    for (Schedule selectedSchedule : selectedSection.getSchedules()) {
                        if (schedulesHaveConflict(schedule, selectedSchedule) && sectionsFromSameCourse(entry.getKey(), currentCourse.getCourseNumber())) {
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
                generateSchedulesHelper(currentSections, schedules);
                currentSections.remove(currentSections.size() - 1);
            }
        }
    }


    public static boolean sectionsFromSameCourse(String courseNumber1, String courseNumber2){
        return courseNumber1.equals(courseNumber2);
    }
    public static void printCombinations(){
        for (List<Map.Entry<String, Section>> generatedSchedule : generatedSchedules){
            System.out.print("[ ");
            for (Map.Entry<String, Section> entry : generatedSchedule){
                System.out.print(entry.getKey() + " : " + entry.getValue().getSection());
                System.out.print(",");
            }
            System.out.print(" ]");
            System.out.println("");
        }
    }


    private static boolean canAddToCombination(List<Map.Entry<String, Section>> combination, Map.Entry<String, Section> entryToAdd) {
        // Check if the section belongs to a course already in the combination
        for (Map.Entry<String, Section> entry : combination){
            if (entry.getKey().equals(entryToAdd.getKey())){
                return false;
            }
        }

        return schedulesHaveConflict(combination, entryToAdd.getValue());
    }

    private static boolean schedulesHaveConflict(List<Map.Entry<String, Section>> combination, Section sectionToAdd) {
        for (Map.Entry<String, Section> entry : combination) {
            Section section = entry.getValue();
            for (Schedule schedule : section.getSchedules()){
                for (Schedule scheduleToAdd : sectionToAdd.getSchedules()){
                    if (schedulesHaveConflict(schedule, scheduleToAdd)){
                        return false;
                    }
                }
            }
        }

        return true;
    }


    public static boolean schedulesHaveConflict(Schedule schedule1, Schedule schedule2) {
        Time start1 = schedule1.getStartTime();
        Time end1 = schedule1.getEndTime();
        Time start2 = schedule2.getStartTime();
        Time end2 = schedule2.getEndTime();

        // If schedules aren't on the same day, no conflicts possible
        if (schedule1.getDayOfWeek() != schedule2.getDayOfWeek()){
            return false;
        }

        // Case 1: Schedule 1 starts before Schedule 2, and ends during Schedule 2
        if (start1.before(start2) && end1.after(start2) && end1.before(end2)) {
            return true;
        }

        // Case 2: Schedule 1 starts before Schedule 2, and ends after Schedule 2
        if (start1.before(start2) && end1.after(end2)) {
            return true;
        }

        // Case 3: Schedule 1 starts during Schedule 2, and ends during Schedule 2
        if (start1.after(start2) && end1.before(end2)) {
            return true;
        }

        // Case 4: Schedule 1 starts during Schedule 2, and ends after Schedule 2
        if (start1.after(start2) && start1.before(end2) && end1.after(end2)) {
            return true;
        }

        // Case 5: Schedule 1 starts and ends at the same time as Schedule 2
        if (start1.equals(start2) && end1.equals(end2)) {
            return true;
        }

        // No conflict found
        return false;
    }

}

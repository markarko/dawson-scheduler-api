package com.dawson.dawsonschedulerapi.common.services;

import com.dawson.dawsonschedulerapi.common.classes.AbstractScheduleDetails;
import com.dawson.dawsonschedulerapi.common.classes.AbstractSection;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ScheduleGeneratorService {
    public static int numGeneratedSchedules = 0;
    public static int maxGeneratedSchedules = 100;
    public static int maxChosenCourses = 7;

    // Generate all possible schedules
    // Returns a list of generated schedules based on the chose courses
    public List<Map<String, AbstractSection>> getGeneratedSchedules(Map<String, List<AbstractSection>> chosenCourses) {
        numGeneratedSchedules = 0;
        List<Map<String, AbstractSection>> schedules = new ArrayList<>();
        generateSchedulesHelper(new HashMap<>(), schedules, chosenCourses, new ArrayList<>(chosenCourses.keySet()));
        return schedules;
    }

    // Recursive helper function to generate schedules
    private void generateSchedulesHelper(Map<String, AbstractSection> currentSections,
                                         List<Map<String, AbstractSection>> schedules,
                                         Map<String, List<AbstractSection>> chosenCourses,
                                         List<String> chosenCourseKeys) {
        if (numGeneratedSchedules >= maxGeneratedSchedules) {
            return;
        }

        // If we have selected a section for each course, add the current selection to the list of schedules
        if (currentSections.size() == chosenCourses.size()) {
            schedules.add(new HashMap<>(currentSections));
            numGeneratedSchedules++;
            return;
        }

        // Get the next course to consider
        String currentCourseNumber = chosenCourseKeys.get(currentSections.size());
        List<AbstractSection> currentCourseSections = chosenCourses.get(currentCourseNumber);

        // Try each section in the current course
        for (AbstractSection section : currentCourseSections) {
            // Check if this section overlaps with any section already in the current selection
            boolean overlaps = false;

            for (Map.Entry<String, AbstractSection> entry : currentSections.entrySet()) {
                AbstractSection selectedSection = entry.getValue();
                for (AbstractScheduleDetails schedule : section.getSchedules()) {
                    for (AbstractScheduleDetails selectedSchedule : selectedSection.getSchedules()) {
                        if (schedulesHaveConflict(schedule, selectedSchedule) ||
                                sectionsFromSameCourse(entry.getKey(), currentCourseNumber)) {
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
                currentSections.put(currentCourseNumber, section);
                generateSchedulesHelper(currentSections, schedules, chosenCourses, chosenCourseKeys);
                currentSections.remove(currentCourseNumber); // Backtrack
            }
        }
    }


    public boolean sectionsFromSameCourse(String courseNumber1, String courseNumber2){
        return courseNumber1.equals(courseNumber2);
    }

    public boolean schedulesHaveConflict(AbstractScheduleDetails schedule1, AbstractScheduleDetails schedule2) {
        int start1 = schedule1.getStartTime();
        int end1 = schedule1.getEndTime();
        int start2 = schedule2.getStartTime();
        int end2 = schedule2.getEndTime();

        // If schedules aren't on the same day, no conflicts possible
        if (schedule1.getDayOfWeek() != schedule2.getDayOfWeek()){
            return false;
        }

        // Case 1: Schedule 1 starts before Schedule 2, and ends during Schedule 2
        if (start1 < start2 && end1 > start2 && end1 <= end2) {
            return true;
        }

        // Case 2: Schedule 1 starts before Schedule 2, and ends after Schedule 2
        if (start1 <= start2 && end1 > end2) {
            return true;
        }

        // Case 3: Schedule 1 starts during Schedule 2, and ends during Schedule 2
        if (start1 >= start2 && end1 <= end2) {
            return true;
        }

        // Case 4: Schedule 1 starts during Schedule 2, and ends after Schedule 2
        return start1 > start2 && start1 < end2;
    }
}
package com.dawson.dawsonschedulerapi.dawson.parsers;

import com.dawson.dawsonschedulerapi.dawson.classes.Course;
import com.dawson.dawsonschedulerapi.dawson.classes.ScheduleDetails;
import com.dawson.dawsonschedulerapi.dawson.classes.Section;
import com.dawson.dawsonschedulerapi.utils.TimeManager;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DawsonCourseParser {
    private final TimeManager timeManager;

    public DawsonCourseParser(TimeManager timeManager) {
        this.timeManager = timeManager;
    }

    public List<Course> parse(String data) {
        if (data == null) {
            return new ArrayList<>();
        }

        List<Course> courses = new ArrayList<>();

        Document doc = Jsoup.parse(data);
        Elements courseWraps = doc.select("div.course-list-table div.course-wrap");

        // Bug with the course 311-912-DW : no sections, no scheduleDetails

        //Courses
        for (Element courseWrap : courseWraps){
            Element courseNumberTitle = courseWrap.select("div.course-number-title").first();
            Element infoContainer = courseNumberTitle.select("div.info-container").first();

            Element courseTitle = infoContainer.select("div.ctitle").first();
            Element courseNumber = infoContainer.select("div.cnumber").first();
            Element description = null;
            Elements sectionDetails = courseWrap.select("ul.section-details");

            List<Section> sectionEntities = new ArrayList<Section>();
            if (courseNumber.text().equals("311-912-DW")) continue;
            //Sections
            for (Element sectionDetail : sectionDetails){

                Elements rows = sectionDetail.select("li.row");
                Element scheduleDetails = rows.last().selectFirst("div.col-md-10").selectFirst("table.schedule-details");
                if (scheduleDetails != null){
                    String potentialIntensive = scheduleDetails.selectFirst("tbody").select("tr").last().select("td").last().text();
                    if (potentialIntensive.equals("Intensive")) continue;
                }


                if (rows.get(rows.size()-1).text().contains("Intensive")) {
                    continue;
                }

                Element section;
                Element teacher;
                Elements schedules;

                if (rows.size() == 6) {
                    if (rows.get(0).selectFirst("label").text().equals("Section Title")) {
                        section = rows.get(1).select("div.col-md-10").first();
                        teacher = rows.get(2).select("div.col-md-10").first();
                        description = rows.get(3).select("div.col-md-10").first();
                        schedules = rows.get(5).select("div.col-md-10").first().select("table tbody tr");
                    } else if (rows.get(3).selectFirst("label").text().equals("Comment")) {
                        section = rows.get(0).select("div.col-md-10").first();
                        teacher = rows.get(1).select("div.col-md-10").first();
                        description = rows.get(2).select("div.col-md-10").first();
                        schedules = rows.get(5).select("div.col-md-10").first().select("table tbody tr");
                    } else {
                        System.out.println("Could not parse course " + courseNumber.text());
                        continue;
                    }
                } else if (rows.size() == 7){
                    section = rows.get(1).select("div.col-md-10").first();
                    teacher = rows.get(2).select("div.col-md-10").first();
                    description = rows.get(3).select("div.col-md-10").first();
                    schedules = rows.get(6).select("div.col-md-10").first().select("table tbody tr");
                } else if (rows.size() == 5){
                    section = rows.get(0).select("div.col-md-10").first();
                    teacher = rows.get(1).select("div.col-md-10").first();
                    description = rows.get(2).select("div.col-md-10").first();
                    schedules = rows.get(4).select("div.col-md-10").first().select("table tbody tr");
                } else {
                    System.out.println("Could not parse course " + courseNumber.text());
                    continue;
                }

                List<ScheduleDetails> scheduleDetailsEntities = new ArrayList<>();
                for (Element schedule : schedules){
                    Elements cells = schedule.select("td");
                    Element dayOfWeek = cells.get(0);
                    Element times = cells.get(1);

                    String[] timeSetArr = times.text().split("-");
                    String startTimeRaw = timeSetArr[0].trim();
                    String endTimeRaw = timeSetArr[1].trim();

                    int startTime = parseTimeToMinutes(startTimeRaw);
                    int endTime = parseTimeToMinutes(endTimeRaw);
                    int dayOfWeekInt = timeManager.weekDayToInt(dayOfWeek.text());
                    if (dayOfWeekInt == -1) {
                        continue;
                    }

                    Element location = cells.get(2);
                    ScheduleDetails scheduleDetailsEntity = ScheduleDetails.builder()
                            .dayOfWeek(dayOfWeekInt)
                            .startTime(startTime)
                            .endTime(endTime)
                            .location(location.text())
                            .build();

                    scheduleDetailsEntities.add(scheduleDetailsEntity);
                }
                Section sectionEntity = Section.builder()
                        .id(section.text())
                        .scheduleDetails(scheduleDetailsEntities)
                        .teacher(teacher.text())
                        .build();
                sectionEntities.add(sectionEntity);
            }

            //Intensive courses that have only 1 section
            if (description == null) {
                continue;
            }
            Course courseEntity = Course.builder()
                    .courseDescription(description.text())
                    .courseNumber(courseNumber.text())
                    .title(courseTitle.text())
                    .sections(sectionEntities)
                    .build();

            courses.add(courseEntity);
        }

        return courses;
    }



    private int parseTimeToMinutes(String time){
        String[] timeArr = time.split(" ");
        String timeOfDay = timeArr[0];
        String amOrPm = timeArr[1];

        String[] hoursAndMinutes = timeOfDay.split(":");
        int hours = Integer.parseInt(hoursAndMinutes[0]);
        int minutes = Integer.parseInt(hoursAndMinutes[1]);

        int pmOffsetTime = amOrPm.equals("PM") && hours != 12 ? 60 * 12 : 0;

        return hours * 60 + minutes + pmOffsetTime;
    }
}

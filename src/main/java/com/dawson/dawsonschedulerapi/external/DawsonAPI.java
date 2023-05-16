package com.dawson.dawsonschedulerapi.external;

import com.dawson.dawsonschedulerapi.entities.Course;
import com.dawson.dawsonschedulerapi.entities.Schedule;
import com.dawson.dawsonschedulerapi.entities.Section;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.NameValuePair;

import java.io.*;
import java.sql.Time;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class DawsonAPI {

    private static final String id = System.getenv("DAWSON_ID");
    private static final String password = System.getenv("DAWSON_PWD");
    // Cache that would hold the courses objects
    private static final Map<String, List<Course>> cache = new ConcurrentHashMap<>();
    private static final String coursesCacheKey = "courses";
    private static final long coursesCacheExpirationTime = 1000*3600*48;
    // To avoid making a request every time I load the program
    // Reads the html response from the file and returns a string representing the html
    public static String getDataFromFile(){
        File file = new File("C:\\Users\\marko\\Desktop\\response_new.txt");
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        StringBuilder stringBuilder = new StringBuilder();
        String line = null;
        String ls = System.getProperty("line.separator");
        while (true) {
            try {
                if ((line = reader.readLine()) == null) break;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            stringBuilder.append(line);
            stringBuilder.append(ls);
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        try {
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return stringBuilder.toString();
    }

    // Gets all courses objects
    // First checks if we have a cached result, and return it if we do
    public static List<Course> getCourses() {
        List<Course> cachedResult = cache.get(coursesCacheKey);
        if (cachedResult != null) {
            return cachedResult;
        }

        List<Course> result = ParseData(GetRawData(id, password));

        cache.put(coursesCacheKey, result);
        if (coursesCacheExpirationTime > 0) {
            scheduleCacheExpiration(coursesCacheKey, coursesCacheExpirationTime);
        }
        return result;
    }

    private static void scheduleCacheExpiration(String cacheKey, long cacheExpirationTime) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.schedule(() -> cache.remove(cacheKey), cacheExpirationTime, TimeUnit.MILLISECONDS);
    }
    public static List<Course> ParseData(String rawData){
        List<Course> courses = new ArrayList<>();

        Document doc = Jsoup.parse(rawData);
        Elements courseWraps = doc.select("div.course-list-table div.course-wrap");

        // Bug with the course 311-912-DW : no sections, no schedules

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

                Element section = null;
                Element teacher = null;
                Elements schedules = null;

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
                        System.out.println("Something went wrong");
                        continue;
                    }
                } else if (rows.size() == 7){
                    section = rows.get(1).select("div.col-md-10").first();
                    teacher = rows.get(2).select("div.col-md-10").first();
                    description = rows.get(3).select("div.col-md-10").first();
                    schedules = rows.get(6).select("div.col-md-10").first().select("table tbody tr");
                } else {
                    section = rows.get(0).select("div.col-md-10").first();
                    teacher = rows.get(1).select("div.col-md-10").first();
                    description = rows.get(2).select("div.col-md-10").first();
                    schedules = rows.get(4).select("div.col-md-10").first().select("table tbody tr");
                }



                List<Schedule> scheduleEntities = new ArrayList<>();
                for (Element schedule : schedules){
                    Elements cells = schedule.select("td");
                    Element dayOfWeek = cells.get(0);
                    Element times = cells.get(1);

                    String[] timeSetArr = times.text().split("-");
                    String startTimeRaw = timeSetArr[0].trim();
                    String endTimeRaw = timeSetArr[1].trim();

                    int startTime = parseTime(startTimeRaw);
                    int endTime = parseTime(endTimeRaw);

                    Element location = cells.get(2);
                    Schedule scheduleEntity = Schedule.builder()
                            .dayOfWeek(getIntValueOfWeekDay(dayOfWeek.text()))
                            .startTime(startTime)
                            .endTime(endTime)
                            .location(location.text())
                            .build();

                    scheduleEntities.add(scheduleEntity);
                }
                try {
                    Section sectionEntity = Section.builder()
                            .section(Integer.parseInt(section.text()))
                            .schedules(scheduleEntities)
                            .teacher(teacher.text())
                            .build();
                    sectionEntities.add(sectionEntity);
                } catch (NumberFormatException e) {
                    System.out.println("Can't convert");
                }
            }

            //Intensive courses that have only 1 section
            if (description == null) {
                continue;
            }
            Course courseEntity = Course.builder()
                    .courseDescription(description.text())
                    .courseNumber(courseNumber.text())
                    .courseTitle(courseTitle.text())
                    .sections(sectionEntities)
                    .build();

            courses.add(courseEntity);
        }

        return courses;
    }

    public static int getIntValueOfWeekDay(String dayOfWeek) {
        switch (dayOfWeek.toLowerCase()) {
            case "sunday":
                return 1;
            case "monday":
                return 2;
            case "tuesday":
                return 3;
            case "wednesday":
                return 4;
            case "thursday":
                return 5;
            case "friday":
                return 6;
            case "saturday":
                return 7;
            default:
                throw new IllegalArgumentException("The entered string is not a week day or is mistyped");
        }
    }

    // input example: 11:30 AM
    public static int parseTime(String time){
        String[] timeArr = time.split(" ");
        // example: 11:30
        String timeOfDay = timeArr[0];
        // example AM
        String amOrPm = timeArr[1];

        String[] hoursAndMinutes = timeOfDay.split(":");
        // example 11
        int hours = Integer.parseInt(hoursAndMinutes[0]);
        // example 30
        int minutes = Integer.parseInt(hoursAndMinutes[1]);

        // offset the time by 12 hours if it's pm
        int pmBonusTime = amOrPm.equals("PM") && hours != 12 ? 60 * 12 : 0;

        return hours * 60 + minutes + pmBonusTime;
    }

    public static String GetRawData(String id, String password) {
        WebClient client = new WebClient();
        client.getOptions().setCssEnabled(false);
        client.getOptions().setUseInsecureSSL(true);
        client.getOptions().setDownloadImages(false);
        client.getOptions().setPopupBlockerEnabled(true);
        client.getOptions().setRedirectEnabled(true);
        client.getOptions().setTimeout(30000);
        client.getOptions().setThrowExceptionOnScriptError(false);

        try {
            String loginUrl = "https://dawsoncollege.omnivox.ca/intr/Module/Identification/Login/Login.aspx";
            // Hiding warnings
            java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(Level.OFF);
            java.util.logging.Logger.getLogger("org.apache.commons.httpclient").setLevel(Level.OFF);

            HtmlPage response = client.getPage(loginUrl);
            HtmlForm form = response.getFormByName("formLogin");

            String k = form.getInputByName("k").getValueAttribute();

            URL url = new URL(loginUrl);
            WebRequest loginRequest = new WebRequest(url, HttpMethod.POST);

            // Filling form requests
            ArrayList<NameValuePair> requestParams = new ArrayList<NameValuePair>();
            requestParams.add(new NameValuePair("NoDA", id));
            requestParams.add(new NameValuePair("PasswordEtu", password));
            requestParams.add(new NameValuePair("TypeIdentification", "Etudiant"));
            requestParams.add(new NameValuePair("TypeLogin", "PostSolutionLogin"));
            requestParams.add(new NameValuePair("k", k));
            loginRequest.setRequestParameters(requestParams);

            client.getPage(loginRequest);
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
            LocalDateTime now = LocalDateTime.now();
            String x = dtf.format(now);
            String omnivoxTimetableRedirectUrl = "https://dawsoncollege.omnivox.ca/intr/Module/ServicesExterne/RedirectionServicesExternes.ashx?idService=1077&C=DAW&E=P&L=ANG&Ref="+x;
            HtmlPage response1 = client.getPage(omnivoxTimetableRedirectUrl);
            List<HtmlForm> form1 = response1.getForms();
            HtmlForm form2 = form1.get(0);
            String l = form2.getInputByName("timetable_search_nonce").getValueAttribute();

            // Params for the search
            ArrayList<NameValuePair> newRequestParams = new ArrayList<NameValuePair>();
            newRequestParams.add(new NameValuePair("action", "timetable_search"));
            newRequestParams.add(new NameValuePair("nonce", l));
            newRequestParams.add(new NameValuePair("specific_ed", ""));
            newRequestParams.add(new NameValuePair("discipline", ""));
            newRequestParams.add(new NameValuePair("general_ed", ""));
            newRequestParams.add(new NameValuePair("special_ed", ""));
            newRequestParams.add(new NameValuePair("course_title", "*"));
            newRequestParams.add(new NameValuePair("section", ""));
            newRequestParams.add(new NameValuePair("teacher", ""));
            newRequestParams.add(new NameValuePair("intensive", ""));
            newRequestParams.add(new NameValuePair("seats", ""));

            String timetableUrl = "https://timetable.dawsoncollege.qc.ca/wp-content/plugins/timetable/search.php";
            URL newUrl = new URL(timetableUrl);
            WebRequest fetchData = new WebRequest(newUrl, HttpMethod.POST);
            fetchData.setRequestParameters(newRequestParams);
            HtmlPage response3 = client.getPage(fetchData);

            WebResponse response2 = response3.getWebResponse();
            String content = response2.getContentAsString();

            return content;

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            client.close();
        }
        return null;
    }
}

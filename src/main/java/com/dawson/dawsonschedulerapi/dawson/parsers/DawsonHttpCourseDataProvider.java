package com.dawson.dawsonschedulerapi.dawson.parsers;

import com.dawson.dawsonschedulerapi.common.data.CourseDataProvider;
import com.dawson.dawsonschedulerapi.dawson.classes.Course;
import com.dawson.dawsonschedulerapi.dawson.classes.CourseCache;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DawsonHttpCourseDataProvider implements CourseDataProvider<Course> {
    private final DawsonCourseParser parser;
    private final CourseCache cache;
    private final String id = System.getenv("DAWSON_ID");
    private final String password = System.getenv("DAWSON_PWD");

    public DawsonHttpCourseDataProvider(DawsonCourseParser parser, CourseCache courseCache) {
        this.parser = parser;
        this.cache = courseCache;
    }

    private WebClient configureWebClient() {
        WebClient client = new WebClient();
        client.getOptions().setCssEnabled(false);
        client.getOptions().setUseInsecureSSL(true);
        client.getOptions().setDownloadImages(false);
        client.getOptions().setPopupBlockerEnabled(true);
        client.getOptions().setRedirectEnabled(true);
        client.getOptions().setTimeout(30000);
        client.getOptions().setThrowExceptionOnScriptError(false);
        return client;
    }

    private List<NameValuePair> configureLoginRequestParams(String k) {
        List<NameValuePair> requestParams = new ArrayList<>();
        requestParams.add(new NameValuePair("NoDA", id));
        requestParams.add(new NameValuePair("PasswordEtu", password));
        requestParams.add(new NameValuePair("TypeIdentification", "Etudiant"));
        requestParams.add(new NameValuePair("TypeLogin", "PostSolutionLogin"));
        requestParams.add(new NameValuePair("k", k));
        return requestParams;
    }

    private List<NameValuePair> configureTimeTableRequestParams(String l) {
        List<NameValuePair> requestParams = new ArrayList<>();
        requestParams.add(new NameValuePair("action", "timetable_search"));
        requestParams.add(new NameValuePair("nonce", l));
        requestParams.add(new NameValuePair("specific_ed", ""));
        requestParams.add(new NameValuePair("discipline", ""));
        requestParams.add(new NameValuePair("general_ed", ""));
        requestParams.add(new NameValuePair("special_ed", ""));
        requestParams.add(new NameValuePair("course_title", "*"));
        requestParams.add(new NameValuePair("section", ""));
        requestParams.add(new NameValuePair("teacher", ""));
        requestParams.add(new NameValuePair("intensive", ""));
        requestParams.add(new NameValuePair("seats", ""));
        return requestParams;
    }

    private String getKValue(WebClient client, String loginUrl) throws IOException {
        return ((HtmlPage) client.getPage(loginUrl))
                .getFormByName("formLogin")
                .getInputByName("k")
                .getValueAttribute();
    }

    private String getDateForTimeTableUrl() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now);
    }

    private String getRawData() {
        final String loginUrl = "https://dawsoncollege.omnivox.ca/intr/Module/Identification/Login/Login.aspx";
        final String timetableRedirectUrl = "https://dawsoncollege.omnivox.ca/intr/Module/ServicesExterne/RedirectionServicesExternes.ashx?idService=1077&C=DAW&E=P&L=ANG&Ref=";
        final String timetableUrl = "https://timetable.dawsoncollege.qc.ca/wp-content/plugins/timetable/search.php";

        try (WebClient client = configureWebClient()) {
            URL url = new URL(loginUrl);
            WebRequest loginRequest = new WebRequest(url, HttpMethod.POST);
            loginRequest.setRequestParameters(configureLoginRequestParams(getKValue(client, loginUrl)));
            client.getPage(loginRequest);

            String x = getDateForTimeTableUrl();
            HtmlPage response1 = client.getPage(timetableRedirectUrl + x);
            List<HtmlForm> form1 = response1.getForms();
            HtmlForm form2 = form1.get(0);
            String l = form2.getInputByName("timetable_search_nonce").getValueAttribute();
            URL newUrl = new URL(timetableUrl);
            WebRequest fetchData = new WebRequest(newUrl, HttpMethod.POST);
            fetchData.setRequestParameters(configureTimeTableRequestParams(l));

            HtmlPage response2 = client.getPage(fetchData);
            return response2.getWebResponse().getContentAsString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public List<Course> getCourses() {
        return null;
    }

    @Override
    public void initCache() {
        updateCache();
        scheduleCacheUpdate();
    }

    private void updateCache() {
        String data = getRawData();
        if (data == null) {
            return;
        }

        cache.setCourses(parser.parse(data));
    }

    private void scheduleCacheUpdate() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.schedule(this::updateCache, 1, TimeUnit.MINUTES);
    }
}

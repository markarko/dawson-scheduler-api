package com.dawson.dawsonschedulerapi.dawson.parsers;

import com.azure.storage.blob.*;
import com.dawson.dawsonschedulerapi.common.data.CourseDataProvider;
import com.dawson.dawsonschedulerapi.dawson.classes.Course;
import com.dawson.dawsonschedulerapi.dawson.classes.CourseCache;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Profile("PRODUCTION")
public class DawsonAzureCourseDataProvider implements CourseDataProvider<Course> {
    private final DawsonCourseParser parser;
    private final CourseCache cache;
    private final BlobServiceClient blobServiceClient;
    private final String containerName = System.getenv("AZURE_CONTAINER_NAME");
    private final String coursesBlobName = System.getenv("COURSES_BLOB_NAME");

    public DawsonAzureCourseDataProvider(DawsonCourseParser parser, CourseCache cache) {
        this.parser = parser;
        this.cache = cache;
        String connectionString = System.getenv("AZURE_STORAGE_CONNECTION_STRING");
        if (connectionString == null) {
            this.blobServiceClient = null;
        } else {
            this.blobServiceClient = new BlobServiceClientBuilder().connectionString(connectionString).buildClient();
        }
    }

    public String readFileFromBlob(String blobName) {
        try {
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
            BlobClient blobClient = containerClient.getBlobClient(blobName);

            if (blobClient.exists()) {
                InputStream inputStream = blobClient.openInputStream();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                    return reader.lines().collect(Collectors.joining("\n"));
                }
            } else {
                System.out.println("Blob does not exist");
                return null;
            }
        } catch (IOException e) {
            System.out.println("Could not read blob");
        }
        return null;
    }

    @Override
    public List<Course> getCourses() {
        return cache.getCourses();
    }

    @Override
    public void initCache() {
        cache.setCourses(parser.parse(readFileFromBlob(coursesBlobName)));
    }
}

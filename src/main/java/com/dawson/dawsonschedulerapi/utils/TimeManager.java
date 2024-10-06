package com.dawson.dawsonschedulerapi.utils;

import org.springframework.stereotype.Component;

@Component
public class TimeManager {
    public int militaryTimeToMinutes(String militaryTime) {
        if (militaryTime == null || militaryTime.isEmpty() || militaryTime.isBlank()) {
            return -1;
        }

        String[] hoursAndMinutes = militaryTime.split(":");
        if (hoursAndMinutes.length != 2) {
            return -1;
        }

        try {
            int hours = Integer.parseInt(hoursAndMinutes[0]);
            if (hours < 0 || hours > 23) {
                return -1;
            }

            int minutes = Integer.parseInt(hoursAndMinutes[0]);
            if (minutes < 0 || minutes > 59) {
                return -1;
            }

            return hours * 60 + minutes;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public int weekDayToInt(String dayOfWeek) {
        return switch (dayOfWeek.toLowerCase()) {
            case "sunday" -> 1;
            case "monday" -> 2;
            case "tuesday" -> 3;
            case "wednesday" -> 4;
            case "thursday" -> 5;
            case "friday" -> 6;
            case "saturday" -> 7;
            default -> -1;
        };
    }
}

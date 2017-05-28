package com.askylol.bookaseat.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Sharon on 28-May-17.
 */
public class OpeningHours {
    private Map<String, Pair<TimeOfDay, TimeOfDay>> weeklySchedule = new HashMap<>();

    public void setOpeningHours(Day day, TimeOfDay openingHours, TimeOfDay closingHours) {
        weeklySchedule.put(day.toString(), new Pair<>(openingHours, closingHours));
    }

    public Pair<TimeOfDay, TimeOfDay> getOpeningHours(Day day) {
        return weeklySchedule.get(day.toString());
    }

    public Map<String, Pair<TimeOfDay, TimeOfDay>> getWeeklySchedule() {
        return weeklySchedule;
    }

    @Override
    public String toString() {
        return "OpeningHours{" +
                "weeklySchedule=" + weeklySchedule +
                '}';
    }

    public enum Day {
        SUNDAY,
        MONDAY,
        TUESDAY,
        WEDNESDAY,
        THURSDAY,
        FRIDAY,
        SATURDAY
    }
}

package com.example.guardiana.utility;

import java.util.Calendar;

public final class Utility {


    public static String showDayMessage() {
        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);
        String message = "";
        if (timeOfDay < 12) {
            message = "Good Morning";
        } else if (timeOfDay < 16) {
            message = "Good Afternoon";
        } else if (timeOfDay < 21) {
            message = "Good Evening";
        } else {
            message = "Good Night";
        }
        return message;
    }

    public static boolean isDay() {
        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);
        return timeOfDay < 14;
    }

}

package com.example.calendarapp;

import java.util.HashMap;
import java.util.Map;
import java.util.Calendar;

public class HolidayManager {
    private static final Map<String, String> fixedHolidays = new HashMap<>();
    private static final Map<String, LunarHoliday> lunarHolidays = new HashMap<>();

    static {
        // 固定節日 (格式: MM-dd)
        fixedHolidays.put("01-01", "元旦");
        fixedHolidays.put("02-28", "和平紀念日");
        fixedHolidays.put("03-08", "婦女節");
        fixedHolidays.put("03-12", "植樹節");
        fixedHolidays.put("04-04", "兒童節");
        fixedHolidays.put("04-05", "清明節");
        fixedHolidays.put("05-01", "勞動節");
        fixedHolidays.put("09-28", "教師節");
        fixedHolidays.put("10-10", "國慶節");
        fixedHolidays.put("12-24", "平安夜");
        fixedHolidays.put("12-25", "聖誕節");

        // 農曆節日
        lunarHolidays.put("除夕", new LunarHoliday(12, 29));
        lunarHolidays.put("春節", new LunarHoliday(1, 1));
        lunarHolidays.put("元宵節", new LunarHoliday(1, 15));
        lunarHolidays.put("端午節", new LunarHoliday(5, 5));
        lunarHolidays.put("七夕", new LunarHoliday(7, 7));
        lunarHolidays.put("中秋節", new LunarHoliday(8, 15));
        lunarHolidays.put("重陽節", new LunarHoliday(9, 9));
    }

    public static String getHoliday(Calendar date) {
        try {
            // 檢查固定節日
            String monthDay = String.format("%02d-%02d",
                    date.get(Calendar.MONTH) + 1,
                    date.get(Calendar.DAY_OF_MONTH));

            String holiday = fixedHolidays.get(monthDay);
            if (holiday != null) {
                return holiday;
            }

            // 檢查農曆節日
            Lunar lunar = new Lunar(date);
            for (Map.Entry<String, LunarHoliday> entry : lunarHolidays.entrySet()) {
                if (entry.getValue().matches(lunar)) {
                    return entry.getKey();
                }
            }
        } catch (Exception e) {
            // 農曆轉換出錯時，只顯示陽曆節日
            e.printStackTrace();
        }
        return null;
    }

    private static class LunarHoliday {
        final int month;
        final int day;

        LunarHoliday(int month, int day) {
            this.month = month;
            this.day = day;
        }

        boolean matches(Lunar lunar) {
            return lunar.getMonth() == month && lunar.getDay() == day;
        }
    }

}
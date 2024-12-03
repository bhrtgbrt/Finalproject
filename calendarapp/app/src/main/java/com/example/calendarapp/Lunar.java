package com.example.calendarapp;

import java.util.Calendar;

public class Lunar {
    private int year;
    private int month;
    private int day;

    // 農曆資料陣列 (1900-2100 年)
    private static final long[] LUNAR_INFO = new long[]{
            0x04bd8,0x04ae0,0x0a570,0x054d5,0x0d260,0x0d950,0x16554,0x056a0,0x09ad0,0x055d2,
            0x04ae0,0x0a5b6,0x0a4d0,0x0d250,0x1d255,0x0b540,0x0d6a0,0x0ada2,0x095b0,0x14977,
            0x04970,0x0a4b0,0x0b4b5,0x06a50,0x06d40,0x1ab54,0x02b60,0x09570,0x052f2,0x04970,
            0x06566,0x0d4a0,0x0ea50,0x06e95,0x05ad0,0x02b60,0x186e3,0x092e0,0x1c8d7,0x0c950,
            0x0d4a0,0x1d8a6,0x0b550,0x056a0,0x1a5b4,0x025d0,0x092d0,0x0d2b2,0x0a950,0x0b557,
            0x06ca0,0x0b550,0x15355,0x04da0,0x0a5d0,0x14573,0x052d0,0x0a9a8,0x0e950,0x06aa0,
            0x0aea6,0x0ab50,0x04b60,0x0aae4,0x0a570,0x05260,0x0f263,0x0d950,0x05b57,0x056a0,
            0x096d0,0x04dd5,0x04ad0,0x0a4d0,0x0d4d4,0x0d250,0x0d558,0x0b540,0x0b5a0,0x195a6,
            0x095b0,0x049b0,0x0a974,0x0a4b0,0x0b27a,0x06a50,0x06d40,0x0af46,0x0ab60,0x09570,
            0x04af5,0x04970,0x064b0,0x074a3,0x0ea50,0x06b58,0x055c0,0x0ab60,0x096d5,0x092e0,
            0x0c960,0x0d954,0x0d4a0,0x0da50,0x07552,0x056a0,0x0abb7,0x025d0,0x092d0,0x0cab5,
            0x0a950,0x0b4a0,0x0baa4,0x0ad50,0x055d9,0x04ba0,0x0a5b0,0x15176,0x052b0,0x0a930,
            0x07954,0x06aa0,0x0ad50,0x05b52,0x04b60,0x0a6e6,0x0a4e0,0x0d260,0x0ea65,0x0d530,
            0x05aa0,0x076a3,0x096d0,0x04bd7,0x04ad0,0x0a4d0,0x1d0b6,0x0d250,0x0d520,0x0dd45,
            0x0b5a0,0x056d0,0x055b2,0x049b0,0x0a577,0x0a4b0,0x0aa50,0x1b255,0x06d20,0x0ada0
    };

    // 每月天數
    private static final int[] DAYS_IN_LUNAR_MONTH = {29, 30};

    // 天干
    private static final String[] CELESTIAL_STEMS = {
            "甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸"
    };

    // 地支
    private static final String[] TERRESTRIAL_BRANCHES = {
            "子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥"
    };

    public Lunar(Calendar solar) {
        int baseYear = 1900;
        int baseMonth = 1;
        int baseDay = 30;

        // 計算輸入日期距離基準日期的天數
        Calendar baseDate = Calendar.getInstance();
        baseDate.set(baseYear, baseMonth - 1, baseDay);

        long offset = (solar.getTimeInMillis() - baseDate.getTimeInMillis()) / 86400000L;

        // 計算農曆年份
        int lunarYear = baseYear;
        while (offset > 0) {
            int daysInYear = getLunarYearDays(lunarYear);
            if (offset < daysInYear) {
                break;
            }
            offset -= daysInYear;
            lunarYear++;
        }

        // 計算農曆月份
        int lunarMonth = 1;
        int leapMonth = getLeapMonth(lunarYear);
        boolean isLeap = false;

        while (offset > 0) {
            int daysInMonth;
            if (isLeap) {
                daysInMonth = getLeapMonthDays(lunarYear);
                isLeap = false;
            } else {
                daysInMonth = getLunarMonthDays(lunarYear, lunarMonth);
                if (lunarMonth == leapMonth) {
                    isLeap = true;
                    daysInMonth = getLeapMonthDays(lunarYear);
                }
            }

            if (offset < daysInMonth) {
                break;
            }
            offset -= daysInMonth;

            if (!isLeap) {
                lunarMonth++;
            }
        }

        // 計算農曆日期
        int lunarDay = (int) (offset + 1);

        this.year = lunarYear;
        this.month = lunarMonth;
        this.day = lunarDay;
    }

    // 取得該年農曆總天數
    private static int getLunarYearDays(int year) {
        int sum = 348;
        int index = year - 1900;
        if (index < 0 || index >= LUNAR_INFO.length) {
            return 365;
        }

        for (int i = 0x8000; i > 0x8; i >>= 1) {
            sum += ((LUNAR_INFO[index] & i) != 0) ? 1 : 0;
        }
        return sum + getLeapMonthDays(year);
    }

    // 取得閏月天數
    private static int getLeapMonthDays(int year) {
        int index = year - 1900;
        if (index < 0 || index >= LUNAR_INFO.length) {
            return 0;
        }
        int leapMonth = getLeapMonth(year);
        if (leapMonth == 0) {
            return 0;
        }
        return ((LUNAR_INFO[index] & 0x10000) != 0) ? 30 : 29;
    }

    // 取得該月天數
    private static int getLunarMonthDays(int year, int month) {
        int index = year - 1900;
        if (index < 0 || index >= LUNAR_INFO.length || month < 1 || month > 12) {
            return 30;
        }
        return ((LUNAR_INFO[index] & (0x10000 >> month)) != 0) ? 30 : 29;
    }

    // 取得閏月月份
    private static int getLeapMonth(int year) {
        int index = year - 1900;
        if (index < 0 || index >= LUNAR_INFO.length) {
            return 0;
        }
        return (int) (LUNAR_INFO[index] & 0xf);
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public int getDay() {
        return day;
    }

    // 取得農曆年的生肖
    public String getZodiac() {
        return TERRESTRIAL_BRANCHES[(year - 4) % 12];
    }

    // 取得農曆年的干支
    public String getCyclical() {
        int num = year - 1900 + 36;
        return CELESTIAL_STEMS[num % 10] + TERRESTRIAL_BRANCHES[num % 12];
    }
}
package com.example.calendarapp;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class LunarCalendarUtils {
    // 農曆月份名稱
    private static final String[] LUNAR_MONTH_NAMES = {
            "正月", "二月", "三月", "四月", "五月", "六月",
            "七月", "八月", "九月", "十月", "冬月", "臘月"
    };

    // 農曆日期名稱
    private static final String[] LUNAR_DAY_NAMES = {
            "初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十",
            "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十",
            "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九", "三十"
    };

    // 天干
    private static final String[] HEAVENLY_STEMS = {
            "甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸"
    };

    // 地支
    private static final String[] EARTHLY_BRANCHES = {
            "子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥"
    };

    // 生肖
    private static final String[] ZODIAC = {
            "鼠", "牛", "虎", "兔", "龍", "蛇", "馬", "羊", "猴", "雞", "狗", "豬"
    };

    // 農曆資料（農曆月份的二進制編碼）
    private static final Map<Integer, int[]> LUNAR_INFO = new HashMap<>();

    static {
        // 這裡需要填入農曆資料，這是一個簡化版本
        // 實際上需要更詳細和準確的農曆資料
        LUNAR_INFO.put(2024, new int[]{0x04bd8, 0x04ae0, 0x0a570, 0x054d5, 0x0d260, 0x0d950});
    }

    /**
     * 將公曆日期轉換為農曆日期
     * @param calendar 公曆日期
     * @return 農曆日期字串
     */
    public static String getLunarDate(Calendar calendar) {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // 這裡是一個簡化的轉換，實際需要更複雜的算法
        return getLunarMonthName(month) + getLunarDayName(day) +
                " " + getYearGanZhi(year) + "年";
    }

    /**
     * 獲取農曆月份名稱
     */
    private static String getLunarMonthName(int month) {
        return month >= 1 && month <= 12 ? LUNAR_MONTH_NAMES[month - 1] : "";
    }

    /**
     * 獲取農曆日期名稱
     */
    private static String getLunarDayName(int day) {
        return day >= 1 && day <= 30 ? LUNAR_DAY_NAMES[day - 1] : "";
    }

    /**
     * 獲取年份的干支
     */
    private static String getYearGanZhi(int year) {
        int ganIndex = (year - 4) % 10;
        int zhiIndex = (year - 4) % 12;
        return HEAVENLY_STEMS[ganIndex] + EARTHLY_BRANCHES[zhiIndex];
    }

    /**
     * 獲取生肖
     */
    public static String getZodiac(int year) {
        return ZODIAC[(year - 4) % 12];
    }
}
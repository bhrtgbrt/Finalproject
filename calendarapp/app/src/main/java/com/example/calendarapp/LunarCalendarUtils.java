package com.example.calendarapp;

import com.lunar.Solar;
import com.lunar.Lunar;

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

    // 屬相
    private static final String[] ZODIACS = {
            "猪", "鼠", "牛", "虎", "兔", "龍", "蛇", "馬", "羊", "猴", "雞", "狗"
    };

    // 節日對應表
    private static final Map<String, String> LUNAR_FESTIVALS = new HashMap<>();
    static {
        LUNAR_FESTIVALS.put("除夕", "臘月三十");
        LUNAR_FESTIVALS.put("春節", "正月初一");
        LUNAR_FESTIVALS.put("元宵節", "正月十五");
        LUNAR_FESTIVALS.put("清明節", "清明");
        LUNAR_FESTIVALS.put("端午節", "五月初五");
        LUNAR_FESTIVALS.put("中秋節", "八月十五");
    }

    /**
     * 獲取農曆日期
     * @param calendar 公曆日期
     * @return 農曆日期字串
     */
    public static String getLunarDate(Calendar calendar) {
        Solar solar = new Solar(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        Lunar lunar = solar.toLunar();

        return getLunarMonthName(lunar.getLunarMonth()) + getLunarDayName(lunar.getLunarDay());
    }

    /**
     * 檢查是否為農曆節日
     * @param lunarDate 農曆日期
     * @return 節日名稱，不是節日則返回空字串
     */
    public static String getFestival(String lunarDate) {
        for (Map.Entry<String, String> entry : LUNAR_FESTIVALS.entrySet()) {
            if (entry.getValue().equals(lunarDate)) {
                return entry.getKey();
            }
        }
        return "";
    }

    /**
     * 獲取農曆月份名稱
     */
    public static String getLunarMonthName(int month) {
        // 處理閏月
        if (month < 0) {
            return "閏" + LUNAR_MONTH_NAMES[Math.abs(month) - 1];
        }
        return month >= 1 && month <= 12 ? LUNAR_MONTH_NAMES[month - 1] : "";
    }

    /**
     * 獲取農曆日期名稱
     */
    public static String getLunarDayName(int day) {
        return day >= 1 && day <= 30 ? LUNAR_DAY_NAMES[day - 1] : "";
    }

    /**
     * 獲取生肖
     * @param year 公曆年份
     * @return 生肖
     */
    public static String getZodiac(int year) {
        return ZODIACS[(year - 4) % 12];
    }

    /**
     * 獲取完整的農曆日期信息
     * @param calendar 公曆日期
     * @return 完整的農曆日期信息
     */
    public static String getFullLunarDate(Calendar calendar) {
        Solar solar = new Solar(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        Lunar lunar = solar.toLunar();

        return String.format("%d年 %s%s",
                lunar.getLunarYear(),
                getLunarMonthName(lunar.getLunarMonth()),
                getLunarDayName(lunar.getLunarDay())
        );
    }
}
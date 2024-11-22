package com.example.calendarapp;

import java.util.Calendar;
import java.util.Date;

public class Lunar {
    private int year;
    private int month;
    private int day;
    private static int[] lunarInfo = {
            0x04bd8,0x04ae0,0x0a570,0x054d5,0x0d260,0x0d950,0x16554,0x056a0,0x09ad0,0x055d2,
            0x04ae0,0x0a5b6,0x0a4d0,0x0d250,0x1d255,0x0b540,0x0d6a0,0x0ada2,0x095b0,0x14977,
            0x04970,0x0a4b0,0x0b4b5,0x06a50,0x06d40,0x1ab54,0x02b60,0x09570,0x052f2,0x04970,
            0x06566,0x0d4a0,0x0ea50,0x06e95,0x05ad0,0x02b60,0x186e3,0x092e0,0x1c8d7,0x0c950,
            0x0d4a0,0x1d8a6,0x0b550,0x056a0,0x1a5b4,0x025d0,0x092d0,0x0d2b2,0x0a950,0x0b557
    };

    public Lunar(Calendar cal) {
        @SuppressWarnings("unused")
        int yearCyl,monCyl,dayCyl;
        int leapMonth = 0;

        // 修正：使用 Calendar 而不是過時的 Date 建構子
        Calendar baseCalendar = Calendar.getInstance();
        baseCalendar.set(1900, Calendar.JANUARY, 31);
        Date baseDate = baseCalendar.getTime();

        // 求出和1900年1月31日相差的天數
        int offset = (int)((cal.getTime().getTime() - baseDate.getTime())/86400000);
        dayCyl = offset + 40;
        monCyl = 14;

        // 用offset減去每農曆年的天數，確定農曆年份
        int iYear, daysOfYear = 0;
        for (iYear = 1900; iYear < 2100 && offset > 0; iYear++) {
            daysOfYear = yearDays(iYear);
            offset -= daysOfYear;
            monCyl += 12;
        }
        if (offset < 0) {
            offset += daysOfYear;
            iYear--;
            monCyl -= 12;
        }

        // 新增：確保年份在有效範圍內
        if (iYear < 1900 || iYear >= 1900 + lunarInfo.length) {
            throw new IllegalArgumentException("年份超出範圍：" + iYear);
        }

        year = iYear;
        yearCyl = iYear - 1864;
        leapMonth = leapMonth(iYear); // 閏哪個月,1-12

        boolean leap = false;

        // 用當年的天數offset,逐個減去每月（農曆）的天數，求出當天是本月的第幾天
        int iMonth, daysOfMonth = 0;
        for (iMonth = 1; iMonth < 13 && offset > 0; iMonth++) {
            // 閏月
            if (leapMonth > 0 && iMonth == (leapMonth + 1) && !leap) {
                --iMonth;
                leap = true;
                daysOfMonth = leapDays(year);
            } else {
                daysOfMonth = monthDays(year, iMonth);
            }

            offset -= daysOfMonth;
            if (leap && iMonth == (leapMonth + 1)) leap = false;
            if (!leap) monCyl++;
        }

        // offset為0時，並且剛才計算的月份是閏月，要校正
        if (offset == 0 && leapMonth > 0 && iMonth == leapMonth + 1) {
            if (leap) {
                leap = false;
            } else {
                leap = true;
                --iMonth;
                --monCyl;
            }
        }

        // offset小於0時，也要校正
        if (offset < 0) {
            offset += daysOfMonth;
            --iMonth;
            --monCyl;
        }
        month = iMonth;
        day = offset + 1;
    }

    // 獲取月份
    public int getMonth() {
        return month;
    }

    // 獲取日期
    public int getDay() {
        return day;
    }

    // 計算某年有多少天
    private static int yearDays(int y) {
        // 新增：年份檢查
        if (y < 1900 || y >= 1900 + lunarInfo.length) {
            throw new IllegalArgumentException("年份超出範圍：" + y);
        }

        int i, sum = 348;
        for (i = 0x8000; i > 0x8; i >>= 1) {
            if ((lunarInfo[y - 1900] & i) != 0) sum += 1;
        }
        return (sum + leapDays(y));
    }

    // 計算某年閏月的天數
    private static int leapDays(int y) {
        // 新增：年份檢查
        if (y < 1900 || y >= 1900 + lunarInfo.length) {
            throw new IllegalArgumentException("年份超出範圍：" + y);
        }

        if (leapMonth(y) != 0) {
            if ((lunarInfo[y - 1900] & 0x10000) != 0)
                return 30;
            else
                return 29;
        }
        return 0;
    }

    // 計算某年哪個月是閏月
    private static int leapMonth(int y) {
        // 新增：年份檢查
        if (y < 1900 || y >= 1900 + lunarInfo.length) {
            return 0;  // 超出範圍時返回 0，表示該年沒有閏月
        }

        return (int) (lunarInfo[y - 1900] & 0xf);
    }

    // 計算某年某月的天數
    private static int monthDays(int y, int m) {
        // 新增：年份檢查
        if (y < 1900 || y >= 1900 + lunarInfo.length) {
            throw new IllegalArgumentException("年份超出範圍：" + y);
        }

        if ((lunarInfo[y - 1900] & (0x10000 >> m)) == 0)
            return 29;
        else
            return 30;
    }
}
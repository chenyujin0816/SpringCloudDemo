package cn.edu.bit.utils;

import java.text.ParseException;
import java.util.Calendar;

public class DateUtils {
    public static String getDate() throws ParseException {
    //获取当前日期
        Calendar now = Calendar.getInstance();
        int year = now.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH) + 1;//注意月份
        int day = now.get(Calendar.DAY_OF_MONTH);
        String date="";
        date+=String.valueOf(year);
        date+=month>=10?String.valueOf(month):"0"+String.valueOf(month);
        date+=String.valueOf(day);
        return date;
    }

}
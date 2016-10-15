package com.sysu.infoexchange.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {

	// 获取当前时间的标准秒
	public static long getCurrentDayBeginTimeStamp() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.MILLISECOND, 001);
		return cal.getTimeInMillis();
	}

	// 获取当前时间
	public static Date getCurrrentDate() {
		Date date = new Date();
		return date;
	}

	// 获取当前时间的字符串形式
	public static String getDateString(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String str = sdf.format(date);
		return str;
	}

	// 两个date之间相差多少天
	public static int getOffsetBetweenDay(Date begin, Date end) {
		return (int) ((end.getTime() - begin.getTime()) / 86400000);
	}

	// 给定一个时间，判断与今天相差多少天
	public static int getOffsetFromToday(Date date) {
		return (int) ((System.currentTimeMillis() - date.getTime()) / 86400000);
	}

	// 对Date进行日期单位的加减法
	public static Date dataIncrease(Date begin, int num) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(begin);
		calendar.add(calendar.DATE, num);// 把日期往后增加一天.整数往后推,负数往前移动
		return calendar.getTime(); // 这个时间就是日期往后推一天的结果
	}
	
	public static Date getData(int year, int month, int day) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(year, month, day);
		return calendar.getTime(); // 这个时间就是日期往后推一天的结果
	}
}

package com.health.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeHelper {
	public static final long MILLSEC_DAY = 1000 * 60 * 60 * 24;
	public static final String FORMAT = "yyyy/MM/dd HH:mm:ss";
	private static DateFormat formater = new SimpleDateFormat(FORMAT);

	/**
	 * ��ȡ��ǰʱ����ַ�����ʽ��ʾ����2013-11-25 10:43:52
	 * 
	 * @return
	 */
	public static String getCurrentTime() {
		return formater.format(new Date()).toString();
	}

	public static Date getCurrentDate() {
		return new Date();
	}

	
	/***
	 * ��beforeDaysǰ�ĸ�ʽ������
	 * 
	 * @param beforeDays
	 * @return
	 */
	public static String getBeforeTime(int beforeDays) {
		Date current = new Date();
		long longTime = current.getTime();
		Date beforeDate = new Date(longTime - MILLSEC_DAY * beforeDays);
		return formater.format(beforeDate).toString();
	}

	public static long parseTime(String stime) throws ParseException {
		return formater.parse(stime).getTime();
	}
}

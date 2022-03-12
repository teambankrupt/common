package com.example.common.utils;

import org.apache.commons.lang3.time.DateUtils;
import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

public final class DateUtil {
	private DateUtil() {
	}

	public static String SERVER_DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
	public static String DATE_PATTERN_BACKWARDS = "yyyy-MM-dd";
	public static String DATE_TIME_PATTERN_BACKWARDS = "yyyy-MM-dd HH:mm:ss";

	public static String DATE_PATTERN_READABLE = "MMM dd, YYYY";
	public static String TIME_PATTERN_READABLE = "hh:mm a";
	public static String DATE_TIME_PATTERN_READABLE = "MMM dd, YYYY hh:mm a";


	public static final String SERVER_DATE_TIME_PATTERN2 = "yyyy-MM-dd'T'HH:mm";
	public static final String DATE_PATTERN_READABLE_dd_MM_yyyy = "dd-MM-yyyy";
	public static final String DATE_PATTERN_READABLE_MONTH_YEAR = "MMM, yy";
	public static final String DATE_PATTERN_READABLE_DAY_NAME = "MMM dd, YYYY EEEE";
	public static final String DATE_PATTERN_DAY_MONTH_NAME = "MMMM dd, YYYY EEEE";
	public static final String DATE_PATTERN_SMS_REPORT = "dd-MM-yy: hh-mm";
	public static final String DATE_PATTERN_SMS_REPORT_TEMP = "dd-MM-yy";
	public static final String DATE_PATTERN_REPORT_TEMP = "dd/MM/yyyy";
	public static final String DATE_PATTERN_DOTTED = "dd.MM.yy";
	public static final String DATE_PATTERN_DOTTED_2 = "dd.MM.yyyy";
	public static final String DATE_PATTERN_MONTH = "MMMM";
	public static final String DATE_PATTERN_MONTH_YEAR_COMPACT = "MMyy";


	public static String getReadableDate(Date date) {
		return getReadableDateFormat().format(date);
	}

	public static String getReadableTime(Date date) {
		return getReadableTimeFormat().format(date);
	}

	public static String getReadableDateTime(Date date) {
		return getReadableDateTimeFormat().format(date);
	}


	public static Date parseServerDateTime(String date) throws ParseException {
		DateFormat sdf = getServerDateTimeFormat();
		sdf.setTimeZone(TimeZone.getDefault());
		return sdf.parse(date);
	}

	public static DateFormat getReadableDateFormat() {
		return new SimpleDateFormat(DATE_PATTERN_READABLE);
	}

	public static DateFormat getReadableTimeFormat() {
		return new SimpleDateFormat(TIME_PATTERN_READABLE);
	}

	public static DateFormat getReadableDateTimeFormat() {
		return new SimpleDateFormat(DATE_TIME_PATTERN_READABLE);
	}

	public static DateFormat getServerDateTimeFormat() {
		return new SimpleDateFormat(SERVER_DATE_TIME_PATTERN);
	}

	public static String getReadableDateWithDayName(Date date, String pattern) {
		return new SimpleDateFormat(pattern).format(date);
	}

	public static String getDateType(Date date) {
		Date lastWeekDay = getWeekDayEnd(date);
		return date.getDate() + " - " + lastWeekDay.getDate() + ", " + getMonthNameFromDate(date);
	}

	public static String getMonthNameFromDate(Date date) {
		Format formatter = new SimpleDateFormat("MMMM");
		return formatter.format(date);
	}

	public static java.time.Period getAge(Date date) {
		if (date == null) return null;
		LocalDate today = LocalDate.now();
		LocalDate birthday = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

		return java.time.Period.between(birthday, today);
	}

	public static long getDurationInDays(Date date) {
		if (date == null) return 0L;
		LocalDate today = LocalDate.now();
		LocalDate myDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		return ChronoUnit.DAYS.between(myDate, today);
	}

	public static List<Date> getDatesBetween(Date startDate, Date endDate) {
		List<Date> datesInRange = new ArrayList<>();
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(startDate);

		Calendar endCalendar = new GregorianCalendar();
		endCalendar.setTime(endDate);

		while (calendar.before(endCalendar)) {
			Date result = calendar.getTime();
			datesInRange.add(result);
			calendar.add(Calendar.DATE, 1);
		}
		return datesInRange;
	}

	public static Collection<Date> getDatesForPeriod(Periods period) throws ParseException {
		Map<DateRangeType, Calendar> dateRange = DateUtil.buildDateRange(period);
		Date fromDate = dateRange.get(DateRangeType.DATE_FROM).getTime();
		Date toDate = dateRange.get(DateRangeType.DATE_TO).getTime();
		switch (period) {
			case TODAY:
				return DateUtil.getDatesBetween(fromDate, toDate);
			case THIS_WEEK:
				return DateUtil.getWeeksBetween(fromDate, toDate);
			default:
				return DateUtil.getMonthBetween(fromDate, toDate);
		}
	}

	public static Date getDayStart(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 1);
		return calendar.getTime();
	}

	public static Date getDayEnd(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.DAY_OF_MONTH, 1);
		calendar.setTime(getDayStart(calendar.getTime()));
		calendar.add(Calendar.SECOND, -2);
		return calendar.getTime();
	}

	public static Date getMonthStartDate(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
		return getDayStart(calendar.getTime());
	}


	public static Date getMonthEndDate(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
		getEndOfDay(calendar);
		return getLastDayOfMonth(calendar.getTime());
	}

	public static Date getLastDayOfMonth(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);

		calendar.add(Calendar.MONTH, 1);
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		calendar.add(Calendar.DATE, -1);

		return calendar.getTime();
	}

	public static Date getFirstMonthOfStartDate(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);

		calendar.add(Calendar.MONTH, Calendar.JANUARY);
		calendar.set(Calendar.DAY_OF_MONTH, 1);

		return getMonthStartDate(calendar.getTime());
	}

	public static Date getLastMonthOfEndDate(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);

		calendar.add(Calendar.MONTH, Calendar.DECEMBER);
		calendar.set(Calendar.DAY_OF_MONTH, 31);

		return getMonthEndDate(calendar.getTime());
	}

	public static Calendar getEndOfDay(Calendar date) {
		date.setTime(DateUtils.addMilliseconds(DateUtils.ceiling(date.getTime(), Calendar.DATE), -1));
		date.set(Calendar.MILLISECOND, 0);
		return date;
	}

	public static Date getPolarizedDatesForPeriod(Periods period, Date date, boolean start) {
		switch (period) {
			case TODAY: {
				if (start) return getDayStart(date);
				else return getDayEnd(date);
			}
			case THIS_WEEK:
			case LAST_WEEK: {
				if (start) return getWeekStartDate(date);
				else return getWeekEndDate(date);
			}
			case THIS_MONTH:
			case LAST_MONTH: {
				if (start) return getWeekStartDate(date);
				else return getWeekDayEnd(date);
			}
			case THIS_YEAR:
			case LAST_YEAR: {
				if (start) return getMonthStartDate(date);
				else return getMonthEndDate(date);
			}
			default:
				if (start) return getDayStart(date);
				else return getDayEnd(date);
		}
	}

	public static Date getWeekStartDate(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.DAY_OF_WEEK, 1);
		return getDayStart(cal.getTime());
	}

	public static Date getWeekEndDate(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.DAY_OF_WEEK, 7);
		return getDayStart(cal.getTime());
	}

	public static Map<DateRangeType, Calendar> buildDateRange(Periods period) {
		DateTimeUtil datetimeUtil = new DateTimeUtil(Calendar.getInstance());
		Calendar dateFrom, dateTo;
		switch (period) {
			case TODAY:
				dateFrom = Calendar.getInstance();
				dateTo = Calendar.getInstance();
				dateFrom.setTime(getDayStart(new Date()));
				dateTo.setTime(getDayEnd(new Date()));
				break;
			case THIS_WEEK:
				dateFrom = datetimeUtil.getThisWeekStartDate();
				dateTo = datetimeUtil.getThisWeekEndDate();
				break;
			case LAST_WEEK:
				dateFrom = datetimeUtil.getLastWeekStartDate();
				dateTo = datetimeUtil.getLastWeekEndDate();
				break;
			case THIS_MONTH:
				dateFrom = datetimeUtil.getThisMonthStartDate();
				dateTo = datetimeUtil.getThisMonthEndDate();
				break;
			case LAST_MONTH:
				dateFrom = datetimeUtil.getLastMonthStartDate();
				dateTo = datetimeUtil.getLastMonthEndDate();
				break;
			case THIS_YEAR:
				dateFrom = datetimeUtil.getThisYearStartDate();
				dateTo = datetimeUtil.getThisYearEndDate();
				break;
			case LAST_YEAR:
				dateFrom = datetimeUtil.getLastYearStartDate();
				dateTo = datetimeUtil.getLastYearEndDate();
				break;
			case ALL_TIME:
			default:
				dateFrom = datetimeUtil.getBeginningFromDate();
				dateTo = datetimeUtil.getThisYearEndDate();
				break;
		}
		Map<DateRangeType, Calendar> dateRangeMap = new HashMap<>();
		dateRangeMap.put(DateRangeType.DATE_FROM, dateFrom);
		dateRangeMap.put(DateRangeType.DATE_TO, dateTo);
		return dateRangeMap;
	}

	public static Map<DateRangeType, Calendar> buildDateRangeWithMonth(String month, int year) {
		DateTimeUtil datetimeUtil = new DateTimeUtil(Calendar.getInstance());
		datetimeUtil.setYear(year);
		Calendar dateFrom, dateTo;
		switch (month.toLowerCase()) {
			case "january":
				datetimeUtil.setMonth(Calendar.JANUARY);
				dateFrom = datetimeUtil.getThisMonthStartDate();
				dateTo = datetimeUtil.getThisMonthEndDate();
				break;
			case "february":
				datetimeUtil.setMonth(Calendar.FEBRUARY);
				dateFrom = datetimeUtil.getThisMonthStartDate();
				dateTo = datetimeUtil.getThisMonthEndDate();
				break;
			case "march":
				datetimeUtil.setMonth(Calendar.MARCH);
				dateFrom = datetimeUtil.getThisMonthStartDate();
				dateTo = datetimeUtil.getThisMonthEndDate();
				break;
			case "april":
				datetimeUtil.setMonth(Calendar.APRIL);
				dateFrom = datetimeUtil.getThisMonthStartDate();
				dateTo = datetimeUtil.getThisMonthEndDate();
				break;
			case "may":
				datetimeUtil.setMonth(Calendar.MAY);
				dateFrom = datetimeUtil.getThisMonthStartDate();
				dateTo = datetimeUtil.getThisMonthEndDate();
				break;
			case "june":
				datetimeUtil.setMonth(Calendar.JUNE);
				dateFrom = datetimeUtil.getThisMonthStartDate();
				dateTo = datetimeUtil.getThisMonthEndDate();
				break;
			case "july":
				datetimeUtil.setMonth(Calendar.JULY);
				dateFrom = datetimeUtil.getThisMonthStartDate();
				dateTo = datetimeUtil.getThisMonthEndDate();
				break;
			case "august":
				datetimeUtil.setMonth(Calendar.AUGUST);
				dateFrom = datetimeUtil.getThisMonthStartDate();
				dateTo = datetimeUtil.getThisMonthEndDate();
				break;
			case "september":
				datetimeUtil.setMonth(Calendar.SEPTEMBER);
				dateFrom = datetimeUtil.getThisMonthStartDate();
				dateTo = datetimeUtil.getThisMonthEndDate();
				break;
			case "october":
				datetimeUtil.setMonth(Calendar.OCTOBER);
				dateFrom = datetimeUtil.getThisMonthStartDate();
				dateTo = datetimeUtil.getThisMonthEndDate();
				break;
			case "november":
				datetimeUtil.setMonth(Calendar.NOVEMBER);
				dateFrom = datetimeUtil.getThisMonthStartDate();
				dateTo = datetimeUtil.getThisMonthEndDate();
				break;
			case "december":
				datetimeUtil.setMonth(Calendar.DECEMBER);
				dateFrom = datetimeUtil.getThisMonthStartDate();
				dateTo = datetimeUtil.getThisMonthEndDate();
				break;
			default:
				Calendar cal = Calendar.getInstance();
				cal.setTime(new Date());
				datetimeUtil.setMonth(cal.get(Calendar.MONTH));
				dateFrom = datetimeUtil.getThisMonthStartDate();
				dateTo = datetimeUtil.getThisMonthEndDate();
				break;

		}
		Map<DateRangeType, Calendar> dateRangeMap = new HashMap<>();
		dateRangeMap.put(DateRangeType.DATE_FROM, dateFrom);
		dateRangeMap.put(DateRangeType.DATE_TO, dateTo);
		return dateRangeMap;
	}

	public static Set<Date> getMonthBetween(Date fromDate, Date toDate) throws ParseException {
		Set<Date> dates = new TreeSet<>();
		Calendar min = Calendar.getInstance();
		Calendar max = Calendar.getInstance();

		min.setTime(fromDate);
		min.set(min.get(Calendar.YEAR), min.get(Calendar.MONTH), 1);

		max.setTime(toDate);
		max.set(max.get(Calendar.YEAR), max.get(Calendar.MONTH), 2);

		while (min.before(max)) {
			dates.add(min.getTime());
			min.add(Calendar.MONTH, 1);
		}
		return dates;
	}

	public static List<Date> getWeeksBetween(Date startDate, Date endDate) {
		List<Date> weeksInRange = new ArrayList<>();
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(startDate);
		calendar.setFirstDayOfWeek(Calendar.SATURDAY);
		calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());

		Date weekLastDay = getWeekDayEnd(calendar.getTime());
		if (!weekLastDay.equals(startDate)) weeksInRange.add(calendar.getTime());

		Calendar endCalendar = new GregorianCalendar();
		endCalendar.setTime(endDate);

		int maxWeekNumber = calendar.getActualMaximum(Calendar.WEEK_OF_MONTH);
		for (int i = 0; i < maxWeekNumber; i++) {
			// Set the calendar to monday of the current week
			calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);

			// Print dates of the current week starting on Monday
			DateFormat df = new SimpleDateFormat("EEE dd/MM/yyyy");
			for (int j = 0; j < 7; j++) {
				calendar.add(Calendar.DATE, 1);
			}
			Date result = calendar.getTime();
			Date weekEndDate = getWeekDayEnd(result);
			if (result.before(endDate) && (weekEndDate.after(endDate) || weekEndDate.before(endDate)))
				weeksInRange.add(result);
		}
		return weeksInRange;
	}

	public static Date getWeekDayEnd(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.DAY_OF_MONTH, 7);
		calendar.setTime(getDayStart(calendar.getTime()));
		calendar.add(Calendar.SECOND, -2);
		return calendar.getTime();
	}

	public static boolean isInCurrentMonthYear(Date date) {
		Calendar cal1 = Calendar.getInstance();
		Calendar cal2 = Calendar.getInstance();
		cal1.setTime(new Date());
		cal2.setTime(date);
		return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) && cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH);
	}

	public static boolean isPast(Date date) {
		return date.before(new Date());
	}
//    public static String calculateAge(St){
//        LocalDate birthdate = new LocalDate(1970, 1, 20);
//        LocalDate now = new LocalDate();
//        Years age = Years.yearsBetween(birthdate, now);
//    }

	public enum DateRangeType {
		DATE_FROM("date_from"), DATE_TO("date_to");

		private final String value;


		DateRangeType(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}

	}

	public enum Periods {
		TODAY, THIS_WEEK, LAST_WEEK, THIS_MONTH, LAST_MONTH, THIS_YEAR, LAST_YEAR, ALL_TIME
	}

	public enum Months {
		JANUARY, FEBRUARY, MARCH, APRIL, MAY, JUNE, JULY, AUGUST, SEPTEMBER, OCTOBER, NOVEMBER, DECEMBER
	}

	public static Periods getPeriod(String period) {
		switch (period) {
			case "today":
				return Periods.TODAY;
			case "this_week":
				return Periods.THIS_WEEK;
			case "last_week":
				return Periods.LAST_WEEK;
			case "this_month":
				return Periods.THIS_MONTH;
			case "last_month":
				return Periods.LAST_MONTH;
			case "this_year":
				return Periods.THIS_YEAR;
			case "last_year":
				return Periods.LAST_YEAR;
			default:
				return Periods.ALL_TIME;
		}
	}

}

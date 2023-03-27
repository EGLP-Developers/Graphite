package me.eglp.gv2.util.stats.element;

import me.eglp.gv2.util.webinterface.js.JavaScriptEnum;
import me.eglp.gv2.util.webinterface.js.WebinterfaceObject;
import me.mrletsplay.mrcore.json.converter.JSONPrimitiveStringConvertible;

// Preset timeframes to avoid weird issues when allowing custom timeframes
@JavaScriptEnum
public enum StatisticsElementTimeframe implements WebinterfaceObject, JSONPrimitiveStringConvertible {
	
	ONE_DAY(24L * 60 * 60 * 1000, 24, 1, -1, -1),
	TWO_DAYS(48L * 60 * 60 * 1000, 48, 2, -1, -1),
	ONE_WEEK(7L * 24 * 60 * 60 * 1000, 7 * 24, 7, 1, -1),
	TWO_WEEKS(14L * 24 * 60 * 60 * 1000, 14 * 24, 14, 2, -1),
	ONE_MONTH(30L * 24 * 60 * 60 * 1000, 30 * 24, 30, 4, 1),
	TWO_MONTHS(60L * 24 * 60 * 60 * 1000, 60 * 24, 60, 8, 2),
	HALF_YEAR(182L * 24 * 60 * 60 * 1000, 182 * 24, 182, 26, 6),
	ONE_YEAR(365L * 24 * 60 * 60 * 1000, 365 * 24, 365, 52, 12);
	
	private long timeframe;
	private int
		hours,
		days,
		weeks,
		months;
	
	private StatisticsElementTimeframe(long timeframe, int hours, int days, int weeks, int months) {
		this.timeframe = timeframe;
		this.hours = hours;
		this.days = days;
		this.weeks = weeks;
		this.months = months;
	}
	
	public long getRawTimeframe() {
		return timeframe;
	}
	
	public int getHours() {
		return hours;
	}
	
	public int getDays() {
		return days;
	}
	
	public int getWeeks() {
		return weeks;
	}
	
	public int getMonths() {
		return months;
	}
	
	public int getAmount(StatisticsElementPointFrequency frequency) {
		switch(frequency) {
			case HOURLY:
				return hours;
			case DAILY:
				return days;
			case WEEKLY:
				return weeks;
			case MONTHLY:
				return months;
			default:
				throw new UnsupportedOperationException("Not implemented");
		}
	}
	
	@Override
	public String toJSONPrimitive() {
		return name();
	}
	
	public static StatisticsElementTimeframe decodePrimitive(Object p) {
		return valueOf((String) p);
	}

}

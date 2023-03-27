package me.eglp.gv2.util.lang;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.eglp.gv2.util.base.GraphiteLocalizable;

public class GraphiteTimeParser {

	private static final Pattern
		FULL_DURATION_PATTERN = Pattern.compile("((\\d+) (.+?))+"),
		DURATION_PATTERN = Pattern.compile("(\\d+) (.+?)(?= +?(?:\\d+) (?:.+?)|$)"),
		TIMESTAMP_PATTERN = Pattern.compile("(?:(?<hours>(?:\\d){1,3}):)?(?<minutes>(?:\\d){1,2}):(?<seconds>(?:\\d){1,2})"),
		SHORT_DURATION_PATTERN = Pattern.compile("(?:(?<weeks>\\d{1,2})w)?(?:(?<days>\\d{1,2})d)?(?:(?<hours>\\d{1,2})h)?(?:(?<minutes>\\d{1,2})m)?");
	
	public static long parseDuration(GraphiteLocalizable localizable, String str) {
		if(!FULL_DURATION_PATTERN.matcher(str).matches()) return -1;
		Matcher m = DURATION_PATTERN.matcher(str);
		long duration = 0;
		while(m.find()) {
			long dur = Long.parseLong(m.group(1));
			String unit = m.group(2);
			LocalizedTimeUnit tU = LocalizedTimeUnit.getByName(localizable, unit);
			if(tU == null) return -1;
			duration += dur * tU.getTimeSeconds() * 1000;
		}
		return duration;
	}
	
	public static long parseTimestamp(String str) {
		Matcher m = TIMESTAMP_PATTERN.matcher(str);
		if(!m.matches()) return -1;
		long duration = 0;
		if(m.group("hours") != null) duration += Integer.parseInt(m.group("hours")) * 60 * 60 * 1000;
		duration += Integer.parseInt(m.group("minutes")) * 60 * 1000;
		duration += Integer.parseInt(m.group("seconds")) * 1000;
		return duration;
	}

	public static String getTimestamp(long millis) {
		long seconds = millis / 1000;
		long hours = Math.floorDiv(seconds, 3600);
		seconds = seconds - (hours * 3600);
		long mins = Math.floorDiv(seconds, 60);
		seconds = seconds - (mins * 60);
		return (hours == 0 ? "" : hours + ":") + String.format("%02d", mins) + ":" + String.format("%02d", seconds);
	}
	
	public static long parseShortDuration(String str) {
		if(str.isBlank()) return -1;
		Matcher m = SHORT_DURATION_PATTERN.matcher(str);
		if(!m.matches()) return -1;
		long duration = 0;
		if(m.group("weeks") != null) duration += (long) Integer.parseInt(m.group("weeks")) * 7 * 24 * 60 * 60 * 1000;
		if(m.group("days") != null) duration += (long) Integer.parseInt(m.group("days")) * 24 * 60 * 60 * 1000;
		if(m.group("hours") != null) duration += (long) Integer.parseInt(m.group("hours")) * 60 * 60 * 1000;
		if(m.group("minutes") != null) duration += (long) Integer.parseInt(m.group("minutes")) * 60 * 1000;
		return duration;
	}
	
}

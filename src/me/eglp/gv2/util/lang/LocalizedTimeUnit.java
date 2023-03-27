package me.eglp.gv2.util.lang;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import me.eglp.gv2.util.base.GraphiteLocalizable;

public enum LocalizedTimeUnit {

	MONTHS(DefaultLocaleString.TIME_UNIT_MONTHS, 30 * 24 * 60 * 60),
	WEEKS(DefaultLocaleString.TIME_UNIT_WEEKS, 7 * 24 * 60 * 60),
	DAYS(DefaultLocaleString.TIME_UNIT_DAYS, 24 * 60 * 60),
	HOURS(DefaultLocaleString.TIME_UNIT_HOURS, 60 * 60),
	MINUTES(DefaultLocaleString.TIME_UNIT_MINUTES, 60),
	SECONDS(DefaultLocaleString.TIME_UNIT_SECONDS, 1);
	
	private final LocalizedString localizedName;
	private final long timeSeconds;
	
	private LocalizedTimeUnit(LocalizedString localizedName, long timeSeconds) {
		this.localizedName = localizedName;
		this.timeSeconds = timeSeconds;
	}
	
	public LocalizedString getLocalizedName() {
		return localizedName;
	}
	
	public long getTimeSeconds() {
		return timeSeconds;
	}
	
	public boolean nameEquals(GraphiteLocalizable localizable, String str) {
		String locN = localizedName.getFor(localizable);
		if(locN.replace("(", "").replace(")", "").equals(str)) return true;
		String withoutBrackets = locN.replaceAll("\\(.+\\)", "");
		return withoutBrackets.equalsIgnoreCase(str);
	}
	
	public static LocalizedTimeUnit getByName(GraphiteLocalizable localizable, String name) {
		return Arrays.stream(values())
				.filter(t -> t.nameEquals(localizable, name))
				.findFirst().orElse(null);
	}
	
	public static String formatTime(GraphiteLocalizable localizable, long durationMs) {
		long durSecs = durationMs / 1000;
		if(durSecs <= 1) {
			return "< 1 " + SECONDS.getLocalizedName().getFor(localizable);
		}
		List<String> fStrs = new ArrayList<>();
		for(LocalizedTimeUnit u : values()) {
			if(durSecs >= u.timeSeconds) {
				long d = Math.floorDiv(durSecs, u.timeSeconds);
				fStrs.add(d + " " + u.getLocalizedName().getFor(localizable));
				durSecs -= d * u.timeSeconds;
			}
		}
		return fStrs.stream().collect(Collectors.joining(" "));
	}
	
}

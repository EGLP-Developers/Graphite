package me.eglp.gv2.util.stats.element;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.util.webinterface.js.JavaScriptEnum;
import me.eglp.gv2.util.webinterface.js.WebinterfaceObject;
import me.mrletsplay.mrcore.json.converter.JSONPrimitiveStringConvertible;

//Preset bar/point frequencies (for counts) to avoid weird issues when allowing custom frequencies
@JavaScriptEnum
public enum StatisticsElementPointFrequency implements WebinterfaceObject, JSONPrimitiveStringConvertible {

	HOURLY(60L * 60 * 1000, ChronoUnit.HOURS),
	DAILY(24L * 60 * 60 * 1000, ChronoUnit.DAYS),
	WEEKLY(7L * 24 * 60 * 60 * 1000, ChronoUnit.WEEKS),
	MONTHLY(30L * 24 * 60 * 60 * 1000, ChronoUnit.MONTHS);

	private long timeframe;
	private ChronoUnit chronoUnit;

	private StatisticsElementPointFrequency(long timeframe, ChronoUnit chronoUnit) {
		this.timeframe = timeframe;
		this.chronoUnit = chronoUnit;
	}

	public long getRawTimeframe() {
		return timeframe;
	}

	public ChronoUnit getChronoUnit() {
		return chronoUnit;
	}

	@Override
	public String toJSONPrimitive() {
		return name();
	}

	public long getCorrespondingUTCTimestamp(GraphiteGuild guild, long timestamp) {
		return getCorrespondingTime(guild, timestamp).toInstant().toEpochMilli();
	}

	private ZonedDateTime getCorrespondingTime(GraphiteGuild guild, long timestamp) {
		ZonedDateTime i = Instant.ofEpochMilli(timestamp).atZone(guild.getConfig().getTimezone());
		switch(this) {
			case HOURLY:
				return i.truncatedTo(ChronoUnit.HOURS);
			case DAILY:
				return i.truncatedTo(ChronoUnit.DAYS);
			case MONTHLY:
				return i.truncatedTo(ChronoUnit.DAYS).with(TemporalAdjusters.firstDayOfMonth());
			case WEEKLY:
				return i.truncatedTo(ChronoUnit.DAYS).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
			default:
				throw new UnsupportedOperationException("Not implemented");
		}
	}

	public List<Long> getUTCTimestampsBetween(GraphiteGuild guild, long startTimestamp, long endTimestamp) {
		ZonedDateTime end = Instant.ofEpochMilli(endTimestamp).atZone(guild.getConfig().getTimezone());

		List<Long> ts = new ArrayList<>();
		ZonedDateTime t = getCorrespondingTime(guild, startTimestamp);
		while(t.isBefore(end)) {
			ts.add(t.toInstant().toEpochMilli());
			t = t.plus(1, chronoUnit);
		}

		return ts;
	}

	public static StatisticsElementPointFrequency decodePrimitive(String p) {
		return valueOf(p);
	}

}

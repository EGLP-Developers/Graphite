package me.eglp.gv2.util.base.guild.reminder;

import java.time.Period;

/**
 * Describes a repetition interval for Reminders
 * 
 * @author The Arrayser
 * @date Mon Mar 27 19:49:22 2023
 */
public enum ReminderRepetition {
	
	YEARLY("Yearly", Period.ofYears(1)),
	MONTHLY("Monthly", Period.ofMonths(1)),
	WEEKLY("Weekly", Period.ofWeeks(1)),
	DAILY("Daily", Period.ofDays(1));

	private String friendlyName;
	private Period period;

	private ReminderRepetition(String friendlyName, Period period) {
		this.friendlyName = friendlyName;
		this.period = period;
	}

	public String getFriendlyName() {
		return friendlyName;
	}

	public Period getPeriod() {
		return period;
	}

}

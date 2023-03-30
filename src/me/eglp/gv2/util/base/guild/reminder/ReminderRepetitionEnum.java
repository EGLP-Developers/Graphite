package me.eglp.gv2.util.base.guild.reminder;

/**
 * This is a reminders helper Enum, to signal, how long to wait, between
 * repetitions of the reminder.
 * 
 * @author The Arrayser
 * @date Mon Mar 27 19:49:22 2023
 */

public enum ReminderRepetitionEnum {
	YEARLY("Yearly", 1, 0, 0, 0), MONTHLY("Monthly", 0, 1, 0, 0), WEEKLY("Weekly", 0, 0, 1, 0),
	DAILY("Daily", 0, 0, 0, 1);

	private String friendlyName;
	private long years;
	private long months;
	private long weeks;
	private long days;

	ReminderRepetitionEnum(String friendlyName, long years, long months, long weeks, long days) {
		this.friendlyName = friendlyName;
		this.years = years;
		this.months = months;
		this.weeks = weeks;
		this.days = days;
	}

	public String getFriendlyName() {
		return friendlyName;
	}

	public long getYearsDisplacement() {
		return years;
	}

	public long getMonthsDisplacement() {
		return months;
	}

	public long getWeeksDisplacement() {
		return weeks;
	}

	public long getDaysDisplacement() {
		return days;
	}

}

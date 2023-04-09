package me.eglp.gv2.util.base.guild.reminder;

/**
 * Describes a repetition interval for Reminders
 * 
 * @author The Arrayser
 * @date Mon Mar 27 19:49:22 2023
 */
public enum ReminderRepetition {
	YEARLY("Yearly", 1, 0, 0, 0),
	MONTHLY("Monthly", 0, 1, 0, 0),
	WEEKLY("Weekly", 0, 0, 1, 0),
	DAILY("Daily", 0, 0, 0, 1);

	private String friendlyName;
	private long years;
	private long months;
	private long weeks;
	private long days;

	ReminderRepetition(String friendlyName, long years, long months, long weeks, long days) {
		this.friendlyName = friendlyName;
		this.years = years;
		this.months = months;
		this.weeks = weeks;
		this.days = days;
	}

	public String getFriendlyName() {
		return friendlyName;
	}

	public long getYears() {
		return years;
	}

	public long getMonths() {
		return months;
	}

	public long getWeeks() {
		return weeks;
	}

	public long getDays() {
		return days;
	}

}

package me.eglp.gv2.util.scripting.object;

import me.eglp.gv2.util.base.guild.GuildReport;

public class JSReport {
	
	protected GuildReport report;
	
	public JSReport(GuildReport report) {
		this.report = report;
	}
	
	/**
	 * Returns this report's unique id
	 * @return This report's unique id
	 */
	public String getID() {
		return report.getID();
	}
	
	/**
	 * Returns the reporter (the user that reported another user)
	 * @return The reporter
	 * @see #getReported()
	 */
	public JSUser getReporter() {
		return new JSUser(report.getReporter());
	}

	/**
	 * Returns the reported user
	 * @return The reported user
	 * @see #getReported()
	 */
	public JSUser getReported() {
		return new JSUser(report.getReported());
	}
	
	/**
	 * Returns the reason the reporter provided for this report
	 * @return The reason the reporter provided for this report
	 */
	public String getReason() {
		return report.getReason();
	}
	
	/**
	 * Returns the timestamp of this report as an epoch millisecond
	 * @return The timestamp of this report
	 */
	public long getTimestamp() {
		return report.getTimestamp();
	}
	
	@Override
	public String toString() {
		return "[JS Report: " + getID() + "]";
	}
	
}

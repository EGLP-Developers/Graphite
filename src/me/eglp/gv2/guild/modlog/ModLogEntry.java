package me.eglp.gv2.guild.modlog;

import me.eglp.gv2.util.webinterface.js.JavaScriptClass;
import me.eglp.gv2.util.webinterface.js.JavaScriptConstructor;
import me.eglp.gv2.util.webinterface.js.JavaScriptFunction;
import me.eglp.gv2.util.webinterface.js.JavaScriptValue;
import me.eglp.gv2.util.webinterface.js.WebinterfaceObject;

@JavaScriptClass(name = "ModLog")
public class ModLogEntry implements WebinterfaceObject {

	@JavaScriptValue(getter = "getType")
	private ModLogEntryType type;

	@JavaScriptValue(getter = "getTimestamp")
	private long timestamp;

	@JavaScriptValue(getter = "getActionDuration")
	private long actionDuration;

	@JavaScriptValue(getter = "getMemberID")
	private String memberID;

	@JavaScriptValue(getter = "getModeratorID")
	private String moderatorID;

	@JavaScriptValue(getter = "getReason")
	private String reason;

	@JavaScriptConstructor
	public ModLogEntry() {}

	public ModLogEntry(ModLogEntryType type, long timestamp, long actionDuration, String memberID, String moderatorID, String reason) {
		this.type = type;
		this.timestamp = timestamp;
		this.actionDuration = actionDuration;
		this.memberID = memberID;
		this.moderatorID = moderatorID;
		this.reason = reason;
	}

	public ModLogEntry(ModLogEntryType type, long actionDuration, String memberID, String moderatorID, String reason) {
		this.type = type;
		this.timestamp = System.currentTimeMillis();
		this.actionDuration = actionDuration;
		this.memberID = memberID;
		this.moderatorID = moderatorID;
		this.reason = reason;
	}

	public ModLogEntryType getType() {
		return type;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public long getActionDuration() {
		return actionDuration;
	}

	public String getMemberID() {
		return memberID;
	}

	public String getModeratorID() {
		return moderatorID;
	}

	public String getReason() {
		return reason;
	}

	@JavaScriptFunction(calling = "getModLogEntries", returning = "entries", withGuild = true)
	public static void getModLogEntries() {};

}

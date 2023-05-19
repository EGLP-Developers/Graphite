package me.eglp.gv2.guild.modlog;

import me.eglp.gv2.util.webinterface.js.JavaScriptEnum;
import me.eglp.gv2.util.webinterface.js.JavaScriptValue;
import me.eglp.gv2.util.webinterface.js.WebinterfaceObject;

@JavaScriptEnum
public enum ModLogEntryType implements WebinterfaceObject{

	TEMP_VOICEMUTE("Temporary voice mute", "temporarily voice muted"),
	VOICEUNMUTE("Voice unmute", "voice unmuted"),
	CHATMUTE("Chat mute", "chat muted"),
	TEMP_CHATMUTE("Temporary chat mute", "temporarily chat muted"),
	CHATUNMUTE("Chat unmute", "chat unmuted"),
	JAIL("Jail", "jailed"),
	TEMP_JAIL("Temporary jail", "temporarily jailed"),
	UNJAIL("Unjail", "unjailed"),
	KICK("Kick", "kicked"),
	BAN("Ban", "banned"),
	TEMP_BAN("Temporary ban", "temporarily banned"),
	UNBAN("Unban", "unbanned"),
	WARNING("Warning", "warned"),
	CLEAR("Clear", "cleared some chat history"),
	CLEAR_ALL("Clear all", "cleared all chat history"),
	;

	@JavaScriptValue(getter = "getFriendlyName")
	private String friendlyName;

	@JavaScriptValue(getter = "getFriendlyAction")
	private String friendlyAction;

	private ModLogEntryType(String friendlyName, String friendlyAction) {
		this.friendlyName = friendlyName;
		this.friendlyAction = friendlyAction;
	}

	public String getFriendlyName() {
		return friendlyName;
	}

	public String getFriendlyAction() {
		return friendlyAction;
	}

}

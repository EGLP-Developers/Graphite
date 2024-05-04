package me.eglp.gv2.guild.automod.autoactions;

import me.eglp.gv2.util.webinterface.js.JavaScriptEnum;
import me.eglp.gv2.util.webinterface.js.JavaScriptValue;
import me.eglp.gv2.util.webinterface.js.WebinterfaceObject;
import me.mrletsplay.mrcore.json.converter.JSONPrimitiveStringConvertible;

@JavaScriptEnum
public enum AutoModPunishment implements JSONPrimitiveStringConvertible, WebinterfaceObject {

	CHATMUTE("Chat mute", false),
	TEMP_CHATMUTE("Temporary chat mute", true),
	TEMP_BAN("Temporary ban", true),
	KICK("Kick", false),
	BAN("Ban", false);

	@JavaScriptValue(getter = "getFriendlyName")
	private String friendlyName;

	@JavaScriptValue(getter = "isRequiresDuration")
	private boolean requiresDuration;

	private AutoModPunishment(String friendlyName, boolean requiresDuration) {
		this.friendlyName = friendlyName;
		this.requiresDuration = requiresDuration;
	}

	public boolean requiresDuration() {
		return requiresDuration;
	}

	@Override
	public String toJSONPrimitive() {
		return name();
	}

	public static AutoModPunishment decodePrimitive(String p) {
		return valueOf(p);
	}

}

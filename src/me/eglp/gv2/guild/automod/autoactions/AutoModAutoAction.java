package me.eglp.gv2.guild.automod.autoactions;

import me.eglp.gv2.util.webinterface.js.JavaScriptClass;
import me.eglp.gv2.util.webinterface.js.JavaScriptConstructor;
import me.eglp.gv2.util.webinterface.js.JavaScriptFunction;
import me.eglp.gv2.util.webinterface.js.JavaScriptParameter;
import me.eglp.gv2.util.webinterface.js.JavaScriptValue;
import me.eglp.gv2.util.webinterface.js.WebinterfaceObject;
import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONConvertible;
import me.mrletsplay.mrcore.json.converter.JSONValue;

@JavaScriptClass(name = "AutoAction")
public class AutoModAutoAction implements WebinterfaceObject, JSONConvertible{

	@JSONValue
	@JavaScriptValue(getter = "getPunishment")
	private AutoModPunishment punishment;

	@JSONValue
	@JavaScriptValue(getter = "getPunishmentDuration")
	private long punishmentDuration;

	@JSONValue
	@JavaScriptValue(getter = "getMinCount")
	private int minCount;

	@JSONValue
	@JavaScriptValue(getter = "getTimeframe")
	private long timeframe;

	@JSONConstructor
	@JavaScriptConstructor
	public AutoModAutoAction() {}

	public AutoModAutoAction(AutoModPunishment punishment, long punishmentDuration, int minCount, long timeframe) {
		this.punishment = punishment;
		this.punishmentDuration = punishmentDuration;
		this.minCount = minCount;
		this.timeframe = timeframe;
	}

	public AutoModPunishment getPunishment() {
		return punishment;
	}

	public long getPunishmentDuration() {
		return punishmentDuration;
	}

	public int getMinCount() {
		return minCount;
	}

	public long getTimeframe() {
		return timeframe;
	}

	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof AutoModAutoAction)) return false;
		AutoModAutoAction o = (AutoModAutoAction) obj;
		return o.punishment == punishment &&
				o.punishmentDuration == punishmentDuration &&
				o.minCount == minCount &&
				o.timeframe == timeframe;
	}

	@JavaScriptFunction(calling = "getAutoActions", returning = "autoactions", withGuild = true)
	public static void getAutoActions() {};

	@JavaScriptFunction(calling = "addAutoAction", returning = "action", withGuild = true)
	public static void addAutoAction(@JavaScriptParameter(name = "punishment") String punishment, @JavaScriptParameter(name = "duration") String duration, @JavaScriptParameter(name = "infractions") int minInfractions, @JavaScriptParameter(name = "timeframe") String timeframe) {};

	@JavaScriptFunction(calling = "removeAutoAction", withGuild = true)
	public static void removeAutoAction(@JavaScriptParameter(name = "object") JSONObject obj) {}

}

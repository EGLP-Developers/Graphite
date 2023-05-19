package me.eglp.gv2.guild.automod.excessive_spoilers;

import me.eglp.gv2.guild.automod.AbstractAutoModSettings;
import me.eglp.gv2.util.webinterface.js.JavaScriptClass;
import me.eglp.gv2.util.webinterface.js.JavaScriptConstructor;
import me.eglp.gv2.util.webinterface.js.JavaScriptFunction;
import me.eglp.gv2.util.webinterface.js.JavaScriptValue;
import me.eglp.gv2.util.webinterface.js.WebinterfaceObject;
import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONValue;

@JavaScriptClass
public class ExcessiveSpoilersSettings extends AbstractAutoModSettings implements WebinterfaceObject {

	public static final String TYPE = "excessive_spoilers";

	@JSONValue
	@JavaScriptValue(getter = "getMaxSpoilers", setter = "setMaxSpoilers")
	private int maxSpoilers;

	@JSONConstructor
	@JavaScriptConstructor
	public ExcessiveSpoilersSettings() {
		super(TYPE, "Excessive Spoilers");
		this.maxSpoilers = 3;
	}

	@Override
	public String getWarnReason() {
		return "Excessive spoiler usage";
	}

	public void setMaxSpoilers(int maxSpoilers) {
		this.maxSpoilers = maxSpoilers;
	}

	public int getMaxSpoilers() {
		return maxSpoilers;
	}

	@JavaScriptFunction(calling = "getExcessiveSpoilersSettings", returning = "settings", withGuild = true)
	private static void getExcessiveSpoilersSettings() {}

}

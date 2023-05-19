package me.eglp.gv2.guild.automod.excessive_caps;

import me.eglp.gv2.guild.automod.AbstractAutoModSettings;
import me.eglp.gv2.util.webinterface.js.JavaScriptClass;
import me.eglp.gv2.util.webinterface.js.JavaScriptConstructor;
import me.eglp.gv2.util.webinterface.js.JavaScriptFunction;
import me.eglp.gv2.util.webinterface.js.JavaScriptValue;
import me.eglp.gv2.util.webinterface.js.WebinterfaceObject;
import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONValue;

@JavaScriptClass
public class ExcessiveCapsSettings extends AbstractAutoModSettings implements WebinterfaceObject {

	public static final String TYPE = "excessive_caps";

	@JSONValue
	@JavaScriptValue(getter = "getMinCapsPercent", setter = "setMaxCapsPercent")
	private int maxCapsPercent;

	@JSONValue
	@JavaScriptValue(getter = "getMinTextLength", setter = "setMinTextLength")
	private int minTextLength;

	@JSONConstructor
	@JavaScriptConstructor
	public ExcessiveCapsSettings() {
		super(TYPE, "Excessive Caps");
		this.maxCapsPercent = 70;
		this.minTextLength = 10;
	}

	@Override
	public String getWarnReason() {
		return "EXCESSIVE CAPS USAGE";
	}

	public void setMaxCapsPercent(int maxCapsPercent) {
		this.maxCapsPercent = maxCapsPercent;
	}

	public int getMaxCapsPercent() {
		return maxCapsPercent;
	}

	public void setMinTextLength(int minTextLength) {
		this.minTextLength = minTextLength;
	}

	public int getMinTextLength() {
		return minTextLength;
	}

	@JavaScriptFunction(calling = "getExcessiveCapsSettings", returning = "settings", withGuild = true)
	private static void getExcessiveCapsSettings() {}

}

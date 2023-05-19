package me.eglp.gv2.guild.automod.repeated_text;

import me.eglp.gv2.guild.automod.AbstractAutoModSettings;
import me.eglp.gv2.util.webinterface.js.JavaScriptClass;
import me.eglp.gv2.util.webinterface.js.JavaScriptConstructor;
import me.eglp.gv2.util.webinterface.js.JavaScriptFunction;
import me.eglp.gv2.util.webinterface.js.JavaScriptValue;
import me.eglp.gv2.util.webinterface.js.WebinterfaceObject;
import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONValue;

@JavaScriptClass
public class RepeatedTextSettings extends AbstractAutoModSettings implements WebinterfaceObject {

	public static final String TYPE = "repeated_text";

	@JSONValue
	@JavaScriptValue(getter = "getMaxRepeats", setter = "setMaxRepeats")
	private int maxRepeats;

	@JSONValue
	@JavaScriptValue(getter = "getMinTextLength", setter = "setMinTextLength")
	private int minTextLength;

	@JSONConstructor
	@JavaScriptConstructor
	public RepeatedTextSettings() {
		super(TYPE, "Repeated Text");
		this.maxRepeats = 2;
		this.minTextLength = 10;
	}

	@Override
	public String getWarnReason() {
		return "Send repeating text";
	}

	public void setMaxRepeats(int maxRepeats) {
		this.maxRepeats = maxRepeats;
	}

	public int getMaxRepeats() {
		return maxRepeats;
	}

	public void setMinTextLength(int minTextLength) {
		this.minTextLength = minTextLength;
	}

	public int getMinTextLength() {
		return minTextLength;
	}

	@JavaScriptFunction(calling = "getRepeatedTextSettings", returning = "settings", withGuild = true)
	private static void getRepeatedTextSettings() {}

}

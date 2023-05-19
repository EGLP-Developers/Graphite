package me.eglp.gv2.guild.automod.excessive_mentions;

import me.eglp.gv2.guild.automod.AbstractAutoModSettings;
import me.eglp.gv2.util.webinterface.js.JavaScriptClass;
import me.eglp.gv2.util.webinterface.js.JavaScriptConstructor;
import me.eglp.gv2.util.webinterface.js.JavaScriptFunction;
import me.eglp.gv2.util.webinterface.js.JavaScriptValue;
import me.eglp.gv2.util.webinterface.js.WebinterfaceObject;
import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONValue;

@JavaScriptClass
public class ExcessiveMentionsSettings extends AbstractAutoModSettings implements WebinterfaceObject {

	public static final String TYPE = "excessive_mentions";

	@JSONValue
	@JavaScriptValue(getter = "getMaxMentions", setter = "setMaxMentions")
	private int maxMentions;

	@JSONConstructor
	@JavaScriptConstructor
	public ExcessiveMentionsSettings() {
		super(TYPE, "Excessive Mentions");
		this.maxMentions = 5;
	}

	@Override
	public String getWarnReason() {
		return "Excessive mention usage";
	}

	public void setMaxMentions(int maxMentions) {
		this.maxMentions = maxMentions;
	}

	public int getMaxMentions() {
		return maxMentions;
	}

	@JavaScriptFunction(calling = "getExcessiveMentionsSettings", returning = "settings", withGuild = true)
	private static void getExcessiveMentionsSettings() {}

}

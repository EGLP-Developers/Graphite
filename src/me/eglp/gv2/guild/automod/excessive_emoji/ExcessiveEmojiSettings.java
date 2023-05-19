package me.eglp.gv2.guild.automod.excessive_emoji;

import me.eglp.gv2.guild.automod.AbstractAutoModSettings;
import me.eglp.gv2.util.webinterface.js.JavaScriptClass;
import me.eglp.gv2.util.webinterface.js.JavaScriptConstructor;
import me.eglp.gv2.util.webinterface.js.JavaScriptFunction;
import me.eglp.gv2.util.webinterface.js.JavaScriptValue;
import me.eglp.gv2.util.webinterface.js.WebinterfaceObject;
import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONValue;

@JavaScriptClass
public class ExcessiveEmojiSettings extends AbstractAutoModSettings implements WebinterfaceObject {

	public static final String TYPE = "excessive_emoji";

	@JSONValue
	@JavaScriptValue(getter = "getMaxEmojis", setter = "setMaxEmojis")
	private int maxEmojis;

	@JSONConstructor
	@JavaScriptConstructor
	public ExcessiveEmojiSettings() {
		super(TYPE, "Excessive Emoji");
		this.maxEmojis = 5;
	}

	@Override
	public String getWarnReason() {
		return "Excessive :regional_indicator_e: :regional_indicator_m: :regional_indicator_o: :regional_indicator_j: :regional_indicator_i: usage";
	}

	public void setMaxEmojis(int maxEmojis) {
		this.maxEmojis = maxEmojis;
	}

	public int getMaxEmojis() {
		return maxEmojis;
	}

	@JavaScriptFunction(calling = "getExcessiveEmojiSettings", returning = "settings", withGuild = true)
	private static void getExcessiveEmojiSettings() {}

}

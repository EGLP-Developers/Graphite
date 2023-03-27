package me.eglp.gv2.util.base.guild.automod.zalgo;

import me.eglp.gv2.util.base.guild.automod.AbstractAutoModSettings;
import me.eglp.gv2.util.webinterface.js.JavaScriptClass;
import me.eglp.gv2.util.webinterface.js.JavaScriptConstructor;
import me.eglp.gv2.util.webinterface.js.JavaScriptFunction;
import me.eglp.gv2.util.webinterface.js.JavaScriptParameter;
import me.eglp.gv2.util.webinterface.js.WebinterfaceObject;
import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.mrcore.json.converter.JSONConstructor;

@JavaScriptClass
public class ZalgoSettings extends AbstractAutoModSettings implements WebinterfaceObject {
	
	public static final String TYPE = "zalgo";
	
	@JSONConstructor
	@JavaScriptConstructor
	public ZalgoSettings() {
		super(TYPE, "Zalgo");
	}
	
	@Override
	public String getWarnReason() {
		return "Send zalgo text";
	}
	
	@JavaScriptFunction(calling = "getZalgoSettings", returning = "settings", withGuild = true)
	private static void getZalgoSettings() {}

	@JavaScriptFunction(calling = "setZalgoSettings", withGuild = true)
	private static void setZalgoSettings(@JavaScriptParameter(name = "object") JSONObject action) {}

}

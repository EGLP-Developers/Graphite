package me.eglp.gv2.guild.automod.external_links;

import java.util.ArrayList;
import java.util.List;

import me.eglp.gv2.guild.automod.AbstractAutoModSettings;
import me.eglp.gv2.util.webinterface.js.JavaScriptClass;
import me.eglp.gv2.util.webinterface.js.JavaScriptConstructor;
import me.eglp.gv2.util.webinterface.js.JavaScriptFunction;
import me.eglp.gv2.util.webinterface.js.JavaScriptValue;
import me.eglp.gv2.util.webinterface.js.WebinterfaceObject;
import me.mrletsplay.mrcore.json.JSONType;
import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONListType;
import me.mrletsplay.mrcore.json.converter.JSONValue;

@JavaScriptClass
public class ExternalLinksSettings extends AbstractAutoModSettings implements WebinterfaceObject {

	public static final String TYPE = "external_links";

	@JSONValue
	@JSONListType(JSONType.STRING)
	@JavaScriptValue(getter = "getAllowedLinks", setter = "setAllowedLinks")
	private List<String> allowedLinks;

	@JSONConstructor
	@JavaScriptConstructor
	public ExternalLinksSettings() {
		super(TYPE, "External Links");
		this.allowedLinks = new ArrayList<>();
	}

	@Override
	public String getWarnReason() {
		return "Send external links";
	}

	public void setAllowedLinks(List<String> allowedLinks) {
		this.allowedLinks = allowedLinks;
	}

	public List<String> getAllowedLinks() {
		return allowedLinks;
	}

	@JavaScriptFunction(calling = "getExternalLinksSettings", returning = "settings", withGuild = true)
	private static void getExternalLinksSettings() {}

}

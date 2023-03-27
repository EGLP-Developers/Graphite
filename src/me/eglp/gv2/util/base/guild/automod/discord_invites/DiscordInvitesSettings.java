package me.eglp.gv2.util.base.guild.automod.discord_invites;

import java.util.ArrayList;
import java.util.List;

import me.eglp.gv2.util.base.guild.automod.AbstractAutoModSettings;
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
public class DiscordInvitesSettings extends AbstractAutoModSettings implements WebinterfaceObject {
	
	public static final String TYPE = "discord_invites";
	
	@JSONValue
	@JSONListType(JSONType.STRING)
	@JavaScriptValue(getter = "getAllowedInviteCodes", setter = "setAllowedInviteCodes")
	private List<String> allowedInviteCodes;
	
	@JSONConstructor
	@JavaScriptConstructor
	public DiscordInvitesSettings() {
		super(TYPE, "Discord Invites");
		this.allowedInviteCodes = new ArrayList<>();
	}
	
	@Override
	public String getWarnReason() {
		return "Send discord invite";
	}
	
	public void setAllowedInviteCodes(List<String> allowedInviteCodes) {
		this.allowedInviteCodes = allowedInviteCodes;
	}
	
	public List<String> getAllowedInviteCodes() {
		return allowedInviteCodes;
	}
	
	@JavaScriptFunction(calling = "getDiscordInvitesSettings", returning = "settings", withGuild = true)
	private static void getDiscordInvitesSettings() {}

}

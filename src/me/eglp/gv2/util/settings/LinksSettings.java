package me.eglp.gv2.util.settings;

import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONConvertible;
import me.mrletsplay.mrcore.json.converter.JSONValue;

public class LinksSettings implements JSONConvertible {
	
	@JSONValue
	private String
		patreon,
		discord;
	
	@JSONConstructor
	private LinksSettings() {}
	
	public String getDiscord() {
		return discord;
	}
	
	public String getPatreon() {
		return patreon;
	}
	
	public static LinksSettings createDefault() {
		LinksSettings s = new LinksSettings();
		s.patreon = "https://patreon.com/graphite_official";
		s.discord = "https://discord.gg/myFancyInviteLink";
		return s;
	}

}

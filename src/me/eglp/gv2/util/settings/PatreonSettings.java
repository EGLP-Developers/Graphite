package me.eglp.gv2.util.settings;

import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONConvertible;
import me.mrletsplay.mrcore.json.converter.JSONValue;

public class PatreonSettings implements JSONConvertible {
	
	@JSONValue
	private String
		clientID,
		clientSecret,
		campaignID;
	
	@JSONConstructor
	private PatreonSettings() {}
	
	public String getClientID() {
		return clientID;
	}
	
	public String getClientSecret() {
		return clientSecret;
	}
	
	public String getCampaignID() {
		return campaignID;
	}
	
	public static PatreonSettings createDefault() {
		PatreonSettings s = new PatreonSettings();
		s.clientID = "Patreon client ID";
		s.clientSecret = "Patreon client secret";
		s.campaignID = "Patreon campaign ID";
		return s;
	}
	
}

package me.eglp.gv2.util.settings;

import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONConvertible;
import me.mrletsplay.mrcore.json.converter.JSONValue;

public class TwitchSettings implements JSONConvertible {
	
	@JSONValue
	private String
		clientID,
		clientSecret;
	
	@JSONConstructor
	private TwitchSettings() {}
	
	public String getClientID() {
		return clientID;
	}
	
	public String getClientSecret() {
		return clientSecret;
	}
	
	public static TwitchSettings createDefault() {
		TwitchSettings s = new TwitchSettings();
		s.clientID = "Twitch client ID";
		s.clientSecret = "Twitch client secret";
		return s;
	}

}

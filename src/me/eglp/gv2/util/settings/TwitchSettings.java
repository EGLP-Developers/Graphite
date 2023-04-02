package me.eglp.gv2.util.settings;

import java.util.List;

import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONConvertible;
import me.mrletsplay.mrcore.json.converter.JSONValue;

public class TwitchSettings implements JSONConvertible {
	
	@JSONValue
	private boolean enable;
	
	@JSONValue
	private String
		clientID,
		clientSecret;
	
	@JSONConstructor
	private TwitchSettings() {}
	
	public boolean isEnabled() {
		return enable;
	}
	
	public String getClientID() {
		return clientID;
	}
	
	public String getClientSecret() {
		return clientSecret;
	}
	
	public void validate(List<String> errors) {
		if(!enable) return;
		if(clientID == null) errors.add("Twitch client id missing");
		if(clientSecret == null) errors.add("Twitch client secret missing");
	}
	
	public static TwitchSettings createDefault() {
		TwitchSettings s = new TwitchSettings();
		s.enable = false;
		s.clientID = "Twitch client ID";
		s.clientSecret = "Twitch client secret";
		return s;
	}

}

package me.eglp.gv2.util.settings;

import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONConvertible;
import me.mrletsplay.mrcore.json.converter.JSONValue;

public class SpotifySettings implements JSONConvertible {
	
	@JSONValue
	private boolean enable;
	
	@JSONValue
	private String
		clientID,
		clientSecret;
	
	@JSONConstructor
	private SpotifySettings() {}
	
	public boolean isEnabled() {
		return enable;
	}
	
	public String getClientID() {
		return clientID;
	}
	
	public String getClientSecret() {
		return clientSecret;
	}
	
	public static SpotifySettings createDefault() {
		SpotifySettings s = new SpotifySettings();
		s.enable = true;
		s.clientID = "Spotify client ID";
		s.clientSecret = "Spotify client secret";
		return s;
	}

}

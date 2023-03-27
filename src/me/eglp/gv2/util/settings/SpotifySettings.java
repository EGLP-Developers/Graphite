package me.eglp.gv2.util.settings;

import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONConvertible;
import me.mrletsplay.mrcore.json.converter.JSONValue;

public class SpotifySettings implements JSONConvertible {
	
	@JSONValue
	private String
		clientID,
		clientSecret;
	
	@JSONConstructor
	private SpotifySettings() {}
	
	public String getClientID() {
		return clientID;
	}
	
	public String getClientSecret() {
		return clientSecret;
	}
	
	public static SpotifySettings createDefault() {
		SpotifySettings s = new SpotifySettings();
		s.clientID = "Spotify client ID";
		s.clientSecret = "Spotify client secret";
		return s;
	}

}

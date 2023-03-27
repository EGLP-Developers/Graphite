package me.eglp.gv2.util.settings;

import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONConvertible;
import me.mrletsplay.mrcore.json.converter.JSONValue;

public class GeniusSettings implements JSONConvertible {
	
	@JSONValue
	private String accessToken;
	
	@JSONConstructor
	private GeniusSettings() {}
	
	public String getAccessToken() {
		return accessToken;
	}
	
	public static GeniusSettings createDefault() {
		GeniusSettings s = new GeniusSettings();
		s.accessToken = "Genius access token";
		return s;
	}

}

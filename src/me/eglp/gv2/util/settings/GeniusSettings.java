package me.eglp.gv2.util.settings;

import java.util.List;

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
	
	public void validate(List<String> errors) {
		if(accessToken == null) errors.add("Genius access token missing");
	}
	
	public static GeniusSettings createDefault() {
		GeniusSettings s = new GeniusSettings();
		s.accessToken = "Genius access token";
		return s;
	}

}

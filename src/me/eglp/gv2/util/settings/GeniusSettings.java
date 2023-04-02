package me.eglp.gv2.util.settings;

import java.util.List;

import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONConvertible;
import me.mrletsplay.mrcore.json.converter.JSONValue;

public class GeniusSettings implements JSONConvertible {
	
	@JSONValue
	private boolean enable;
	
	@JSONValue
	private String accessToken;
	
	@JSONConstructor
	private GeniusSettings() {}
	
	public boolean isEnabled() {
		return enable;
	}
	
	public String getAccessToken() {
		return accessToken;
	}
	
	public void validate(List<String> errors) {
		if(!enable) return;
		if(accessToken == null) errors.add("Genius access token missing");
	}
	
	public static GeniusSettings createDefault() {
		GeniusSettings s = new GeniusSettings();
		s.enable = false;
		s.accessToken = "Genius access token";
		return s;
	}

}

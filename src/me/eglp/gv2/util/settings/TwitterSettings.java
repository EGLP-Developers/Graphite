package me.eglp.gv2.util.settings;

import java.util.List;

import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONConvertible;
import me.mrletsplay.mrcore.json.converter.JSONValue;

public class TwitterSettings implements JSONConvertible {
	
	@JSONValue
	private String token;
	
	@JSONConstructor
	private TwitterSettings() {}
	
	public String getToken() {
		return token;
	}

	public void validate(List<String> errors) {
		if(token == null) errors.add("Twitter token missing");
	}
	
	public static TwitterSettings createDefault() {
		TwitterSettings s = new TwitterSettings();
		s.token = "Twitter OAuth token";
		return s;
	}

}

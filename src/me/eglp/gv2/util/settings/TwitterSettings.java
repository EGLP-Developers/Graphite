package me.eglp.gv2.util.settings;

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
	
	public static TwitterSettings createDefault() {
		TwitterSettings s = new TwitterSettings();
		s.token = "Twitter OAuth token";
		return s;
	}

}

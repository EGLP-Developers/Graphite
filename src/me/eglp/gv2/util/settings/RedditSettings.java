package me.eglp.gv2.util.settings;

import java.util.List;

import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONConvertible;
import me.mrletsplay.mrcore.json.converter.JSONValue;

public class RedditSettings implements JSONConvertible {
	
	@JSONValue
	private String
		clientID,
		clientSecret;
	
	@JSONConstructor
	private RedditSettings() {}
	
	public String getClientID() {
		return clientID;
	}
	
	public String getClientSecret() {
		return clientSecret;
	}
	
	public void validate(List<String> errors) {
		if(clientID == null) errors.add("Reddit client id missing");
		if(clientSecret == null) errors.add("Reddit client secret missing");
	}
	
	public static RedditSettings createDefault() {
		RedditSettings s = new RedditSettings();
		s.clientID = "Reddit client ID";
		s.clientSecret = "Reddit client secret";
		return s;
	}

}

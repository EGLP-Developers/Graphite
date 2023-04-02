package me.eglp.gv2.util.settings;

import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONConvertible;
import me.mrletsplay.mrcore.json.converter.JSONValue;

public class AmongUsSettings implements JSONConvertible {
	
	@JSONValue
	private boolean enable;
	
	@JSONValue
	private String captureURL;
	
	@JSONValue
	private String captureAlternativeURL;
	
	@JSONValue
	private int port;
	
	@JSONConstructor
	private AmongUsSettings() {}
	
	public boolean isEnabled() {
		return enable;
	}
	
	public String getCaptureURL() {
		return captureURL;
	}
	
	public String getCaptureAlternativeURL() {
		return captureAlternativeURL;
	}
	
	public int getPort() {
		return port;
	}
	
	public static AmongUsSettings createDefault() {
		AmongUsSettings s = new AmongUsSettings();
		s.enable = false;
		s.captureURL = "aucapture://au.example.com:443/{code}";
		s.captureAlternativeURL = "https://au.example.com:443/";
		s.port = 6585;
		return s;
	}
	
}

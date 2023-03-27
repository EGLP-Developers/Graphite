package me.eglp.gv2.util.webinterface.base;

import me.eglp.gv2.util.webinterface.WebinterfacePacket;
import me.mrletsplay.mrcore.json.JSONObject;

public class WebinterfaceResponse {

	private boolean success;
	private JSONObject data;
	private String errorMessage;
	
	private WebinterfaceResponse(boolean success, JSONObject data, String errorMessage) {
		this.success = success;
		this.data = data;
		this.errorMessage = errorMessage;
	}
	
	public boolean isSuccess() {
		return success;
	}
	
	public JSONObject getData() {
		return data;
	}
	
	public WebinterfacePacket toPacket(String referrerID) {
		if(!success) return WebinterfacePacket.error(referrerID, errorMessage);
		return WebinterfacePacket.ofResponse(referrerID, data);
	}
	
	public static WebinterfaceResponse success(JSONObject data) {
		return new WebinterfaceResponse(true, data, null);
	}
	
	public static WebinterfaceResponse success() {
		return success(null);
	}
	
	public static WebinterfaceResponse error(String message) {
		return new WebinterfaceResponse(false, null, message);
	}
	
}

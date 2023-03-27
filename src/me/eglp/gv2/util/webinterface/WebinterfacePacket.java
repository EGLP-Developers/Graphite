package me.eglp.gv2.util.webinterface;

import java.util.UUID;

import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONConverter;
import me.mrletsplay.mrcore.json.converter.JSONConvertible;
import me.mrletsplay.mrcore.json.converter.JSONValue;

public class WebinterfacePacket implements JSONConvertible {
	
	@JSONValue
	private String
		botIdentifier,
		id,
		referrerID;

	@JSONValue
	private boolean success;

	@JSONValue
	private String guildID;

	@JSONValue
	private String requestMethod;

	@JSONValue
	private JSONObject data;
	
	@JSONValue
	private String errorMessage;
	
	@JSONConstructor
	private WebinterfacePacket() {}
	
	public WebinterfacePacket(String id, String referrerID, boolean success, String guildID, String requestMethod, JSONObject data, String errorMessage) {
		this.id = id;
		this.referrerID = referrerID;
		this.success = success;
		this.guildID = guildID;
		this.requestMethod = requestMethod;
		this.data = data;
		this.errorMessage = errorMessage;
	}
	
	public String getID() {
		return id;
	}

	public String getReferrerID() {
		return referrerID;
	}
	
	public boolean isSuccess() {
		return success;
	}
	
	public String getBotIdentifier() {
		return botIdentifier;
	}
	
	public String getGuildID() {
		return guildID;
	}

	public String getRequestMethod() {
		return requestMethod;
	}

	public JSONObject getData() {
		return data;
	}

	public String getErrorMessage() {
		return errorMessage;
	}
	
	public static WebinterfacePacket deserialize(JSONObject obj) {
		return JSONConverter.decodeObject(obj, WebinterfacePacket.class);
	}
	
	public static WebinterfacePacket of(String requestMethod, JSONObject data) {
		return new WebinterfacePacket(randomID(), null, true, null, requestMethod, data, null);
	}
	
	public static WebinterfacePacket ofResponse(String referrerID, JSONObject data) {
		return new WebinterfacePacket(randomID(), referrerID, true, null, null, data, null);
	}
	
	public static WebinterfacePacket error(String referrerID, String errorMessage) {
		return new WebinterfacePacket(randomID(), referrerID, false, null, null, null, errorMessage);
	}
	
	private static String randomID() {
		return UUID.randomUUID().toString().replace("-", "");
	}
	
}

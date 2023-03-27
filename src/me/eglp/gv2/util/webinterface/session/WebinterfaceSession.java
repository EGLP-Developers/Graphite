package me.eglp.gv2.util.webinterface.session;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.webinterface.base.GraphiteWebinterfaceUser;
import me.mrletsplay.mrcore.json.JSONObject;

public class WebinterfaceSession {
	
	private String id;
	private String userID;
	private GraphiteWebinterfaceUser user;
	private long expiresAt;
	private JSONObject data;
	
	public WebinterfaceSession(String id, String userID, long expiresAt, JSONObject data) {
		this.id = id;
		this.userID = userID;
		this.expiresAt = expiresAt;
		this.data = data;
	}
	
	void load() {
		this.user = Graphite.getWebinterface().getAccountManager().getUser(userID);
	}
	
	public String getID() {
		return id;
	}
	
	public String getUserID() {
		return userID;
	}
	
	public GraphiteWebinterfaceUser getUser() {
		return user;
	}
	
	public long getExpiresAt() {
		return expiresAt;
	}
	
	public JSONObject getData() {
		return data;
	}
	
	public void commitData() {
		Graphite.getWebinterface().getSessionStorage().updateData(id, data);
	}
	
	public boolean isValid() {
		return expiresAt > System.currentTimeMillis() && user != null;
	}

}

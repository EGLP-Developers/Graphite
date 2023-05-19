	package me.eglp.gv2.util.webinterface.base;

import me.eglp.gv2.guild.GraphiteGuild;
import me.mrletsplay.mrcore.json.JSONObject;

public class WebinterfaceRequestEvent {

	private WebinterfaceRequest request;

	public WebinterfaceRequestEvent(WebinterfaceRequest request) {
		this.request = request;
	}

	public GraphiteWebinterfaceUser getUser() {
		return request.getUser();
	}

	public long getJDAUserIDLong() {
		return Long.parseLong(request.getUser().getDiscordUser().getID());
	}

	public GraphiteGuild getSelectedGuild() {
		return request.getSelectedGuild();
	}

	public WebinterfaceRequest getRequest() {
		return request;
	}

	public String getRequestMethod() {
		return request.getRequestMethod();
	}

	public JSONObject getRequestData() {
		return request.getRequestData();
	}

	public boolean isLoggedIn() {
		return request.getUser() != null;
	}

	public boolean hasGuildSelected() {
		return request.getSelectedGuild() != null;
	}

}

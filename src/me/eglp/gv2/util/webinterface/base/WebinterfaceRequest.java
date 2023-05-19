package me.eglp.gv2.util.webinterface.base;

import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.multiplex.bot.MultiplexBot;
import me.eglp.gv2.util.webinterface.WebinterfacePacket;
import me.mrletsplay.mrcore.json.JSONObject;

public class WebinterfaceRequest {

	private GraphiteWebinterfaceUser user;
	private MultiplexBot selectedBot;
	private GraphiteGuild selectedGuild;
	private WebinterfacePacket rawRequest;

	public WebinterfaceRequest(GraphiteWebinterfaceUser user, MultiplexBot selectedBot, GraphiteGuild selectedGuild, WebinterfacePacket rawRequest) {
		this.rawRequest = rawRequest;
		this.selectedBot = selectedBot;
		this.selectedGuild = selectedGuild;
		this.user = user;
	}

	public String getRequestMethod() {
		return rawRequest.getRequestMethod();
	}

	public JSONObject getRequestData() {
		return rawRequest.getData();
	}

	public GraphiteWebinterfaceUser getUser() {
		return user;
	}

	public MultiplexBot getSelectedBot() {
		return selectedBot;
	}

	public String getSelectedGuildID() {
		return rawRequest.getGuildID();
	}

	public GraphiteGuild getSelectedGuild() {
		return selectedGuild;
	}

	public WebinterfacePacket getRawRequest() {
		return rawRequest;
	}

}

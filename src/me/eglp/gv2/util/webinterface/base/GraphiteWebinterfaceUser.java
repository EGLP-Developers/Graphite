package me.eglp.gv2.util.webinterface.base;

import java.util.ArrayList;
import java.util.List;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.base.user.GraphiteUser;
import me.eglp.gv2.util.webinterface.GraphiteWebinterface;

public class GraphiteWebinterfaceUser {

	private GraphiteWebinterface webinterface;
	private GraphiteUser discordUser;
	private boolean admin;
	private List<GraphiteWebinterfaceGuild> guilds;
	
	public GraphiteWebinterfaceUser(GraphiteWebinterface webinterface, String id, boolean admin) {
		this.webinterface = webinterface;
		this.discordUser = Graphite.getGlobalUser(id);
		this.admin = admin;
		this.guilds = new ArrayList<>();
	}
	
	public boolean isKnown() {
		return discordUser != null && discordUser.isAvailable();
	}
	
	public void setAdmin(boolean admin) {
		this.admin = admin;
		webinterface.getAccountManager().updateUser(this);
	}
	
	public boolean isAdmin() {
		return admin;
	}
	
	public GraphiteUser getDiscordUser() {
		return discordUser;
	}
	
	public GraphiteWebinterface getWebinterface() {
		return webinterface;
	}
	
	public boolean isOnGuild(String guildID) {
		if(isAdmin()) return true;
		return guilds.stream().anyMatch(g -> g.getID().equals(guildID));
	}
	
	public void setGuilds(List<GraphiteWebinterfaceGuild> guilds) {
		this.guilds = guilds;
		webinterface.getAccountManager().updateUser(this);
	}
	
	public void setGuildsRaw(List<GraphiteWebinterfaceGuild> guilds) {
		this.guilds = guilds;
	}
	
	public List<GraphiteWebinterfaceGuild> getGuilds() {
		return guilds;
	}
	
}

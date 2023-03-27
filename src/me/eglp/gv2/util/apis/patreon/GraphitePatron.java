package me.eglp.gv2.util.apis.patreon;

import com.patreon.resources.User;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.base.user.GraphiteUser;

public class GraphitePatron {
	
	private String patreonID, discordUserID, fullName;
	private GraphiteUser discordUser;
	
	public GraphitePatron(User patreonUser) {
		this.patreonID = patreonUser.getId();
		this.fullName = patreonUser.getFullName();
		this.discordUserID = patreonUser.getSocialConnections().getDiscord().getUser_id();
	}
	
	public GraphitePatron(String patreonID, String fullName, String discordUserID) {
		this.patreonID = patreonID;
		this.fullName = fullName;
		this.discordUserID = discordUserID;
	}
	
	public String getFullName() {
		return fullName;
	}
	
	public GraphiteUser getDiscordUser() {
		if(discordUser == null) discordUser = Graphite.getUser(discordUserID);
		return discordUser;
	}
	
	public String getPatreonID() {
		return patreonID;
	}
	
	public PatreonPledge getPledge() {
		return Graphite.getPatreon().getPledgeByPatron(this);
	}
	
}

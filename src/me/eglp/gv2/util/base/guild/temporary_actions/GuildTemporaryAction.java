package me.eglp.gv2.util.base.guild.temporary_actions;

import me.eglp.gv2.util.base.guild.GraphiteGuild;
import me.eglp.gv2.util.base.guild.GraphiteMember;

abstract class GuildTemporaryAction {
	
	private GraphiteGuild guild;
	private String userID;
	private long expirationTime;
	
	public GuildTemporaryAction(GraphiteGuild guild, String userID, long expiresAt) {
		this.guild = guild;
		this.userID = userID;
		this.expirationTime = expiresAt;
	}
	
	public void setGuild(GraphiteGuild guild) {
		this.guild = guild;
	}
	
	public GraphiteGuild getGuild() {
		return guild;
	}
	
	public String getUserID() {
		return userID;
	}
	
	public long getExpirationTime() {
		return expirationTime;
	}
	
	public boolean isExpired() {
		return System.currentTimeMillis() >= getExpirationTime();
	}
	
	public abstract void remove(GraphiteMember moderator, String reason);
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof GuildTemporaryAction)) return false;
		GuildTemporaryAction o = (GuildTemporaryAction) obj;
		return guild.equals(o.guild) &&
				userID.equals(o.userID) &&
				expirationTime == o.expirationTime;
	}

}

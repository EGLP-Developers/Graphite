package me.eglp.gv2.util.backup.data.bans;

import java.util.concurrent.TimeUnit;

import me.eglp.gv2.util.base.guild.GraphiteGuild;
import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONConvertible;
import me.mrletsplay.mrcore.json.converter.JSONValue;
import net.dv8tion.jda.api.entities.Guild.Ban;

public class BackupBan implements JSONConvertible {
	
	@JSONValue
	private String 
		userName,
		userID,
		reason;
	
	@JSONConstructor
	private BackupBan() {}
	
	public BackupBan(Ban b) {
		try {
			this.userName = b.getUser().getName();
		}catch(UnsupportedOperationException ignored) {}
		
		this.userID = b.getUser().getId();
		this.reason = b.getReason();
	}
	
	public String getUserName() {
		return userName;
	}
	
	public String getUserID() {
		return userID;
	}
	
	public String getReason() {
		return reason;
	}
	
	public void restore(GraphiteGuild guild) {
		if(guild.getMember(userID) != null) guild.getJDAGuild().ban(guild.getMember(userID).getJDAMember(), 0, TimeUnit.SECONDS).reason(reason).queue();
	}

}

package me.eglp.gv2.util.base.guild.temporary_actions;

import me.eglp.gv2.util.base.guild.GraphiteGuild;
import me.eglp.gv2.util.base.guild.GraphiteMember;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.ErrorResponse;

public class GuildTempBan extends GuildTemporaryAction {
	
	public GuildTempBan(GraphiteGuild guild, String userID, long expiresAt) {
		super(guild, userID, expiresAt);
	}
	
	@Override
	public void remove(GraphiteMember moderator, String reason) {
		getGuild().getTemporaryActionsConfig().removeTempBan(this, moderator, reason);
	}
	
	public boolean existsBan() {
		try {
			return getGuild().getJDAGuild().retrieveBan(UserSnowflake.fromId(getUserID())).complete() != null;
		}catch(ErrorResponseException e) {
			if(e.getErrorResponse() == ErrorResponse.UNKNOWN_BAN) return false;
			throw e;
		}
	}
	
}

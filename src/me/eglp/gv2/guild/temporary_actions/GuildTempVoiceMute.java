package me.eglp.gv2.guild.temporary_actions;

import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.guild.GraphiteMember;

public class GuildTempVoiceMute extends GuildTemporaryAction {

	public GuildTempVoiceMute(GraphiteGuild guild, String userID, long expiresAt) {
		super(guild, userID, expiresAt);
	}

	@Override
	public void remove(GraphiteMember moderator, String reason) {
		GraphiteMember mem = getGuild().getMember(getUserID());
		if(mem == null) return;
		if(mem.getCurrentAudioChannel() != null) getGuild().getJDAGuild().mute(mem.getJDAMember(), false).complete();
		getGuild().getTemporaryActionsConfig().removeTempMute(this, moderator, reason);
	}

}

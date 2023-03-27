package me.eglp.gv2.util.base.guild.automod.discord_invites;

import java.util.List;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.base.guild.automod.AutoModMessageActionHandler;
import me.eglp.gv2.util.base.guild.automod.GuildAutoModSettings;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class DiscordInvites implements AutoModMessageActionHandler<DiscordInvitesSettings> {

	@Override
	public DiscordInvitesSettings getRelevantSettings(GuildAutoModSettings settings) {
		return settings.getDiscordInvitesSettings();
	}

	@Override
	public void takeAction(MessageReceivedEvent event, DiscordInvitesSettings settings) {
		List<String> invites = event.getMessage().getInvites();
		
		if(!invites.isEmpty()) {
			if(invites.stream().allMatch(i -> settings.getAllowedInviteCodes().contains(i))) return;
			if(settings.getAction().isDelete()) event.getMessage().delete().queue();
			if(settings.getAction().isWarn()) {
				settings.sendWarningMessage(Graphite.getMessageChannel(event.getChannel()), Graphite.getMember(event.getMember()));
			}
			
			if(settings.isPunishable()) settings.addInfraction(Graphite.getMember(event.getMember()));
		}
	}
	
}

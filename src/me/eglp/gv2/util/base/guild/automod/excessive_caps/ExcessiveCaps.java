package me.eglp.gv2.util.base.guild.automod.excessive_caps;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.base.guild.automod.AutoModMessageActionHandler;
import me.eglp.gv2.util.base.guild.automod.GuildAutoModSettings;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ExcessiveCaps implements AutoModMessageActionHandler<ExcessiveCapsSettings> {
	
	@Override
	public ExcessiveCapsSettings getRelevantSettings(GuildAutoModSettings settings) {
		return settings.getExcessiveCapsSettings();
	}
	
	@Override
	public void takeAction(MessageReceivedEvent event, ExcessiveCapsSettings settings) {
		int caps = (int) event.getMessage().getContentRaw().chars()
				.filter(Character::isUpperCase)
				.count();
		
		if(event.getMessage().getContentRaw().length() > settings.getMinTextLength() && ((double) caps / event.getMessage().getContentRaw().length()) > settings.getMaxCapsPercent() / 100D) {
			if(settings.getAction().isDelete()) event.getMessage().delete().queue();
			if(settings.getAction().isWarn()) {
				settings.sendWarningMessage(Graphite.getMessageChannel(event.getChannel()), Graphite.getMember(event.getMember()));
			}
			if(settings.isPunishable()) settings.addInfraction(Graphite.getMember(event.getMember()));
		}
	}
	
}

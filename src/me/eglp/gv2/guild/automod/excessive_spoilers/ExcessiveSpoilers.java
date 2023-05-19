package me.eglp.gv2.guild.automod.excessive_spoilers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.eglp.gv2.guild.automod.AutoModMessageActionHandler;
import me.eglp.gv2.guild.automod.GuildAutoModSettings;
import me.eglp.gv2.main.Graphite;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ExcessiveSpoilers implements AutoModMessageActionHandler<ExcessiveSpoilersSettings> {

	private static final Pattern SPOILER_PATTERN = Pattern.compile("\\|\\|.+?\\|\\|");

	@Override
	public ExcessiveSpoilersSettings getRelevantSettings(GuildAutoModSettings settings) {
		return settings.getExcessiveSpoilersSettings();
	}

	@Override
	public void takeAction(MessageReceivedEvent event, ExcessiveSpoilersSettings settings) {
		Matcher m = SPOILER_PATTERN.matcher(event.getMessage().getContentDisplay());
		if(m.results().count() > settings.getMaxSpoilers()) {
			if(settings.getAction().isDelete()) event.getMessage().delete().queue();
			if(settings.getAction().isWarn()) {
				settings.sendWarningMessage(Graphite.getMessageChannel(event.getChannel()), Graphite.getMember(event.getMember()));
			}
			if(settings.isPunishable()) settings.addInfraction(Graphite.getMember(event.getMember()));
		}
	}

}

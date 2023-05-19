package me.eglp.gv2.guild.automod.repeated_text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.eglp.gv2.guild.automod.AutoModMessageActionHandler;
import me.eglp.gv2.guild.automod.GuildAutoModSettings;
import me.eglp.gv2.main.Graphite;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class RepeatedText implements AutoModMessageActionHandler<RepeatedTextSettings> {

	@Override
	public RepeatedTextSettings getRelevantSettings(GuildAutoModSettings settings) {
		return settings.getRepeatedTextSettings();
	}

	@Override
	public void takeAction(MessageReceivedEvent event, RepeatedTextSettings settings) {
		Pattern pattern = Pattern.compile(String.format("(.{%s,})(.+?\\1){%s,}", settings.getMinTextLength() /* text length min */, settings.getMaxRepeats() /* repetitions max */));
		Matcher m = pattern.matcher(event.getMessage().getContentRaw());
		if(m.matches()) {
			if(settings.getAction().isDelete()) event.getMessage().delete().queue();
			if(settings.getAction().isWarn()) {
				settings.sendWarningMessage(Graphite.getMessageChannel(event.getChannel()), Graphite.getMember(event.getMember()));
			}
			if(settings.isPunishable()) settings.addInfraction(Graphite.getMember(event.getMember()));
		}
	}

}

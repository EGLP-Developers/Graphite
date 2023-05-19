package me.eglp.gv2.guild.automod.external_links;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.eglp.gv2.guild.automod.AutoModMessageActionHandler;
import me.eglp.gv2.guild.automod.GuildAutoModSettings;
import me.eglp.gv2.main.Graphite;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ExternalLinks implements AutoModMessageActionHandler<ExternalLinksSettings> {

	private static final Pattern URL_PATTERN = Pattern.compile("((http:\\/\\/|https:\\/\\/)(www.)?(([a-zA-Z0-9-]){2,}\\.){1,4}([a-zA-Z]){2,6}(\\/([a-zA-Z-_\\/\\.0-9#:?=&;,]*)?)?)");

	@Override
	public ExternalLinksSettings getRelevantSettings(GuildAutoModSettings settings) {
		return settings.getExternalLinksSettings();
	}

	@Override
	public void takeAction(MessageReceivedEvent event, ExternalLinksSettings settings) {
		Matcher m = URL_PATTERN.matcher(event.getMessage().getContentRaw());

		while(m.find()) {
			if(settings.getAllowedLinks().stream().anyMatch(e -> m.group().equalsIgnoreCase(e))) continue;
			if(settings.getAction().isDelete()) event.getMessage().delete().queue();
			if(settings.getAction().isWarn()) {
				settings.sendWarningMessage(Graphite.getMessageChannel(event.getChannel()), Graphite.getMember(event.getMember()));
			}
			if(settings.isPunishable()) settings.addInfraction(Graphite.getMember(event.getMember()));
			break;
		}
	}

}

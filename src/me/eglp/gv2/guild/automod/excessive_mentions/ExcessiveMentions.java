package me.eglp.gv2.guild.automod.excessive_mentions;

import me.eglp.gv2.guild.automod.AutoModMessageActionHandler;
import me.eglp.gv2.guild.automod.GuildAutoModSettings;
import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.mention.MentionFinder;
import me.eglp.gv2.util.mention.MentionType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ExcessiveMentions implements AutoModMessageActionHandler<ExcessiveMentionsSettings> {

	@Override
	public ExcessiveMentionsSettings getRelevantSettings(GuildAutoModSettings settings) {
		return settings.getExcessiveMentionsSettings();
	}

	@Override
	public void takeAction(MessageReceivedEvent event, ExcessiveMentionsSettings settings) {
		int mentions = (int) MentionFinder.findAllMentions(Graphite.getGuild(event.getGuild()), event.getMessage().getContentRaw()).stream()
				.filter(r -> r.getMention().isOfType(MentionType.USER) || r.getMention().isOfType(MentionType.ROLE))
				.count();

		if(mentions > settings.getMaxMentions()) {
			if(settings.getAction().isDelete()) event.getMessage().delete().queue();
			if(settings.getAction().isWarn()) {
				settings.sendWarningMessage(Graphite.getMessageChannel(event.getChannel()), Graphite.getMember(event.getMember()));
			}
			if(settings.isPunishable()) settings.addInfraction(Graphite.getMember(event.getMember()));
		}
	}

}

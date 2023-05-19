package me.eglp.gv2.guild.automod.excessive_emoji;

import me.eglp.gv2.guild.automod.AutoModMessageActionHandler;
import me.eglp.gv2.guild.automod.GuildAutoModSettings;
import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.emote.unicode.GraphiteUnicode;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ExcessiveEmoji implements AutoModMessageActionHandler<ExcessiveEmojiSettings> {

	@Override
	public ExcessiveEmojiSettings getRelevantSettings(GuildAutoModSettings settings) {
		return settings.getExcessiveEmojiSettings();
	}

	@Override
	public void takeAction(MessageReceivedEvent event, ExcessiveEmojiSettings settings) {
		String t = event.getMessage().getContentRaw();

		int i = 0;
		int idx = 0;
		while(idx < t.length()) {
			final int idx2 = idx;
			String em = GraphiteUnicode.getRawCodePoints().stream()
					.filter(e -> t.startsWith(e, idx2))
					.findFirst().orElse(null);
			if(em != null) {
				i++;
				idx += em.length();
			}else {
				idx++;
			}
		}

		if(i > settings.getMaxEmojis()) {
			if(settings.getAction().isDelete()) event.getMessage().delete().queue();
			if(settings.getAction().isWarn()) {
				settings.sendWarningMessage(Graphite.getMessageChannel(event.getChannel()), Graphite.getMember(event.getMember()));
			}
			if(settings.isPunishable()) settings.addInfraction(Graphite.getMember(event.getMember()));
		}
	}

}

package me.eglp.gv2.commands.info;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.text.StringEscapeUtils;

import me.eglp.gv2.util.command.Command;
import me.eglp.gv2.util.command.CommandCategory;
import me.eglp.gv2.util.command.CommandInvokedEvent;
import me.eglp.gv2.util.lang.DefaultLocaleString;
import me.eglp.gv2.util.lang.DefaultMessage;
import me.eglp.gv2.util.mention.GraphiteEmoteMention;
import me.eglp.gv2.util.mention.GraphiteMention;
import me.eglp.gv2.util.mention.MentionFinder;
import me.eglp.gv2.util.mention.MentionFinder.SearchResult;
import me.eglp.gv2.util.mention.MentionType;
import me.eglp.gv2.util.selfcheck.SpecialSelfcheck;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class CommandEmoteInfo extends Command{
	
	public CommandEmoteInfo() {
		super(null, CommandCategory.INFO, "emoteinfo");
		setDescription(DefaultLocaleString.COMMAND_EMOTEINFO_DESCRIPTION);
		setUsage(DefaultLocaleString.COMMAND_EMOTEINFO_USAGE);
		setAllowPrivate(true);
		addAlias("einfo");
	}
	
	@SpecialSelfcheck(needsPermission = false)
	@Override
	public void action(CommandInvokedEvent event) {
		String emote = (String) event.getOption("emote");
		
		List<SearchResult> results = MentionFinder.findAllMentions(event.getGuild(), emote);
		if(results.size() != 1) {
			DefaultMessage.COMMAND_EMOTEINFO_UNICODE.reply(event,
					"escaped_java", StringEscapeUtils.escapeJava(emote),
					"escaped_html", StringEscapeUtils.escapeHtml4(emote));
			return;
		}
		
		GraphiteMention m = results.get(0).getMention();
		if(!m.isValid() || !m.isOfType(MentionType.EMOTE)) {
			DefaultMessage.ERROR_ALLOWED_MENTION_TYPE_MESSAGE.reply(event,
					"allowed_mentions",
					DefaultMessage.getMentionTypesString(event.getGuild(), MentionType.EMOTE));
			return;
		}

		GraphiteEmoteMention em = m.asEmoteMention();
		DefaultMessage.COMMAND_EMOTEINFO_EMOTE.reply(event, "id", em.getEmoteID(), "raw", em.getJDAEmote().getFormatted());
	}

	@Override
	public List<OptionData> getOptions() {
		return Arrays.asList(
				new OptionData(OptionType.STRING, "emote", "The emote to view information about", true)
			);
	}
	
}

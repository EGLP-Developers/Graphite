package me.eglp.gv2.commands.fun;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import me.eglp.gv2.util.GraphiteUtil;
import me.eglp.gv2.util.base.guild.GraphiteModule;
import me.eglp.gv2.util.base.guild.poll.GuildPoll;
import me.eglp.gv2.util.command.Command;
import me.eglp.gv2.util.command.CommandCategory;
import me.eglp.gv2.util.command.CommandInvokedEvent;
import me.eglp.gv2.util.command.ParentCommand;
import me.eglp.gv2.util.emote.JDAEmote;
import me.eglp.gv2.util.emote.unicode.GraphiteUnicode;
import me.eglp.gv2.util.lang.DefaultLocaleString;
import me.eglp.gv2.util.lang.DefaultMessage;
import me.eglp.gv2.util.lang.GraphiteTimeParser;
import me.eglp.gv2.util.permission.DefaultPermissions;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class CommandPoll extends ParentCommand {
	
	private static final int OPTION_COUNT = 15;
	
	public CommandPoll() {
		super(GraphiteModule.FUN, CommandCategory.FUN, "poll");
		setDescription(DefaultLocaleString.COMMAND_POLL_DESCRIPTION);
		
		addSubCommand(new Command(this, "create") {
			
			@Override
			public void action(CommandInvokedEvent event) {
				String question = (String) event.getOption("question");
				String duration = (String) event.getOption("duration");
				boolean allowMultiple = (boolean) event.getOption("allow_multiple");
				
				long durationMs = GraphiteTimeParser.parseShortDuration(duration);
				
				if(durationMs == -1) {
					DefaultMessage.ERROR_INVALID_TIMESTAMP.reply(event);
					return;
				}
				
				if(durationMs < 5 * 60 * 1000) {
					DefaultMessage.COMMAND_POLL_CREATE_DURATION_TOO_SHORT.reply(event);
					return;
				}
				
				GuildPoll poll = new GuildPoll(event.getGuild(), question, System.currentTimeMillis() + durationMs, allowMultiple);
				
				for(int i = 1; i <= OPTION_COUNT; i++) {
					String op = (String) event.getOption("option" + i);
					if(op == null) continue;
					
					if(op.contains("=")) {
						String[] spl = op.split("=", 2);
						String emoji = spl[0].trim();
						if(!GraphiteUnicode.getRawCodePoints().contains(emoji) && !Message.MentionType.EMOJI.getPattern().matcher(emoji).matches()) {
							DefaultMessage.COMMAND_POLL_CREATE_INVALID_EMOJI.reply(event, "emoji", emoji);
							return;
						}
						Emoji e = Emoji.fromFormatted(emoji);
						poll.addOption("poll_" + GraphiteUtil.randomShortID(), e, spl[1].trim());
					}else {
						poll.addOption("poll_" + GraphiteUtil.randomShortID(), JDAEmote.getKeycapNumber(i).getEmoji(), op);
					}
				}
				
				poll.send(event.getTextChannel());
				event.getGuild().getPollsConfig().savePoll(poll);
				
				event.deleteMessage(JDAEmote.OK_HAND);
			}
			
			@Override
			public List<OptionData> getOptions() {
				List<OptionData> ops = new ArrayList<>(Arrays.asList(
						new OptionData(OptionType.STRING, "question", "The question of the poll", true),
						new OptionData(OptionType.STRING, "duration", "The duration of the poll", true),
						new OptionData(OptionType.BOOLEAN, "allow_multiple", "Whether to allow users to vote for more than one option", true)));
				
				for(int i = 1; i <= OPTION_COUNT; i++) {
					ops.add(new OptionData(OptionType.STRING, "option" + i, "Option #" + i, i <= 2));
				}
				
				return ops;
			}
		})
		.setDescription(DefaultLocaleString.COMMAND_POLL_CREATE_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_POLL_CREATE_USAGE)
		.setPermission(DefaultPermissions.FUN_POLL_CREATE);
		
		addSubCommand(new Command(this, "list") {
			
			@Override
			public void action(CommandInvokedEvent event) {
				List<GuildPoll> polls = event.getGuild().getPollsConfig().getPolls();
				if(polls.isEmpty()) {
					DefaultMessage.COMMAND_POLL_LIST_NO_POLLS.reply(event);
					return;
				}
				
				event.reply(DefaultLocaleString.COMMAND_POLL_LIST_LIST.getFor(event.getGuild(), "polls", polls.stream()
						.map(p -> p.getID() + ": " + p.getQuestion())
						.collect(Collectors.joining("\n"))));
			}
			
			@Override
			public List<OptionData> getOptions() {
				return Collections.emptyList();
			}
		})
		.setDescription(DefaultLocaleString.COMMAND_POLL_LIST_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_POLL_LIST_USAGE)
		.setPermission(DefaultPermissions.FUN_POLL_LIST);
		
		addSubCommand(new Command(this, "stop") {
			
			@Override
			public void action(CommandInvokedEvent event) {
				String pollID = (String) event.getOption("poll");
				GuildPoll poll = event.getGuild().getPollsConfig().getPoll(pollID);
				if(poll == null) {
					DefaultMessage.COMMAND_POLL_STOP_INVALID_POLL.reply(event);
					return;
				}
				
				poll.finish();
				DefaultMessage.COMMAND_POLL_STOP_SUCCESS.reply(event);
			}
			
			@Override
			public List<OptionData> getOptions() {
				return Arrays.asList(
						new OptionData(OptionType.STRING, "poll", "The ID of the poll", true)
					);
			}
		})
		.setDescription(DefaultLocaleString.COMMAND_POLL_STOP_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_POLL_STOP_USAGE)
		.setPermission(DefaultPermissions.FUN_POLL_STOP);
	}
	
}

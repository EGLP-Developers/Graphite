package me.eglp.gv2.commands.fun;

import java.awt.Color;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import me.eglp.gv2.util.base.guild.GraphiteGuildMessageChannel;
import me.eglp.gv2.util.base.guild.GraphiteModule;
import me.eglp.gv2.util.base.guild.reminder.GuildReminder;
import me.eglp.gv2.util.base.guild.reminder.ReminderRepetitionEnum;
import me.eglp.gv2.util.command.Command;
import me.eglp.gv2.util.command.CommandCategory;
import me.eglp.gv2.util.command.CommandInvokedEvent;
import me.eglp.gv2.util.command.ParentCommand;
import me.eglp.gv2.util.lang.DefaultLocaleString;
import me.eglp.gv2.util.lang.DefaultMessage;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

/**
 * This is the reminder Command class. It handles the different sub-commands and deligates the functionality to {@link GuildReminder}.
 * 
 * @author The Arrayser
 * @date Mon Mar 27 18:28:48 2023
 */

public class CommandReminder extends ParentCommand {
	
	public CommandReminder() {
		super(GraphiteModule.FUN, CommandCategory.FUN, "reminder");
		setDescription(DefaultLocaleString.COMMAND_REMINDER_DESCRIPTION);
		
		addSubCommand(new Command(this, "create") {
			
			@Override
			public void action(CommandInvokedEvent event) {
				//"{prefix}reminder create <date and time> <reminder message> [repeat (1y2d)] [channel]"
				String date = (String) event.getOption("date_and_time");
				String reminderMessage = (String) event.getOption("reminder_message");
				
				// optional
				String repeat = (String) event.getOption("repeat");
				ReminderRepetitionEnum repeatE; 
				if(repeat == null) {
					repeatE = null;
				}else {
					repeatE = ReminderRepetitionEnum.valueOf(repeat);
				}
				GraphiteGuildMessageChannel channel = (GraphiteGuildMessageChannel) event.getOption("channel");
				if(channel == null) {
					//Use the current channel instead
					channel = event.getGuildChannel();
				}
				
				LocalDateTime dateMs = null;
				
				try {
					 dateMs =  LocalDateTime.parse(date, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
				}catch(DateTimeParseException e) {
					DefaultMessage.ERROR_INVALID_TIMESTAMP.reply(event);
					e.printStackTrace();
					return;
				}
				
				if(dateMs.toEpochSecond(event.getGuild().getConfig().getTimezone().getRules().getOffset(Instant.now())) <= System.currentTimeMillis() / 1000) {
					//TODO reminder is in the past
					//event.reply("youz stink");
					DefaultMessage.COMMAND_REMINDER_CREATE_EVENT_IS_IN_THE_PAST.reply(event);
					return;
				}
				
				GuildReminder reminder = new GuildReminder(event.getGuild(), dateMs, reminderMessage, repeatE, channel);
				
				reminder.enqueue();
				event.getGuild().getRemindersConfig().saveReminder(reminder);
				
				DefaultMessage.COMMAND_REMINDER_CREATE_SUCCESS.reply(event);//"Your reminder got succesefully enqueued!");
				
				//event.deleteMessage(JDAEmote.OK_HAND);
			}
			
			@Override
			public List<OptionData> getOptions() {
				OptionData optionRepeat = new OptionData(OptionType.STRING, "repeat", "Whether and when to repeat the reminder", false);
				for(ReminderRepetitionEnum a : ReminderRepetitionEnum.values()) {
					optionRepeat.addChoice(a.toString(), a.name());
				}
				List<OptionData> ops = new ArrayList<>(Arrays.asList(
						new OptionData(OptionType.STRING, "date_and_time", "When the reminder is triggered", true),
						new OptionData(OptionType.STRING, "reminder_message", "The message of the reminder", true),
						optionRepeat,
						new OptionData(OptionType.CHANNEL, "channel", "The channel in which to send the reminder", false).setChannelTypes(ChannelType.TEXT, ChannelType.NEWS, ChannelType.GUILD_NEWS_THREAD, ChannelType.GUILD_PRIVATE_THREAD, ChannelType.GUILD_PUBLIC_THREAD)));
				return ops;
			}
		})
		.setDescription(DefaultLocaleString.COMMAND_REMINDER_CREATE_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_REMINDER_CREATE_USAGE)
		.setPermission("fun.reminder.create");
		
		addSubCommand(new Command(this, "list") {
			
			@Override
			public void action(CommandInvokedEvent event) {
				List<GuildReminder> reminders = event.getGuild().getRemindersConfig().getReminders();
				if(reminders.isEmpty()) {
					DefaultMessage.COMMAND_REMINDER_LIST_NO_REMINDERS.reply(event);
					return;
				}
				
				EmbedBuilder theEmbed = new EmbedBuilder().setColor(Color.PINK).setDescription(DefaultLocaleString.COMMAND_REMINDER_LIST_EMBED_DESCRIPTION.getFor(event.getGuild())).setTitle(DefaultLocaleString.COMMAND_REMINDER_LIST_EMBED_TITLE.getFor(event.getGuild()));
				for(GuildReminder currentReminder : reminders) {
					theEmbed.addField(currentReminder.getId(),
							DefaultLocaleString.COMMAND_REMINDER_LIST_EMBED_CHANNEL.getFor(event.getGuild()) + " " +
					        currentReminder.getGuild().getGuildMessageChannelByID(currentReminder.getChannelID()).getName() + "\n" +
									DefaultLocaleString.COMMAND_REMINDER_LIST_EMBED_MESSAGE.getFor(event.getGuild()) + " " + currentReminder.getMessage(), true);
				}
				event.reply(theEmbed.build());
				/*event.reply(DefaultLocaleString.COMMAND_REMINDER_LIST_LIST.getFor(event.getGuild(), "reminders", reminders.stream()
						.map(p -> {
							return (p.getId() + " in " + p.getGuild().getGuildMessageChannelByID(p.getChannelID()).getName() + ": " + p.getMessage());
						})
						.collect(Collectors.joining("\n"))));*/
			}
			
			@Override
			public List<OptionData> getOptions() {
				return Collections.emptyList();
			}
		})
		.setDescription(DefaultLocaleString.COMMAND_REMINDER_LIST_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_REMINDER_LIST_USAGE)
		.setPermission("fun.reminder.list");
		
		addSubCommand(new Command(this, "remove") {
			
			@Override
			public void action(CommandInvokedEvent event) {
				String reminderID = (String) event.getOption("reminder");
				GuildReminder reminder = event.getGuild().getRemindersConfig().getReminder(reminderID);
				if(reminder == null) {
					DefaultMessage.COMMAND_REMINDER_REMOVE_INVALID_REMINDER.reply(event);
					return;
				}
				
				reminder.remove();
				DefaultMessage.COMMAND_REMINDER_REMOVE_SUCCESS.reply(event);
 		}
			
			@Override
			public List<OptionData> getOptions() {
				return Arrays.asList(
						new OptionData(OptionType.STRING, "reminder", "The ID of the reminder", true)
					);
			}
		})
		.setDescription(DefaultLocaleString.COMMAND_REMINDER_REMOVE_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_REMINDER_REMOVE_USAGE)
		.setPermission("fun.reminder.remove");
	}
	
}

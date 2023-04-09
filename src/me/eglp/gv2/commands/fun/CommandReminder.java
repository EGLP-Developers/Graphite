package me.eglp.gv2.commands.fun;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import me.eglp.gv2.util.base.guild.GraphiteGuildMessageChannel;
import me.eglp.gv2.util.base.guild.GraphiteModule;
import me.eglp.gv2.util.base.guild.reminder.GuildReminder;
import me.eglp.gv2.util.base.guild.reminder.ReminderRepetition;
import me.eglp.gv2.util.command.Command;
import me.eglp.gv2.util.command.CommandCategory;
import me.eglp.gv2.util.command.CommandInvokedEvent;
import me.eglp.gv2.util.command.ParentCommand;
import me.eglp.gv2.util.lang.DefaultLocaleString;
import me.eglp.gv2.util.lang.DefaultMessage;
import me.eglp.gv2.util.permission.DefaultPermissions;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

/**
 * This is the reminder Command class. It handles the different sub-commands and
 * deligates the functionality to {@link GuildReminder}.
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
				String rawDate = (String) event.getOption("date_and_time");
				String reminderMessage = (String) event.getOption("reminder_message");

				// optional
				String rawRepeat = (String) event.getOption("repeat");
				ReminderRepetition repeat = null;
				if (rawRepeat != null) {
					repeat = ReminderRepetition.valueOf(rawRepeat);
				}
				
				GraphiteGuildMessageChannel channel = (GraphiteGuildMessageChannel) event.getOption("channel");
				if (channel == null) {
					// Use the current channel instead
					channel = event.getGuildChannel();
				}

				LocalDateTime date = null;

				try {
					date = LocalDateTime.parse(rawDate, GuildReminder.HUMAN_TIMESTAMP_FORMAT);
				} catch (DateTimeParseException e) {
					DefaultMessage.ERROR_INVALID_TIMESTAMP.reply(event);
					return;
				}

				if (date.atZone(event.getGuild().getConfig().getTimezone()).toInstant().isBefore(Instant.now())) {
					DefaultMessage.COMMAND_REMINDER_CREATE_EVENT_IS_IN_THE_PAST.reply(event);
					return;
				}
				
				GuildReminder reminder = new GuildReminder(channel, reminderMessage, repeat, date);
				reminder.schedule();
				event.getGuild().getRemindersConfig().saveReminder(reminder);
				
				DefaultMessage.COMMAND_REMINDER_CREATE_SUCCESS.reply(event);
			}

			@Override
			public List<OptionData> getOptions() {
				OptionData optionRepeat = new OptionData(OptionType.STRING, "repeat", "Whether and when to repeat the reminder", false);
				for (ReminderRepetition a : ReminderRepetition.values()) {
					optionRepeat.addChoice(a.getFriendlyName(), a.name());
				}
				return Arrays.asList(
					new OptionData(OptionType.STRING, "date_and_time", "When the reminder is triggered", true),
					new OptionData(OptionType.STRING, "reminder_message", "The message of the reminder", true),
					optionRepeat,
					new OptionData(OptionType.CHANNEL, "channel", "The channel in which to send the reminder", false).setChannelTypes(ChannelType.TEXT, ChannelType.NEWS)
				);
			}
		})
		.setDescription(DefaultLocaleString.COMMAND_REMINDER_CREATE_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_REMINDER_CREATE_USAGE)
		.setPermission(DefaultPermissions.FUN_REMINDER_CREATE);

		addSubCommand(new Command(this, "list") {

			@Override
			public void action(CommandInvokedEvent event) {
				List<GuildReminder> reminders = event.getGuild().getRemindersConfig().getReminders();
				if (reminders.isEmpty()) {
					DefaultMessage.COMMAND_REMINDER_LIST_NO_REMINDERS.reply(event);
					return;
				}

				EmbedBuilder b = new EmbedBuilder()
					.setTitle(DefaultLocaleString.COMMAND_REMINDER_LIST_EMBED_TITLE.getFor(event.getGuild()))
					.setDescription(DefaultLocaleString.COMMAND_REMINDER_LIST_EMBED_DESCRIPTION.getFor(event.getGuild()));
				
				Map<String, List<GuildReminder>> remindersByChannel = new LinkedHashMap<>();
				
				for (GuildReminder currentReminder : reminders) {
					List<GuildReminder> a = remindersByChannel.getOrDefault(currentReminder.getChannelID(), new ArrayList<>());
					a.add(currentReminder);
					remindersByChannel.put(currentReminder.getChannelID(), a);
				}
				
				for(Map.Entry<String, List<GuildReminder>> e : remindersByChannel.entrySet()) {
					String remindersStr = e.getValue().stream()
						.map(r -> r.getID() + ": " + r.getMessage())
						.collect(Collectors.joining("\n"));
					
					String channelName = event.getGuild().getGuildMessageChannelByID(e.getKey()).getName();
					b.addField(channelName, remindersStr, true);
				}
				
				event.reply(b.build());
			}

			@Override
			public List<OptionData> getOptions() {
				return Collections.emptyList();
			}
		})
		.setDescription(DefaultLocaleString.COMMAND_REMINDER_LIST_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_REMINDER_LIST_USAGE)
		.setPermission(DefaultPermissions.FUN_REMINDER_LIST);

		addSubCommand(new Command(this, "remove") {

			@Override
			public void action(CommandInvokedEvent event) {
				String reminderID = (String) event.getOption("reminder");
				GuildReminder reminder = event.getGuild().getRemindersConfig().getReminder(reminderID);
				if (reminder == null) {
					DefaultMessage.COMMAND_REMINDER_REMOVE_INVALID_REMINDER.reply(event);
					return;
				}

				reminder.remove();
				DefaultMessage.COMMAND_REMINDER_REMOVE_SUCCESS.reply(event);
			}

			@Override
			public List<OptionData> getOptions() {
				return Arrays.asList(new OptionData(OptionType.STRING, "reminder", "The ID of the reminder", true));
			}
		})
		.setDescription(DefaultLocaleString.COMMAND_REMINDER_REMOVE_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_REMINDER_REMOVE_USAGE)
		.setPermission(DefaultPermissions.FUN_REMINDER_REMOVE);
	}

}

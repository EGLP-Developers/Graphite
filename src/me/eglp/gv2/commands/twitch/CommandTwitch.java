package me.eglp.gv2.commands.twitch;

import java.util.Arrays;
import java.util.List;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.apis.twitch.GraphiteTwitchUser;
import me.eglp.gv2.util.base.guild.GraphiteGuild;
import me.eglp.gv2.util.base.guild.GraphiteModule;
import me.eglp.gv2.util.base.guild.GraphiteTextChannel;
import me.eglp.gv2.util.command.Command;
import me.eglp.gv2.util.command.CommandCategory;
import me.eglp.gv2.util.command.CommandInvokedEvent;
import me.eglp.gv2.util.command.ParentCommand;
import me.eglp.gv2.util.lang.DefaultLocaleString;
import me.eglp.gv2.util.lang.DefaultMessage;
import me.eglp.gv2.util.permission.DefaultPermissions;
import me.eglp.twitch.entity.TwitchUser;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class CommandTwitch extends ParentCommand{
	
	public CommandTwitch() {
		super(GraphiteModule.TWITCH, CommandCategory.TWITCH, "twitch");
		setDescription(DefaultLocaleString.COMMAND_TWITCH_DESCRIPTION);
		
		addSubCommand(new Command(this, "message") {
			
			@Override
			public void action(CommandInvokedEvent event) {
				GraphiteGuild g = event.getGuild();
				String streamerName = (String) event.getOption("twitch-user");
				if(g.getTwitchConfig().getTwitchUserByName(streamerName) == null) {
					DefaultMessage.COMMAND_TWITCH_STREAMER_NOT_FOUND.reply(event);
					return;
				}
				String message = (String) event.getOption("message");
				GraphiteTwitchUser u = g.getTwitchConfig().getTwitchUserByName(streamerName);
				u.setNotificationMessage(message);
				g.getTwitchConfig().updateTwitchUser(u);
				DefaultMessage.COMMAND_TWITCH_SET_MESSAGE.reply(event, 
						"message", message);
			}
			
			@Override
			public List<OptionData> getOptions() {
				return Arrays.asList(
						new OptionData(OptionType.STRING, "twitch-user", "The twitch user u want to change the message", true),
						new OptionData(OptionType.STRING, "message", "The new message", true)
					);
			}
			
		})
		.setDescription(DefaultLocaleString.COMMAND_TWITCH_MESSAGE_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_TWITCH_MESSAGE_USAGE)
		.setPermission(DefaultPermissions.TWITCH_MESSAGE);
		
		addSubCommand(new Command(this, "channel") {
			
			@Override
			public void action(CommandInvokedEvent event) {
				GraphiteGuild g = event.getGuild();
				String streamerName = (String) event.getOption("twitch-user");
				if(g.getTwitchConfig().getTwitchUserByName(streamerName) == null) {
					DefaultMessage.COMMAND_TWITCH_STREAMER_NOT_FOUND.reply(event);
					return;
				}
				
				GraphiteTwitchUser u = g.getTwitchConfig().getTwitchUserByName(streamerName);
				GraphiteTextChannel tex = (GraphiteTextChannel) event.getOption("channel");
				if(!tex.canWrite()) {
					DefaultMessage.ERROR_BOT_CANNOT_WRITE.reply(event);
					return;
				}
				
				u.setNotificationChannel(tex);
				g.getTwitchConfig().updateTwitchUser(u);
				DefaultMessage.COMMAND_TWITCH_SET_CHANNEL.reply(event, 
						"channel", tex.getName());
			}
			
			@Override
			public List<OptionData> getOptions() {
				return Arrays.asList(
						new OptionData(OptionType.STRING, "twitch-user", "The twitch user u want to change the notification channel", true),
						new OptionData(OptionType.CHANNEL, "channel", "The new channel", true).setChannelTypes(ChannelType.TEXT)
					);
			}
			
		})
		.setDescription(DefaultLocaleString.COMMAND_TWITCH_CHANNEL_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_TWITCH_CHANNEL_USAGE)
		.setPermission(DefaultPermissions.TWITCH_CHANNEL);
		
		addSubCommand(new Command(this, "add") {
			
			@Override
			public void action(CommandInvokedEvent event) {
				GraphiteGuild g = event.getGuild();
				String streamerName = (String) event.getOption("twitch-user");
				
				GraphiteTwitchUser tu = g.getTwitchConfig().getTwitchUserByName(streamerName);
				if(tu != null) {
					DefaultMessage.COMMAND_TWITCH_ALREADY_ADDED.reply(event);
					return;
				}
				
				TwitchUser u = Graphite.getTwitch().getTwitchAPI().getUserByName(streamerName);
				if(u == null) {
					DefaultMessage.COMMAND_TWITCH_INVALID_STREAMER.reply(event);
					return;
				}
				
				GraphiteTextChannel tc = (GraphiteTextChannel) event.getOption("channel");
				g.getTwitchConfig().createTwitchUser(u, tc);
				DefaultMessage.COMMAND_TWITCH_USER_ADDED.reply(event);
			}
			
			@Override
			public List<OptionData> getOptions() {
				return Arrays.asList(
						new OptionData(OptionType.STRING, "twitch-user", "The twitch user u want to add", true),
						new OptionData(OptionType.CHANNEL, "channel", "The channel where the notifications will be send", true).setChannelTypes(ChannelType.TEXT)
					);
			}
			
		})
		.setDescription(DefaultLocaleString.COMMAND_TWITCH_ADD_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_TWITCH_ADD_USAGE)
		.setPermission(DefaultPermissions.TWITCH_ADD);
		
		addSubCommand(new Command(this, "remove") {
			
			@Override
			public void action(CommandInvokedEvent event) {
				GraphiteGuild g = event.getGuild();
				String streamerName = (String) event.getOption("twitch-user");
				GraphiteTwitchUser tu = g.getTwitchConfig().getTwitchUserByName(streamerName);
				if(tu == null) {
					DefaultMessage.COMMAND_TWITCH_STREAMER_NOT_FOUND.reply(event);
					return;
				}
				
				g.getTwitchConfig().removeTwitchUser(tu);
				DefaultMessage.COMMAND_TWITCH_USER_REMOVED.reply(event);
			}
			
			@Override
			public List<OptionData> getOptions() {
				return Arrays.asList(
						new OptionData(OptionType.STRING, "twitch-user", "The twitch user u want to add", true)
					);
			}
			
		})
		.setDescription(DefaultLocaleString.COMMAND_TWITCH_REMOVE_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_TWITCH_REMOVE_USAGE)
		.setPermission(DefaultPermissions.TWITCH_REMOVE);
	}

}

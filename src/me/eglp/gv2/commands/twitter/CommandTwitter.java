package me.eglp.gv2.commands.twitter;

import java.util.Arrays;
import java.util.List;

import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.guild.GraphiteModule;
import me.eglp.gv2.guild.GraphiteTextChannel;
import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.apis.twitter.GraphiteTwitterUser;
import me.eglp.gv2.util.command.Command;
import me.eglp.gv2.util.command.CommandCategory;
import me.eglp.gv2.util.command.CommandInvokedEvent;
import me.eglp.gv2.util.command.ParentCommand;
import me.eglp.gv2.util.lang.DefaultLocaleString;
import me.eglp.gv2.util.lang.DefaultMessage;
import me.eglp.gv2.util.permission.DefaultPermissions;
import me.eglp.twitter.entity.TwitterUser;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class CommandTwitter extends ParentCommand{

	public CommandTwitter() {
		super(GraphiteModule.TWITTER, CommandCategory.TWITTER, "twitter");
		setDescription(DefaultLocaleString.COMMAND_TWITTER_DESCRIPTION);

		addSubCommand(new Command(this, "channel") {

			@Override
			public void action(CommandInvokedEvent event) {
				GraphiteGuild g = event.getGuild();
				String streamerName = (String) event.getOption("twitter-user");
				if(g.getTwitterConfig().getTwitterUserByName(streamerName) == null) {
					DefaultMessage.COMMAND_TWITTER_USER_NOT_FOUND.reply(event);
					return;
				}

				GraphiteTwitterUser u = g.getTwitterConfig().getTwitterUserByName(streamerName);
				GraphiteTextChannel tex = (GraphiteTextChannel) event.getOption("channel");
				if(!tex.canWrite()) {
					DefaultMessage.ERROR_BOT_CANNOT_WRITE.reply(event);
					return;
				}

				u.setNotificationChannel(tex);
				g.getTwitterConfig().updateTwitterUser(u);
				DefaultMessage.COMMAND_TWITTER_SET_CHANNEL.reply(event,
						"channel", tex.getName());
			}

			@Override
			public List<OptionData> getOptions() {
				return Arrays.asList(
						new OptionData(OptionType.STRING, "twitter-user", "The Twitter user you want to change the notification channel", true),
						new OptionData(OptionType.CHANNEL, "channel", "The new channel", true).setChannelTypes(ChannelType.TEXT)
					);
			}

		})
		.setDescription(DefaultLocaleString.COMMAND_TWITTER_CHANNEL_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_TWITTER_CHANNEL_USAGE)
		.setPermission(DefaultPermissions.TWITTER_CHANNEL);

		addSubCommand(new Command(this, "add") {

			@Override
			public void action(CommandInvokedEvent event) {
				GraphiteGuild g = event.getGuild();
				String streamerName = (String) event.getOption("twitter-user");

				GraphiteTwitterUser tu = g.getTwitterConfig().getTwitterUserByName(streamerName);
				if(tu != null) {
					DefaultMessage.COMMAND_TWITTER_ALREADY_ADDED.reply(event);
					return;
				}

				TwitterUser u = Graphite.getTwitter().getTwitterAPI().getUserByUsername(streamerName);
				if(u == null) {
					DefaultMessage.COMMAND_TWITTER_INVALID_USER.reply(event);
					return;
				}

				GraphiteTextChannel tc = (GraphiteTextChannel) event.getOption("channel");
				g.getTwitterConfig().createTwitterUser(u, tc);
				DefaultMessage.COMMAND_TWITTER_USER_ADDED.reply(event);
			}

			@Override
			public List<OptionData> getOptions() {
				return Arrays.asList(
						new OptionData(OptionType.STRING, "twitter-user", "The Twitter user you want to add", true),
						new OptionData(OptionType.CHANNEL, "channel", "The channel where the notifications will be send", true).setChannelTypes(ChannelType.TEXT)
					);
			}

		})
		.setDescription(DefaultLocaleString.COMMAND_TWITTER_ADD_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_TWITTER_ADD_USAGE)
		.setPermission(DefaultPermissions.TWITTER_ADD);

		addSubCommand(new Command(this, "remove") {

			@Override
			public void action(CommandInvokedEvent event) {
				GraphiteGuild g = event.getGuild();
				String streamerName = (String) event.getOption("twitter-user");
				GraphiteTwitterUser tu = g.getTwitterConfig().getTwitterUserByName(streamerName);
				if(tu == null) {
					DefaultMessage.COMMAND_TWITTER_USER_NOT_FOUND.reply(event);
					return;
				}

				g.getTwitterConfig().removeTwitterUser(tu);
				DefaultMessage.COMMAND_TWITTER_USER_REMOVED.reply(event);
			}

			@Override
			public List<OptionData> getOptions() {
				return Arrays.asList(
						new OptionData(OptionType.STRING, "twitter-user", "The Twitter user you want to add", true)
					);
			}

		})
		.setDescription(DefaultLocaleString.COMMAND_TWITTER_REMOVE_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_TWITTER_REMOVE_USAGE)
		.setPermission(DefaultPermissions.TWITTER_REMOVE);
	}

}

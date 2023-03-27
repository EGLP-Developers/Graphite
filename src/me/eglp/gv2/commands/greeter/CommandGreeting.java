package me.eglp.gv2.commands.greeter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import me.eglp.gv2.util.base.guild.GraphiteModule;
import me.eglp.gv2.util.base.guild.GraphiteTextChannel;
import me.eglp.gv2.util.base.guild.config.GuildGreeterConfig;
import me.eglp.gv2.util.command.Command;
import me.eglp.gv2.util.command.CommandCategory;
import me.eglp.gv2.util.command.CommandInvokedEvent;
import me.eglp.gv2.util.command.ParentCommand;
import me.eglp.gv2.util.lang.DefaultLocaleString;
import me.eglp.gv2.util.lang.DefaultMessage;
import me.eglp.gv2.util.permission.DefaultPermissions;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class CommandGreeting extends ParentCommand{

	public CommandGreeting() {
		super(GraphiteModule.GREETER, CommandCategory.GREETER, "greeting");
		setDescription(DefaultLocaleString.COMMAND_GREETING_DESCRIPTION);
		
		addSubCommand(new Command(this, "channel") {
			
			@Override
			public void action(CommandInvokedEvent event) {
				//NONBETA Sent to user?
				GraphiteTextChannel channel = (GraphiteTextChannel) event.getOption("channel");
				GuildGreeterConfig c = event.getGuild().getGreeterConfig();
				c.setGreetingChannel(channel);
				DefaultMessage.COMMAND_GREETING_CHANNEL_SET.reply(event, "channel", channel == null ? "Direct message to user" : channel.getName());
			}
			
			@Override
			public List<OptionData> getOptions() {
				return Arrays.asList(
						new OptionData(OptionType.CHANNEL, "sent-to", "The channel where the notification should be send to", true).setChannelTypes(ChannelType.TEXT)
					);
			}
		})
		.setUsage(DefaultLocaleString.COMMAND_GREETING_CHANNEL_USAGE)
		.setDescription(DefaultLocaleString.COMMAND_GREETING_CHANNEL_DESCRIPTION)
		.setPermission(DefaultPermissions.GREETER_GREETING_CHANNEL);
		
		addSubCommand(new Command(this, "message") {
			
			@Override
			public void action(CommandInvokedEvent event) {
				if(!event.hasOption("message")) {
					GuildGreeterConfig c = event.getGuild().getGreeterConfig();
					DefaultMessage.COMMAND_GREETING_CURRENT_MESSAGE.reply(event, "message", c.getGreetingMessage());
					return;
				}

				String message = (String) event.getOption("message");
				GuildGreeterConfig c = event.getGuild().getGreeterConfig();
				c.setGreetingMessage(message);
				DefaultMessage.COMMAND_GREETING_MESSAGE_SET.reply(event, "message", message);
			}
			
			@Override
			public List<OptionData> getOptions() {
				return Arrays.asList(
						new OptionData(OptionType.STRING, "message", "A new greeting message", false)
					);
			}
			
		})
		.setUsage(DefaultLocaleString.COMMAND_GREETING_MESSAGE_USAGE)
		.setDescription(DefaultLocaleString.COMMAND_GREETING_MESSAGE_DESCRIPTION)
		.setPermission(DefaultPermissions.GREETER_GREETING_MESSAGE);
		
		addSubCommand(new Command(this, "enable") {
			
			@Override
			public void action(CommandInvokedEvent event) {
				GuildGreeterConfig c = event.getGuild().getGreeterConfig();
				if(c.isGreetingEnabled()) {
					DefaultMessage.COMMAND_GREETING_ALREADY_ENABLED.reply(event);
					return;
				}
				c.enableGreeting(true);
				DefaultMessage.COMMAND_GREETING_ENABLED.reply(event);
			}
			
			@Override
			public List<OptionData> getOptions() {
				return Collections.emptyList();
			}
		})
		.setDescription(DefaultLocaleString.COMMAND_GREETING_ENABLE_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_GREETING_ENABLE_USAGE)
		.setPermission(DefaultPermissions.GREETER_GREETING_ENABLE);
		
		addSubCommand(new Command(this, "disable") {
			
			@Override
			public void action(CommandInvokedEvent event) {
				GuildGreeterConfig c = event.getGuild().getGreeterConfig();
				if(!c.isGreetingEnabled()) {
					DefaultMessage.COMMAND_GREETING_ALREADY_DISABLED.reply(event);
					return;
				}
				c.enableGreeting(false);
				DefaultMessage.COMMAND_GREETING_DISABLED.reply(event);
			}
			
			@Override
			public List<OptionData> getOptions() {
				return Collections.emptyList();
			}
		})
		.setDescription(DefaultLocaleString.COMMAND_GREETING_DISABLE_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_GREETING_DISABLE_USAGE)
		.setPermission(DefaultPermissions.GREETER_GREETING_DISABLE);
	}

}

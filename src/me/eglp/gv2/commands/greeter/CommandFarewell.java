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

public class CommandFarewell extends ParentCommand {

	public CommandFarewell() {
		super(GraphiteModule.GREETER, CommandCategory.GREETER, "farewell");
		setDescription(DefaultLocaleString.COMMAND_FAREWELL_DESCRIPTION);
		
		addSubCommand(new Command(this, "channel") {
			
			@Override
			public void action(CommandInvokedEvent event) {
				GraphiteTextChannel ch = (GraphiteTextChannel) event.getOption("channel");
				GuildGreeterConfig c = event.getGuild().getGreeterConfig();
				c.setFarewellChannel(ch);
				DefaultMessage.COMMAND_FAREWELL_CHANNEL_SET.reply(event, "channel", ch.getName());
			}
			
			@Override
			public List<OptionData> getOptions() {
				return Arrays.asList(
						 new OptionData(OptionType.CHANNEL, "channel", "The channel where the notification should be send to", true).setChannelTypes(ChannelType.TEXT)
					);
			}
			
		})
		.setUsage(DefaultLocaleString.COMMAND_FAREWELL_CHANNEL_USAGE)
		.setDescription(DefaultLocaleString.COMMAND_FAREWELL_CHANNEL_DESCRIPTION)
		.setPermission(DefaultPermissions.GREETER_FAREWELL_CHANNEL);
		
		addSubCommand(new Command(this, "message") {
			
			@Override
			public void action(CommandInvokedEvent event) {
				if(!event.hasOption("message")) {
					GuildGreeterConfig c = event.getGuild().getGreeterConfig();
					DefaultMessage.COMMAND_FAREWELL_CURRENT_MESSAGE.reply(event, "message", c.getFarewellMessage());
					return;
				}

				String message = (String) event.getOption("message");
				GuildGreeterConfig c = event.getGuild().getGreeterConfig();
				c.setFarewellMessage(message);
				DefaultMessage.COMMAND_FAREWELL_MESSAGE_SET.reply(event, "message", message);
			}
			
			@Override
			public List<OptionData> getOptions() {
				return Arrays.asList(
						new OptionData(OptionType.STRING, "message", "A new farewell message", false)
					);
			}
			
		})
		.setUsage(DefaultLocaleString.COMMAND_FAREWELL_MESSAGE_USAGE)
		.setDescription(DefaultLocaleString.COMMAND_FAREWELL_MESSAGE_DESCRIPTION)
		.setPermission(DefaultPermissions.GREETER_FAREWELL_MESSAGE);
		
		addSubCommand(new Command(this, "enable") {
			
			@Override
			public void action(CommandInvokedEvent event) {
				GuildGreeterConfig c = event.getGuild().getGreeterConfig();
				if(c.isFarewellEnabled()) {
					DefaultMessage.COMMAND_FAREWELL_ALREADY_ENABLED.reply(event);
					return;
				}
				
				c.enableFarewell(true);
				DefaultMessage.COMMAND_FAREWELL_ENABLED.reply(event);
			}
			
			@Override
			public List<OptionData> getOptions() {
				return Collections.emptyList();
			}
		})
		.setDescription(DefaultLocaleString.COMMAND_FAREWELL_ENABLE_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_FAREWELL_ENABLE_USAGE)
		.setPermission(DefaultPermissions.GREETER_FAREWELL_ENABLE);
		
		addSubCommand(new Command(this, "disable") {
			
			@Override
			public void action(CommandInvokedEvent event) {
				GuildGreeterConfig c = event.getGuild().getGreeterConfig();
				if(!c.isFarewellEnabled()) {
					DefaultMessage.COMMAND_FAREWELL_ALREADY_DISABLED.reply(event);
					return;
				}
				
				c.enableFarewell(false);
				DefaultMessage.COMMAND_FAREWELL_DISABLED.reply(event);
			}
			
			@Override
			public List<OptionData> getOptions() {
				return Collections.emptyList();
			}
		})
		.setDescription(DefaultLocaleString.COMMAND_FAREWELL_DISABLE_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_FAREWELL_DISABLE_USAGE)
		.setPermission(DefaultPermissions.GREETER_FAREWELL_DISABLE);
	}

}

package me.eglp.gv2.commands.moderation;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import me.eglp.gv2.util.base.guild.GraphiteGuild;
import me.eglp.gv2.util.base.guild.GraphiteModule;
import me.eglp.gv2.util.base.guild.GraphiteVoiceChannel;
import me.eglp.gv2.util.base.guild.config.GuildChannelsConfig;
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

public class CommandSupport extends ParentCommand {
	
	public CommandSupport() {
		super(GraphiteModule.MODERATION, CommandCategory.MODERATION, "support");
		setDescription(DefaultLocaleString.COMMAND_SUPPORT_DESCRIPTION);
		
		addSubCommand(new Command(this, "queue") {
			
			@Override
			public void action(CommandInvokedEvent event) {
				GraphiteGuild g = event.getGuild();
				GuildChannelsConfig c = g.getChannelsConfig();
				
				GraphiteVoiceChannel vc = (GraphiteVoiceChannel) event.getOption("queue");
				c.setSupportQueue(vc);
				
				DefaultMessage.COMMAND_SUPPORT_QUEUE_MESSAGE.reply(event, "channel", vc.getName());
			}
			
			@Override
			public List<OptionData> getOptions() {
				return Arrays.asList(
						new OptionData(OptionType.CHANNEL, "queue", "The channel to set as the support queue channel", true).setChannelTypes(ChannelType.VOICE)
					);
			}
		})
		.setDescription(DefaultLocaleString.COMMAND_SUPPORT_QUEUE_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_SUPPORT_QUEUE_USAGE)
		.setPermission(DefaultPermissions.MODERATION_SUPPORT_QUEUE);
		
		addSubCommand(new Command(this, "unsetqueue") {
			
			@Override
			public void action(CommandInvokedEvent event) {
				GraphiteGuild g = event.getGuild();
				GuildChannelsConfig c = g.getChannelsConfig();
				c.unsetSupportQueue();
				DefaultMessage.COMMAND_SUPPORT_UNSETQUEUE_MESSAGE.reply(event);
			}
			
			@Override
			public List<OptionData> getOptions() {
				return Collections.emptyList();
			}
		})
		.setDescription(DefaultLocaleString.COMMAND_SUPPORT_UNSETQUEUE_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_SUPPORT_UNSETQUEUE_USAGE)
		.setPermission(DefaultPermissions.MODERATION_SUPPORT_UNSETQUEUE);
	}

}

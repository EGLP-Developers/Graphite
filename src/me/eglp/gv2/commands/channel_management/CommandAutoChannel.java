package me.eglp.gv2.commands.channel_management;

import java.util.Arrays;
import java.util.List;

import me.eglp.gv2.util.base.guild.GraphiteCategory;
import me.eglp.gv2.util.base.guild.GraphiteGuild;
import me.eglp.gv2.util.base.guild.GraphiteModule;
import me.eglp.gv2.util.base.guild.GraphiteVoiceChannel;
import me.eglp.gv2.util.base.guild.GuildAutoChannel;
import me.eglp.gv2.util.base.guild.config.GuildChannelsConfig;
import me.eglp.gv2.util.command.Command;
import me.eglp.gv2.util.command.CommandCategory;
import me.eglp.gv2.util.command.CommandInvokedEvent;
import me.eglp.gv2.util.lang.DefaultLocaleString;
import me.eglp.gv2.util.lang.DefaultMessage;
import me.eglp.gv2.util.permission.DefaultPermissions;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class CommandAutoChannel extends Command {
	
	public CommandAutoChannel() {
		super(GraphiteModule.CHANNEL_MANAGEMENT, CommandCategory.CHANNEL_MANAGEMENT, "autochannel");
		setDescription(DefaultLocaleString.COMMAND_AUTOCHANNEL_DESCRIPTION);
		setUsage(DefaultLocaleString.COMMAND_AUTOCHANNEL_USAGE);
		setPermission(DefaultPermissions.CHANNEL_AUTOCHANNEL);
		addAlias("autoc");
		addAlias("achannel");
	}
	
	@Override
	public void action(CommandInvokedEvent event) {
		GraphiteGuild g = event.getGuild();
		GuildChannelsConfig c = g.getChannelsConfig();
		
		GraphiteVoiceChannel ch = (GraphiteVoiceChannel) event.getOption("channel");
		if(c.isAutoCreatedChannel(ch)) {
			DefaultMessage.COMMAND_AUTOCHANNEL_AUTOCHANNEL_ERROR.reply(event);
			return;
		}
		
		if(c.isUserChannel(ch)) {
			DefaultMessage.COMMAND_AUTOCHANNEL_USERCHANNEL_ERROR.reply(event);
			return;
		}
		
		GuildAutoChannel ac = c.getAutoChannelByID(ch.getID());
		GraphiteCategory category = ch.getCategory();
		
		if(event.hasOption("category")) {
			category = (GraphiteCategory) event.getOption("category");
		}
		
		if(ac == null) {
			c.createAutoChannel(ch, category);
			DefaultMessage.COMMAND_AUTOCHANNEL_ENABLED.reply(event, "channel_name", ch.getName());
		}else {
			ac.delete();
			DefaultMessage.COMMAND_AUTOCHANNEL_DISABLED.reply(event, "channel_name", ch.getName());
		}
	}

	@Override
	public List<OptionData> getOptions() {
		return Arrays.asList(
				new OptionData(OptionType.CHANNEL, "channel", "Specify a channel that will be automatically copied if anybody joins", true).setChannelTypes(ChannelType.VOICE),
				new OptionData(OptionType.CHANNEL, "category", "Specify a category where autochannel will be created", false).setChannelTypes(ChannelType.CATEGORY)
			);
	}
	
}

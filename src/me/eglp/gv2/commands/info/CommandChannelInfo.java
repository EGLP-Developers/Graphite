package me.eglp.gv2.commands.info;

import java.util.Arrays;
import java.util.List;

import me.eglp.gv2.util.base.guild.GraphiteCategory;
import me.eglp.gv2.util.base.guild.GraphiteGuildChannel;
import me.eglp.gv2.util.base.guild.GraphiteTextChannel;
import me.eglp.gv2.util.base.guild.GraphiteVoiceChannel;
import me.eglp.gv2.util.command.Command;
import me.eglp.gv2.util.command.CommandCategory;
import me.eglp.gv2.util.command.CommandInvokedEvent;
import me.eglp.gv2.util.lang.DefaultLocaleString;
import me.eglp.gv2.util.lang.LocalizedTimeUnit;
import me.eglp.gv2.util.selfcheck.SpecialSelfcheck;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class CommandChannelInfo extends Command{
	
	public CommandChannelInfo() {
		super(null, CommandCategory.INFO, "channelinfo");
		setDescription(DefaultLocaleString.COMMAND_CHANNELINFO_DESCRIPTION);
		setUsage(DefaultLocaleString.COMMAND_CHANNELINFO_USAGE);
		addAlias("cinfo");
	}
	
	@SpecialSelfcheck(needsPermission = false)
	@Override
	public void action(CommandInvokedEvent event) {
		GraphiteGuildChannel channel = (GraphiteGuildChannel) event.getOption("channel");

		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle(DefaultLocaleString.COMMAND_CHANNELINFO_TITLE.getFor(event.getSender(), "channel", channel.getName()));
		eb.addField(DefaultLocaleString.COMMAND_CHANNELINFO_NAME_TITLE.getFor(event.getSender()), channel.getName(), true);
		eb.addField(DefaultLocaleString.COMMAND_CHANNELINFO_ID_TITLE.getFor(event.getSender()), channel.getJDAChannel().getId(), true);
		
		String parentString = (channel.getCategory() != null ? channel.getCategory().getName() : DefaultLocaleString.COMMAND_CHANNELINFO_PARENT_NO_PARENT.getFor(event.getSender()));
		if(channel instanceof GraphiteVoiceChannel) {
			GraphiteVoiceChannel vc = (GraphiteVoiceChannel) channel;
			VoiceChannel jvc = vc.getJDAChannel();
			eb.addField(DefaultLocaleString.COMMAND_CHANNELINFO_PARENT_TITLE.getFor(event.getSender()), parentString, true);
			
			eb.addBlankField(false);
			
			eb.addField(DefaultLocaleString.COMMAND_CHANNELINFO_USER_LIMIT_TITLE.getFor(event.getSender()), jvc.getUserLimit() == 0 ? DefaultLocaleString.COMMAND_CHANNELINFO_USER_LIMIT_UNLIMITED.getFor(event.getSender()) : String.valueOf(jvc.getUserLimit()), true);
			eb.addField(DefaultLocaleString.COMMAND_CHANNELINFO_BITRATE_TITLE.getFor(event.getSender()), String.valueOf(jvc.getBitrate()), true);
			eb.addField(DefaultLocaleString.COMMAND_CHANNELINFO_REGION_TITLE.getFor(event.getSender()), jvc.getRegion().getName(), true);
		}else if(channel instanceof GraphiteTextChannel) {
			GraphiteTextChannel tc = (GraphiteTextChannel) channel;
			TextChannel jtc = tc.getJDAChannel();
			eb.addField(DefaultLocaleString.COMMAND_CHANNELINFO_PARENT_TITLE.getFor(event.getSender()), parentString, true);

			eb.addBlankField(false);
			
			eb.addField(DefaultLocaleString.COMMAND_CHANNELINFO_TOPIC_TITLE.getFor(event.getSender()), jtc.getTopic() == null ? DefaultLocaleString.COMMAND_CHANNELINFO_TOPIC_NO_TOPIC.getFor(event.getSender()) : jtc.getTopic(), false);
			eb.addField(DefaultLocaleString.COMMAND_CHANNELINFO_SLOWMODE_TITLE.getFor(event.getSender()), jtc.getSlowmode() == 0 ? DefaultLocaleString.COMMAND_CHANNELINFO_SLOWMODE_OFF.getFor(event.getSender()) : LocalizedTimeUnit.formatTime(event.getSender(), jtc.getSlowmode() * 1000L), false);
		}else if(channel instanceof GraphiteCategory) {
			// Nothing extra
		}
		
		event.reply(eb.build());
	}

	@Override
	public List<OptionData> getOptions() {
		return Arrays.asList(
				new OptionData(OptionType.CHANNEL, "channel", "The channel to view information about", true)
			);
	}

}

package me.eglp.gv2.commands.moderation;

import java.util.Arrays;
import java.util.List;

import me.eglp.gv2.util.base.guild.GraphiteAudioChannel;
import me.eglp.gv2.util.base.guild.GraphiteGuild;
import me.eglp.gv2.util.base.guild.GraphiteMember;
import me.eglp.gv2.util.base.guild.GraphiteModule;
import me.eglp.gv2.util.base.guild.GraphiteVoiceChannel;
import me.eglp.gv2.util.base.guild.GuildJail;
import me.eglp.gv2.util.base.guild.config.GuildChannelsConfig;
import me.eglp.gv2.util.base.guild.config.GuildModerationConfig;
import me.eglp.gv2.util.base.user.GraphiteUser;
import me.eglp.gv2.util.command.Command;
import me.eglp.gv2.util.command.CommandCategory;
import me.eglp.gv2.util.command.CommandInvokedEvent;
import me.eglp.gv2.util.lang.DefaultLocaleString;
import me.eglp.gv2.util.lang.DefaultMessage;
import me.eglp.gv2.util.lang.GraphiteTimeParser;
import me.eglp.gv2.util.lang.LocalizedTimeUnit;
import me.eglp.gv2.util.permission.DefaultPermissions;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class CommandTempJail extends Command{
	
	public CommandTempJail() {
		super(GraphiteModule.MODERATION, CommandCategory.MODERATION, "tempjail");
		setDescription(DefaultLocaleString.COMMAND_TEMPJAIL_DESCRIPTION);
		setUsage(DefaultLocaleString.COMMAND_TEMPJAIL_USAGE);
		setPermission(DefaultPermissions.MODERATION_TEMPJAIL);
		requirePermissions(Permission.VOICE_MOVE_OTHERS);
	}
	
	@Override
	public void action(CommandInvokedEvent event) {
		GraphiteGuild g = event.getGuild();
		GraphiteUser user = (GraphiteUser) event.getOption("user");
		GraphiteMember mem = g.getMember(user);
		if(mem == null) {
			DefaultMessage.ERROR_NOT_A_MEMBER.reply(event);
			return;
		}
		
		if(mem.isBot()) {
			DefaultMessage.ERROR_IS_BOT.reply(event);
			return;
		}
		
		GraphiteAudioChannel aCh = mem.getCurrentAudioChannel();
		
		if(aCh == null) {
			DefaultMessage.ERROR_NOT_IN_AUDIOCHANNEL.reply(event);
			return;
		}
		
		GuildChannelsConfig c = g.getChannelsConfig();
		if(aCh.isVoiceChannel() && c.isAutoCreatedChannel((GraphiteVoiceChannel) aCh)) {
			DefaultMessage.COMMAND_JAIL_ERROR_AUTOCHANNEL.reply(event);
			return;
		}
		
		if(aCh.isVoiceChannel() && c.isUserChannel((GraphiteVoiceChannel) aCh)) {
			DefaultMessage.COMMAND_JAIL_ERROR_USERCHANNEL.reply(event);
			return;
		}
		
		GuildModerationConfig mC = g.getModerationConfig();
		if(mC.isJailed(mem)) {
			DefaultMessage.COMMAND_TEMPJAIL_ALREADY_JAILED.reply(event);
			return;
		}
		
		if(event.hasOption("channel")) {
			aCh = (GraphiteAudioChannel) event.getOption("channel");
		}
		
		long duration = GraphiteTimeParser.parseShortDuration((String) event.getOption("duration"));
		if(duration == -1) {
			DefaultMessage.ERROR_INVALID_DURATION.reply(event);
			return;
		}
		
		if(duration < 1000 * 60) {
			DefaultMessage.ERROR_MINIMUM_TEMP_DURATION.reply(event);
			return;
		}
		
		String reason = (String) event.getOption("reason");
		
		GuildJail j = mC.createTempJail(mem, aCh, duration, event.getMember(), reason);
		
		DefaultMessage.COMMAND_JAIL_JAILED.sendMessage(mem.openPrivateChannel(), "attempts", String.valueOf(j.getLeaveAttempts()), "max_attempts", "3");
		DefaultMessage.COMMAND_TEMPJAIL_SUCCESS.reply(event,
				"user", mem.getAsMention(),
				"duration", LocalizedTimeUnit.formatTime(g, duration),
				"channel", aCh.getName());
	}

	@Override
	public List<OptionData> getOptions() {
		return Arrays.asList(
				new OptionData(OptionType.USER, "user", "The user you want to temporarily jail", true),
				new OptionData(OptionType.STRING, "duration", "How long you want to temporarily jail the user", true),
				new OptionData(OptionType.CHANNEL, "channel", "The channel you want to jail the user in", false).setChannelTypes(ChannelType.VOICE, ChannelType.STAGE),
				new OptionData(OptionType.STRING, "reason", "A short reason why you want to temporarily jail this user", false)
			);
	}

}

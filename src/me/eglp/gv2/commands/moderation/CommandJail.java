package me.eglp.gv2.commands.moderation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import me.eglp.gv2.util.permission.DefaultPermissions;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class CommandJail extends Command{
	
	public static Map<String, String> jail = new HashMap<>();
	
	public CommandJail() {
		super(GraphiteModule.MODERATION, CommandCategory.MODERATION, "jail");
		setDescription(DefaultLocaleString.COMMAND_JAIL_DESCRIPTION);
		setUsage(DefaultLocaleString.COMMAND_JAIL_USAGE);
		setPermission(DefaultPermissions.MODERATION_JAIL);
		requirePermissions(Permission.VOICE_MOVE_OTHERS);
	}
	
	@Override
	public void action(CommandInvokedEvent event) {
		GraphiteGuild g = event.getGuild();
		GraphiteUser u = (GraphiteUser) event.getOption("user");
		GraphiteMember mem = g.getMember(u);
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
			DefaultMessage.COMMAND_JAIL_ERROR_ALREADY_JAILED.reply(event);
			return;
		}
		
		if(event.hasOption("channel")) {
			aCh = (GraphiteAudioChannel) event.getOption("channel");
		}
		
		String r = (String) event.getOption("reason");
		GuildJail j = mC.createJail(mem, aCh, event.getMember(), r);
		
		DefaultMessage.COMMAND_JAIL_JAILED.sendMessage(mem.openPrivateChannel(), "attempts", String.valueOf(j.getLeaveAttempts()), "max_attempts", "3");
		DefaultMessage.COMMAND_JAIL_SUCCESS.reply(event, "channel", aCh.getName());
	}

	@Override
	public List<OptionData> getOptions() {
		return Arrays.asList(
				new OptionData(OptionType.USER, "user", "The user you want to jail", true),
				new OptionData(OptionType.CHANNEL, "channel", "The channel you want to jail the member to", false).setChannelTypes(ChannelType.VOICE, ChannelType.STAGE),
				new OptionData(OptionType.STRING, "reason", "A short reason why you want to jail this user", false)
			);
	}

}

package me.eglp.gv2.commands.moderation;

import java.util.Arrays;
import java.util.List;

import me.eglp.gv2.util.base.guild.GraphiteGuild;
import me.eglp.gv2.util.base.guild.GraphiteMember;
import me.eglp.gv2.util.base.guild.GraphiteModule;
import me.eglp.gv2.util.base.guild.temporary_actions.GuildTempVoiceMute;
import me.eglp.gv2.util.base.user.GraphiteUser;
import me.eglp.gv2.util.command.CommandCategory;
import me.eglp.gv2.util.command.CommandInvokedEvent;
import me.eglp.gv2.util.command.Command;
import me.eglp.gv2.util.emote.JDAEmote;
import me.eglp.gv2.util.lang.DefaultLocaleString;
import me.eglp.gv2.util.lang.DefaultMessage;
import me.eglp.gv2.util.permission.DefaultPermissions;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class CommandVoiceUnmute extends Command{
	
	public CommandVoiceUnmute() {
		super(GraphiteModule.MODERATION, CommandCategory.MODERATION, "voiceunmute");
		addAlias("vunmute");
		setDescription(DefaultLocaleString.COMMAND_VOICEUNMUTE_DESCRIPTION);
		setUsage(DefaultLocaleString.COMMAND_VOICEUNMUTE_USAGE);
		setPermission(DefaultPermissions.MODERATION_VOICEUNMUTE);
		requirePermissions(Permission.VOICE_MUTE_OTHERS);
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
		
		if(!g.getSelfMember().canInteract(mem)) {
			DefaultMessage.ERROR_CANT_INTERACT_MEMBER.reply(event);
			return;
		}
		
		if(!mem.isMuted()) {
			DefaultMessage.COMMAND_UNMUTE_NOT_MUTED.reply(event);
			return;
		}
		
		// NONBETA: Remove temp voice mute
		String r = (String) event.getOption("reason");
		GuildTempVoiceMute tb = event.getGuild().getTemporaryActionsConfig().getTempMuteByUserID(mem.getID());
		if(tb != null) tb.remove(event.getMember(), r);
		mem.unmute();
		
		event.react(JDAEmote.WHITE_CHECK_MARK);
	}

	@Override
	public List<OptionData> getOptions() {
		return Arrays.asList(
				new OptionData(OptionType.USER, "user", "The user you want to unmute", true),
				new OptionData(OptionType.STRING, "reason", "A short reason why you want to unmute this user", false)
			);
	}
	
}

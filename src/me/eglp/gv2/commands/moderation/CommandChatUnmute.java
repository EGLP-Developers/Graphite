package me.eglp.gv2.commands.moderation;

import java.util.Arrays;
import java.util.List;

import me.eglp.gv2.util.base.guild.GraphiteGuild;
import me.eglp.gv2.util.base.guild.GraphiteMember;
import me.eglp.gv2.util.base.guild.GraphiteModule;
import me.eglp.gv2.util.base.guild.config.GuildModerationConfig;
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

public class CommandChatUnmute extends Command{
	
	public CommandChatUnmute() {
		super(GraphiteModule.MODERATION, CommandCategory.MODERATION, "chatunmute");
		addAlias("cunmute");
		setDescription(DefaultLocaleString.COMMAND_CHATUNMUTE_DESCRIPTION);
		setUsage(DefaultLocaleString.COMMAND_CHATUNMUTE_USAGE);
		setPermission(DefaultPermissions.MODERATION_CHATUNMUTE);
		requirePermissions(Permission.MANAGE_ROLES);
	}
	
	@Override
	public void action(CommandInvokedEvent event) {
		GraphiteGuild g = event.getGuild();
		GraphiteUser u = (GraphiteUser) event.getOption("user");
		GraphiteMember mem = g.getMember(u);
		
		if(!g.getSelfMember().canInteract(mem)) {
			DefaultMessage.ERROR_CANT_INTERACT_MEMBER.reply(event);
			return;
		}
		
		GuildModerationConfig c = g.getModerationConfig();
		if(!c.isChatMuted(mem)) {
			DefaultMessage.COMMAND_UNMUTE_NOT_MUTED.reply(event);
			return;
		}
		
		String r = (String) event.getOption("reason");
		c.removeChatMute(c.getChatMute(mem), event.getMember(), r);
		
		event.react(JDAEmote.WHITE_CHECK_MARK);
	}

	@Override
	public List<OptionData> getOptions() {
		return Arrays.asList(
				new OptionData(OptionType.USER, "user", "The user you want to chatunmute", true),
				new OptionData(OptionType.STRING, "reason", "A short reason why you want to unmute this user", false)
			);
	}

}

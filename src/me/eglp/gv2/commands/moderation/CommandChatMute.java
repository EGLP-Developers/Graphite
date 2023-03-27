package me.eglp.gv2.commands.moderation;

import java.util.Arrays;
import java.util.List;

import me.eglp.gv2.util.base.guild.GraphiteGuild;
import me.eglp.gv2.util.base.guild.GraphiteMember;
import me.eglp.gv2.util.base.guild.GraphiteModule;
import me.eglp.gv2.util.base.user.GraphiteUser;
import me.eglp.gv2.util.command.CommandCategory;
import me.eglp.gv2.util.command.CommandInvokedEvent;
import me.eglp.gv2.util.command.Command;
import me.eglp.gv2.util.lang.DefaultLocaleString;
import me.eglp.gv2.util.lang.DefaultMessage;
import me.eglp.gv2.util.permission.DefaultPermissions;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class CommandChatMute extends Command {
	
	public CommandChatMute() {
		super(GraphiteModule.MODERATION, CommandCategory.MODERATION, "chatmute");
		addAlias("cmute");
		setDescription(DefaultLocaleString.COMMAND_CHATMUTE_DESCRIPTION);
		setUsage(DefaultLocaleString.COMMAND_CHATMUTE_USAGE);
		setPermission(DefaultPermissions.MODERATION_CHATMUTE);
		requirePermissions(Permission.MANAGE_ROLES);
	}
	
	@Override
	public void action(CommandInvokedEvent event) {
		GraphiteGuild g = event.getGuild();
		GraphiteUser u = (GraphiteUser) event.getOption("user");
		GraphiteMember mem = g.getMember(u);
		String reason = (String) event.getOption("reason");
		
		if(!g.getSelfMember().canInteract(mem)) {
			DefaultMessage.ERROR_CANT_INTERACT_MEMBER.reply(event);
			return;
		}
		
		if(g.getModerationConfig().isChatMuted(mem)) {
			DefaultMessage.ERROR_ALREADY_MUTED.reply(event);
			return;
		}
		
		g.getModerationConfig().createChatMute(mem, event.getMember(), reason);
		DefaultMessage.COMMAND_CHATMUTE_SUCCESS.reply(event, 
				"user", mem.getName(),
				"reason", reason == null ? "No reason" : reason);
	}

	@Override
	public List<OptionData> getOptions() {
		return Arrays.asList(
				new OptionData(OptionType.USER, "user", "The user you want to chatmute", true),
				new OptionData(OptionType.STRING, "reason", "The reason for the chatmute", false)
			);
	}

}

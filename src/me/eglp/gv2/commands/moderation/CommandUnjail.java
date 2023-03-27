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
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class CommandUnjail extends Command{
	
	public CommandUnjail() {
		super(GraphiteModule.MODERATION, CommandCategory.MODERATION, "unjail");
		setDescription(DefaultLocaleString.COMMAND_UNJAIL_DESCRIPTION);
		setUsage(DefaultLocaleString.COMMAND_UNJAIL_USAGE);
		setPermission(DefaultPermissions.MODERATION_UNJAIL);
	}
	
	@Override
	public void action(CommandInvokedEvent event) {
		GraphiteUser user = (GraphiteUser) event.getOption("user");
		GraphiteMember mem = event.getGuild().getMember(user);
		if(mem == null) {
			DefaultMessage.ERROR_NOT_A_MEMBER.reply(event);
			return;
		}
		
		GraphiteGuild g = event.getGuild();
		GuildModerationConfig c = g.getModerationConfig();
		if(!c.isJailed(mem)) {
			DefaultMessage.COMMAND_UNJAIL_NOT_JAILED.reply(event);
			return;
		}

		String r = (String) event.getOption("reason");
		c.removeJailByMemberID(mem.getID(), event.getMember(), r);
		
		event.react(JDAEmote.WHITE_CHECK_MARK);
	}

	@Override
	public List<OptionData> getOptions() {
		return Arrays.asList(
				new OptionData(OptionType.USER, "user", "The user you want to unjail", true),
				new OptionData(OptionType.STRING, "reason", "A short reason why you want to unjail this user", false)
			);
	}

}

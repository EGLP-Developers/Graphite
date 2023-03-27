package me.eglp.gv2.commands.moderation;

import java.util.Arrays;
import java.util.List;

import me.eglp.gv2.util.base.guild.GraphiteMember;
import me.eglp.gv2.util.base.guild.GraphiteModule;
import me.eglp.gv2.util.base.guild.temporary_actions.GuildTempBan;
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

public class CommandUnban extends Command{
	
	public CommandUnban() {
		super(GraphiteModule.MODERATION, CommandCategory.MODERATION, "unban");
		setDescription(DefaultLocaleString.COMMAND_UNBAN_DESCRIPTION);
		setUsage(DefaultLocaleString.COMMAND_UNBAN_USAGE);
		setPermission(DefaultPermissions.MODERATION_UNBAN);
		requirePermissions(Permission.BAN_MEMBERS);
	}
	
	@Override
	public void action(CommandInvokedEvent event) {
		GraphiteUser user = (GraphiteUser) event.getOption("user");
		GraphiteMember mem = event.getGuild().getMember(user);
		if(mem == null) {
			DefaultMessage.ERROR_NOT_A_MEMBER.reply(event);
			return;
		}
		
		if(!event.getGuild().getSelfMember().canInteract(mem)) {
			DefaultMessage.ERROR_CANT_INTERACT_MEMBER.reply(event);
			return;
		}
		
		if(!mem.isBanned()) {
			DefaultMessage.COMMAND_UNBAN_NOT_BANNED.reply(event);
			return;
		}

		String r = (String) event.getOption("reason");
		GuildTempBan tb = event.getGuild().getTemporaryActionsConfig().getTempBanByUserID(mem.getID());
		if(tb != null) tb.remove(event.getMember(), r);
		mem.unban();
		
		event.react(JDAEmote.WHITE_CHECK_MARK);
	}

	@Override
	public List<OptionData> getOptions() {
		return Arrays.asList(
				new OptionData(OptionType.USER, "user", "The user you want to unban", true),
				new OptionData(OptionType.STRING, "reason", "A short reason why you want to unban this user", false)
			);
	}

}

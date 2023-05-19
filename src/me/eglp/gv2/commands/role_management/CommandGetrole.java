package me.eglp.gv2.commands.role_management;

import java.util.Arrays;
import java.util.List;

import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.guild.GraphiteModule;
import me.eglp.gv2.guild.GraphiteRole;
import me.eglp.gv2.guild.config.GuildRolesConfig;
import me.eglp.gv2.util.command.Command;
import me.eglp.gv2.util.command.CommandCategory;
import me.eglp.gv2.util.command.CommandInvokedEvent;
import me.eglp.gv2.util.lang.DefaultLocaleString;
import me.eglp.gv2.util.lang.DefaultMessage;
import me.eglp.gv2.util.selfcheck.SpecialSelfcheck;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class CommandGetrole extends Command{

	public CommandGetrole() {
		super(GraphiteModule.ROLE_MANAGEMENT, CommandCategory.ROLE_MANAGEMENT, "getrole");
		setDescription(DefaultLocaleString.COMMAND_GETROLE_DESCRIPTION);
		setUsage(DefaultLocaleString.COMMAND_GETROLE_USAGE);
		requirePermissions(Permission.MANAGE_ROLES);
	}

	@SpecialSelfcheck(needsPermission = false)
	@Override
	public void action(CommandInvokedEvent event) {
		GraphiteRole rol = (GraphiteRole) event.getOption("role");
		if(!event.getGuild().getSelfMember().canInteract(rol)) {
			DefaultMessage.ERROR_CANT_INTERACT_ROLE.reply(event);
			return;
		}
		if(!event.getGuild().getSelfMember().canInteract(event.getMember())) {
			DefaultMessage.ERROR_CANT_INTERACT_MEMBER.reply(event);
			return;
		}
		GraphiteGuild g = event.getGuild();
		GuildRolesConfig c = g.getRolesConfig();
		if(!c.isRoleAccessible(rol)) {
			DefaultMessage.COMMAND_GETROLE_ROLE_NOT_ACCESSIBLE.reply(event);
			return;
		}
		g.addRoleToMember(event.getMember(), rol);
		DefaultMessage.COMMAND_GETROLE_SUCCESS.reply(event,
				"role", rol.getName());
	}

	@Override
	public List<OptionData> getOptions() {
		return Arrays.asList(
				new OptionData(OptionType.ROLE, "role", "The role you want to get", true)
			);
	}

}

package me.eglp.gv2.commands.moderation;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import me.eglp.gv2.util.command.CommandCategory;
import me.eglp.gv2.util.command.CommandInvokedEvent;
import me.eglp.gv2.util.command.ParentCommand;
import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.guild.GraphiteModule;
import me.eglp.gv2.guild.GraphiteRole;
import me.eglp.gv2.guild.config.GuildRolesConfig;
import me.eglp.gv2.util.command.Command;
import me.eglp.gv2.util.lang.DefaultLocaleString;
import me.eglp.gv2.util.lang.DefaultMessage;
import me.eglp.gv2.util.permission.DefaultPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class CommandModRole extends ParentCommand{

	public CommandModRole() {
		super(GraphiteModule.MODERATION, CommandCategory.MODERATION, "modrole");
		setDescription(DefaultLocaleString.COMMAND_MODROLE_DESCRIPTION);

		addSubCommand(new Command(this, "add") {

			@Override
			public void action(CommandInvokedEvent event) {
				GraphiteRole rol = (GraphiteRole) event.getOption("role");
				GraphiteGuild g = event.getGuild();
				GuildRolesConfig c = g.getRolesConfig();
				if(!g.getSelfMember().canInteract(rol)) {
					DefaultMessage.ERROR_CANT_INTERACT_MEMBER.reply(event);
					return;
				}

				if(c.isModeratorRole(rol)) {
					DefaultMessage.COMMAND_MODROLE_ALREADY_ADDED.reply(event, "role", rol.getName());
					return;
				}

				c.addModeratorRole(rol);

				DefaultMessage.COMMAND_MODROLE_ADDED.reply(event, "role", rol.getName());
			}

			@Override
			public List<OptionData> getOptions() {
				return Arrays.asList(
						new OptionData(OptionType.ROLE, "role", "The role you want to add as moderation role")
					);
			}

		})
		.setDescription(DefaultLocaleString.COMMAND_MODROLE_ADD_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_MODROLE_ADD_USAGE)
		.setPermission(DefaultPermissions.ROLE_MODROLE_ADD);

		addSubCommand(new Command(this, "remove") {

			@Override
			public void action(CommandInvokedEvent event) {
				GraphiteRole rol = (GraphiteRole) event.getOption("role");
				GraphiteGuild g = event.getGuild();
				GuildRolesConfig c = g.getRolesConfig();

				if(!g.getSelfMember().canInteract(rol)) {
					DefaultMessage.ERROR_CANT_INTERACT_ROLE.reply(event);
					return;
				}

				if(c.getModeratorRoles().isEmpty()) {
					DefaultMessage.COMMAND_MODROLE_NO_ROLES.reply(event);
					return;
				}

				if(!c.isModeratorRole(rol)) {
					DefaultMessage.COMMAND_MODROLE_NOT_LISTED.reply(event);
					return;
				}

				c.removeModeratorRole(rol);
				DefaultMessage.COMMAND_MODROLE_REMOVED.reply(event, "role", rol.getName());
			}

			@Override
			public List<OptionData> getOptions() {
				return Arrays.asList(
						new OptionData(OptionType.ROLE, "role", "The role you want to add as moderation role")
					);
			}
		})
		.setDescription(DefaultLocaleString.COMMAND_MODROLE_REMOVE_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_MODROLE_REMOVE_USAGE)
		.setPermission(DefaultPermissions.ROLE_MODROLE_REMOVE);

		addSubCommand(new Command(this, "list") {

			@Override
			public void action(CommandInvokedEvent event) {
				List<GraphiteRole> roles = event.getGuild().getRolesConfig().getModeratorRoles();
				if(roles.isEmpty()) {
					DefaultMessage.COMMAND_MODROLE_LIST_NO_ROLES.reply(event);
					return;
				}
				String m = roles.stream().map(r -> r.getName()).collect(Collectors.joining("\n"));
				event.reply("```" + m + "```");
			}

			@Override
			public List<OptionData> getOptions() {
				return Collections.emptyList();
			}

		})
		.setDescription(DefaultLocaleString.COMMAND_MODROLE_LIST_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_MODROLE_LIST_USAGE)
		.setPermission(DefaultPermissions.ROLE_MODROLE_LIST);
	}

}

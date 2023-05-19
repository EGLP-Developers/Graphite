package me.eglp.gv2.commands.role_management;

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
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class CommandAutorole extends ParentCommand{

	public CommandAutorole() {
		super(GraphiteModule.ROLE_MANAGEMENT, CommandCategory.ROLE_MANAGEMENT, "autorole");
		setDescription(DefaultLocaleString.COMMAND_AUTOROLE_DESCRIPTION);

		addSubCommand(new Command(this, "add") {

			@Override
			public void action(CommandInvokedEvent event) {
				GraphiteRole rol = (GraphiteRole) event.getOption("role");
				if(!event.getMember().canInteract(rol)) {
					DefaultMessage.ERROR_CANT_INTERACT_ROLE.reply(event);
					return;
				}
				GraphiteGuild g = event.getGuild();
				GuildRolesConfig c = g.getRolesConfig();
				if(c.isRoleAuto(rol)) {
					DefaultMessage.COMMAND_AUTOROLE_ALREADY_ADDED.reply(event);
					return;
				}
				c.addAutoRole(rol);
				DefaultMessage.COMMAND_AUTOROLE_ADDED_AUTOROLE.reply(event,
						"role", rol.getName());
			}

			@Override
			public List<OptionData> getOptions() {
				return Arrays.asList(
						new OptionData(OptionType.ROLE, "role", "The role you want to give automatically to new users", true)
					);
			}

		})
		.setDescription(DefaultLocaleString.COMMAND_AUTOROLE_ADD_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_AUTOROLE_ADD_USAGE)
		.setPermission(DefaultPermissions.ROLE_AUTOROLE_ADD);

		addSubCommand(new Command(this, "remove") {

			@Override
			public void action(CommandInvokedEvent event) {
				GraphiteRole rol = (GraphiteRole) event.getOption("role");
				if(!event.getMember().canInteract(rol)) {
					DefaultMessage.ERROR_CANT_INTERACT_ROLE.reply(event);
					return;
				}
				GraphiteGuild g = event.getGuild();
				GuildRolesConfig c = g.getRolesConfig();
				if(!c.isRoleAuto(rol)) {
					DefaultMessage.COMMAND_AUTOROLE_ALREADY_REMOVED.reply(event);
					return;
				}
				c.removeAutoRole(rol);
				DefaultMessage.COMMAND_AUTOROLE_REMOVED_AUTOROLE.reply(event,
						"role", rol.getName());
			}

			@Override
			public List<OptionData> getOptions() {
				return Arrays.asList(
						new OptionData(OptionType.ROLE, "role", "The role you want to remove", true)
					);
			}

		})
		.setDescription(DefaultLocaleString.COMMAND_AUTOROLE_REMOVE_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_AUTOROLE_REMOVE_USAGE)
		.setPermission(DefaultPermissions.ROLE_AUTOROLE_REMOVE);

		addSubCommand(new Command(this, "list") {

			@Override
			public void action(CommandInvokedEvent event) {
				GraphiteGuild g = event.getGuild();
				List<GraphiteRole> autoRoles = g.getRolesConfig().getAutoRoles();
				if(autoRoles.isEmpty()) {
					DefaultMessage.COMMAND_AUTOROLE_NO_AUTOROLES.reply(event);
					return;
				}
				EmbedBuilder eb = new EmbedBuilder();
				eb.addField(DefaultLocaleString.COMMAND_AUTOROLE_LIST_TITLE.getFor(event.getSender()),
							autoRoles.stream().map(m -> m.getName()).collect(Collectors.joining(", ")),
							true);
				event.reply(eb.build());
			}

			@Override
			public List<OptionData> getOptions() {
				return Collections.emptyList();
			}

		})
		.setDescription(DefaultLocaleString.COMMAND_AUTOROLE_LIST_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_AUTOROLE_LIST_USAGE)
		.setPermission(DefaultPermissions.ROLE_AUTOROLE_LIST);
	}

}

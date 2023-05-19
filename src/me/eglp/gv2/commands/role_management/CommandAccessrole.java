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

public class CommandAccessrole extends ParentCommand{

	public CommandAccessrole() {
		super(GraphiteModule.ROLE_MANAGEMENT, CommandCategory.ROLE_MANAGEMENT, "accessrole");
		setDescription(DefaultLocaleString.COMMAND_ACCESSROLE_DESCRIPTION);

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
				if(c.isRoleAccessible(rol)) {
					DefaultMessage.COMMAND_ACCESSROLE_ALREADY_ACCESSIBLE.reply(event);
					return;
				}
				c.addAccessibleRole(rol);
				DefaultMessage.COMMAND_ACCESSROLE_ADDED_ACCESSIBLE_ROLE.reply(event,
						"role", rol.getAsMention(),
						"everyone", g.getJDAGuild().getPublicRole().getAsMention());
			}

			@Override
			public List<OptionData> getOptions() {
				return Arrays.asList(
						new OptionData(OptionType.ROLE, "role", "The role you want to make accessible", true)
					);
			}

		})
		.setDescription(DefaultLocaleString.COMMAND_ACCESSROLE_ADD_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_ACCESSROLE_ADD_USAGE)
		.setPermission(DefaultPermissions.ROLE_ACCESSROLE_ADD);

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
				if(!c.isRoleAccessible(rol)) {
					DefaultMessage.COMMAND_ACCESSROLE_ALREADY_REMOVED.reply(event);
					return;
				}
				c.removeAccessibleRole(rol);
				DefaultMessage.COMMAND_ACCESSROLE_REMOVED_ACCESSIBLE_ROLE.reply(event,
						"role", rol.getAsMention());
			}

			@Override
			public List<OptionData> getOptions() {
				return Arrays.asList(
						new OptionData(OptionType.ROLE, "role", "The role you want to remove from accessible roles", true)
					);
			}

		})
		.setDescription(DefaultLocaleString.COMMAND_ACCESSROLE_REMOVE_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_ACCESSROLE_REMOVE_USAGE)
		.setPermission(DefaultPermissions.ROLE_ACCESSROLE_REMOVE);

		addSubCommand(new Command(this, "list") {

			@Override
			public void action(CommandInvokedEvent event) {
				GraphiteGuild g = event.getGuild();
				List<GraphiteRole> accessibleRoles = g.getRolesConfig().getAccessibleRoles();
				if(accessibleRoles.isEmpty()) {
					DefaultMessage.COMMAND_ACCESSROLE_NO_ACCESSROLES.reply(event);
					return;
				}
				EmbedBuilder eb = new EmbedBuilder();
				eb.addField(DefaultLocaleString.COMMAND_ACCESSROLE_LIST_FIELD_TITLE.getFor(event.getSender()),
							accessibleRoles.stream().map(m -> m.getAsMention()).collect(Collectors.joining(", ")),
							true);
				event.reply(eb.build());
			}

			@Override
			public List<OptionData> getOptions() {
				return Collections.emptyList();
			}

		})
		.setDescription(DefaultLocaleString.COMMAND_ACCESSROLE_LIST_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_ACCESSROLE_LIST_USAGE)
		.setPermission(DefaultPermissions.ROLE_ACCESSROLE_LIST);
	}

}

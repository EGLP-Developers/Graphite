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

public class CommandBotrole extends ParentCommand{

	public CommandBotrole() {
		super(GraphiteModule.ROLE_MANAGEMENT, CommandCategory.ROLE_MANAGEMENT, "botrole");
		setDescription(DefaultLocaleString.COMMAND_BOTROLE_DESCRIPTION);

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
				if(c.isBotRole(rol)) {
					DefaultMessage.COMMAND_BOTROLE_ALREADY_ADDED.reply(event);
					return;
				}
				c.addBotRole(rol);
				DefaultMessage.COMMAND_BOTROLE_ADDED_BOTROLE.reply(event,
						"role", rol.getName());
			}

			@Override
			public List<OptionData> getOptions() {
				return Arrays.asList(
						new OptionData(OptionType.ROLE, "role", "The role you want to add to botroles", true)
					);
			}

		})
		.setDescription(DefaultLocaleString.COMMAND_BOTROLE_ADD_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_BOTROLE_ADD_USAGE)
		.setPermission(DefaultPermissions.ROLE_BOTROLE_ADD);

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
				if(!c.isBotRole(rol)) {
					DefaultMessage.COMMAND_BOTROLE_ALREADY_REMOVED.reply(event);
					return;
				}
				c.removeBotRole(rol);
				DefaultMessage.COMMAND_BOTROLE_REMOVED_BOTROLE.reply(event,
						"role", rol.getName());
			}

			@Override
			public List<OptionData> getOptions() {
				return Arrays.asList(
						new OptionData(OptionType.ROLE, "role", "The role you want to remove from botroles", true)
					);
			}

		})
		.setDescription(DefaultLocaleString.COMMAND_BOTROLE_REMOVE_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_BOTROLE_REMOVE_USAGE)
		.setPermission(DefaultPermissions.ROLE_BOTROLE_REMOVE);

		addSubCommand(new Command(this, "list") {

			@Override
			public void action(CommandInvokedEvent event) {
				GraphiteGuild g = event.getGuild();
				List<GraphiteRole> botRoles = g.getRolesConfig().getBotRoles();
				if(botRoles.isEmpty()) {
					DefaultMessage.COMMAND_BOTROLE_NO_BOTROLES.reply(event);
					return;
				}
				EmbedBuilder eb = new EmbedBuilder();
				eb.addField(DefaultLocaleString.COMMAND_BOTROLE_LIST_TITLE.getFor(event.getSender()),
							botRoles.stream().map(m -> m.getName()).collect(Collectors.joining(", ")),
							true);
				event.reply(eb.build());
			}

			@Override
			public List<OptionData> getOptions() {
				return Collections.emptyList();
			}
		})
		.setDescription(DefaultLocaleString.COMMAND_BOTROLE_LIST_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_BOTROLE_LIST_USAGE)
		.setPermission(DefaultPermissions.ROLE_BOTROLE_LIST);
	}

}

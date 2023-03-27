package me.eglp.gv2.commands.admin;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import me.eglp.gv2.util.base.guild.GraphiteGuild;
import me.eglp.gv2.util.base.guild.GraphiteRole;
import me.eglp.gv2.util.base.user.GraphiteUser;
import me.eglp.gv2.util.command.Command;
import me.eglp.gv2.util.command.CommandCategory;
import me.eglp.gv2.util.command.CommandInvokedEvent;
import me.eglp.gv2.util.command.ParentCommand;
import me.eglp.gv2.util.lang.DefaultLocaleString;
import me.eglp.gv2.util.lang.DefaultMessage;
import me.eglp.gv2.util.permission.DefaultPermissions;
import me.eglp.gv2.util.permission.GuildPermissionManager;
import me.eglp.gv2.util.permission.Permissible;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class CommandPermission extends ParentCommand {

	public CommandPermission() {
		super(null, CommandCategory.ADMIN, "permission");
		setDescription(DefaultLocaleString.COMMAND_PERMISSION_DESCRIPTION);
		
		addSubCommand(new Command(this, "add") {
			
			@Override
			public void action(CommandInvokedEvent event) {
				Object o = event.getOption("who");
				String perm = (String) event.getOption("permission");
				
				Permissible perms = getPermissible(event.getGuild(), o);
				if(perms == null) {
					DefaultMessage.COMMAND_PERMISSION_ALLOWED_MENTION_TYPES.reply(event);
					return;
				}
				
				if(perms.hasPermissionExactly(perm)) {
					DefaultMessage.COMMAND_PERMISSION_ADD_ALREADY_HAS_PERMISSION.reply(event, "entity", getAsMention(o));
					return;
				}
				
				perms.addPermission(perm);
				DefaultMessage.COMMAND_PERMISSION_ADD_PERMISSION_ADDED.reply(event, "permission", perm, "entity", getAsMention(o));
			}
			
			@Override
			public List<OptionData> getOptions() {
				return Arrays.asList(
						new OptionData(OptionType.MENTIONABLE, "who", "Which user or role to add the permission to", true),
						new OptionData(OptionType.STRING, "permission", "The permission to check", true)
					);
			}
		})
		.setPermission(DefaultPermissions.ADMIN_PERMISSION_ADD)
		.setDescription(DefaultLocaleString.COMMAND_PERMISSION_ADD_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_PERMISSION_ADD_USAGE);
		
		addSubCommand(new Command(this, "remove") {
			
			@Override
			public void action(CommandInvokedEvent event) {
				Object o = event.getOption("who");
				String perm = (String) event.getOption("permission");
				
				Permissible perms = getPermissible(event.getGuild(), o);
				if(perms == null) {
					DefaultMessage.COMMAND_PERMISSION_ALLOWED_MENTION_TYPES.reply(event);
					return;
				}
				
				if(!perms.hasPermissionExactly(perm)) {
					DefaultMessage.COMMAND_PERMISSION_REMOVE_DOESNT_HAVE_PERMISSION.reply(event, "entity", getAsMention(o));
					return;
				}
				
				perms.removePermission(perm);
				DefaultMessage.COMMAND_PERMISSION_REMOVE_PERMISSION_REMOVED.reply(event, "permission", perm, "entity", getAsMention(o));
			}
			
			@Override
			public List<OptionData> getOptions() {
				return Arrays.asList(
						new OptionData(OptionType.MENTIONABLE, "who", "Which user or role to remove the permission from", true),
						new OptionData(OptionType.STRING, "permission", "The permission to check", true)
					);
			}
		})
		.setPermission(DefaultPermissions.ADMIN_PERMISSION_REMOVE)
		.setDescription(DefaultLocaleString.COMMAND_PERMISSION_REMOVE_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_PERMISSION_REMOVE_USAGE);
		
		addSubCommand(new Command(this, "list") {
			
			@Override
			public void action(CommandInvokedEvent event) {
				Object o = event.getOption("who");
				
				EmbedBuilder b = new EmbedBuilder();
				Map<String, List<String>> map = getFullPermissions(event.getGuild(), o);
				
				List<String> selfPerms = map.get(null);
				if(!selfPerms.isEmpty()) b.addField(DefaultLocaleString.COMMAND_PERMISSION_LIST_SELF_TITLE.getFor(event.getGuild(), "entity", getFriendlyName(o)), selfPerms.stream().collect(Collectors.joining("\n", "```\n", "\n```")), false);
				map.forEach((t, v) -> {
					if(t == null || v.isEmpty()) return;
					b.addField(DefaultLocaleString.COMMAND_PERMISSION_LIST_INHERITED_TITLE.getFor(event.getGuild(), "entity", t), map.get(t).stream().collect(Collectors.joining("\n", "```\n", "\n```")), false);
				});
				
				if(b.isEmpty()) {
					DefaultMessage.COMMAND_PERMISSION_LIST_NO_PERMISSIONS.reply(event, "entity", getAsMention(o));
					return;
				}
				
				event.reply(b.build());
			}
			
			@Override
			public List<OptionData> getOptions() {
				return Arrays.asList(
						new OptionData(OptionType.MENTIONABLE, "who", "Which user or role to list the permissions for", true)
					);
			}
		})
		.setPermission(DefaultPermissions.ADMIN_PERMISSION_LIST)
		.setDescription(DefaultLocaleString.COMMAND_PERMISSION_LIST_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_PERMISSION_LIST_USAGE);
		
		addSubCommand(new Command(this, "check") {
			
			@Override
			public void action(CommandInvokedEvent event) {
				Object o = event.getOption("who");
				String perm = (String) event.getOption("permission");
				
				Permissible perms = getPermissible(event.getGuild(), o);
				if(perms == null) {
					DefaultMessage.COMMAND_PERMISSION_ALLOWED_MENTION_TYPES.reply(event);
					return;
				}
				
				if(perms.hasPermission(perm)) {
					DefaultMessage.COMMAND_PERMISSION_CHECK_HAS_PERMISSION.reply(event, "entity", getAsMention(o), "permission", perm);
				}else {
					DefaultMessage.COMMAND_PERMISSION_CHECK_NO_PERMISSION.reply(event, "entity", getAsMention(o), "permission", perm);
				}
			}
			
			@Override
			public List<OptionData> getOptions() {
				return Arrays.asList(
						new OptionData(OptionType.MENTIONABLE, "who", "Which user or role to check the permission for", true),
						new OptionData(OptionType.STRING, "permission", "The permission to check", true)
					);
			}
		})
		.setPermission(DefaultPermissions.ADMIN_PERMISSION_CHECK)
		.setDescription(DefaultLocaleString.COMMAND_PERMISSION_CHECK_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_PERMISSION_CHECK_USAGE);
	}
	
	private Permissible getPermissible(GraphiteGuild guild, Object o) {
		GuildPermissionManager m = guild.getPermissionManager();
		if(o instanceof GraphiteUser) {
			return m.getPermissions((GraphiteUser) o);
		}else if(o instanceof GraphiteRole) {
			GraphiteRole role = (GraphiteRole) o;
			if(role.isPublicRole()) {
				return m.getEveryonePermissions();
			}else {
				return m.getPermissions(role);
			}
		}else {
			return null;
		}
	}
	
	private String getAsMention(Object o) {
		if(o instanceof GraphiteUser) {
			return ((GraphiteUser) o).getAsMention();
		}else if(o instanceof GraphiteRole) {
			return ((GraphiteRole) o).getAsMention();
		}else {
			return null;
		}
	}
	
	private String getFriendlyName(Object o) {
		if(o instanceof GraphiteUser) {
			return "@" + ((GraphiteUser) o).getName();
		}else if(o instanceof GraphiteRole) {
			GraphiteRole role = (GraphiteRole) o;
			if(role.isPublicRole()) {
				return "@everyone";
			}else {
				return "@" + role.getName();
			}
		}else {
			return null;
		}
	}
	
	private Map<String, List<String>> getFullPermissions(GraphiteGuild guild, Object o) {
		Map<String, List<String>> permissionsMap = new LinkedHashMap<>();
		
		if(o instanceof GraphiteUser) {
			GraphiteUser u = (GraphiteUser) o;
			permissionsMap.put(null, getPermissible(guild, u).getRawPermissions());
			guild.getMember(u).getRoles().forEach(r -> permissionsMap.put("@" + r.getName(), getPermissible(guild, r).getRawPermissions()));
			permissionsMap.put("@everyone", getPermissible(guild, guild.getPublicRole()).getRawPermissions());
		}else if(o instanceof GraphiteRole) {
			GraphiteRole role = (GraphiteRole) o;
			if(role.isPublicRole()) {
				permissionsMap.put(null, getPermissible(guild, guild.getPublicRole()).getRawPermissions());
			}else {
				permissionsMap.put(null, getPermissible(guild, role).getRawPermissions());
				permissionsMap.put("@everyone", getPermissible(guild, guild.getPublicRole()).getRawPermissions());
			}
		}else {
			return null;
		}
		return permissionsMap;
	}
	
}

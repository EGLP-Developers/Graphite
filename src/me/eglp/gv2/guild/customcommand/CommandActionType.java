package me.eglp.gv2.guild.customcommand;

import java.awt.Color;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.WrappedException;

import me.eglp.gv2.guild.GraphiteGuildMessageChannel;
import me.eglp.gv2.guild.GraphiteMember;
import me.eglp.gv2.guild.GraphiteRole;
import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.user.GraphiteUser;
import me.eglp.gv2.util.backup.GuildBackup;
import me.eglp.gv2.util.backup.RestoreSelector;
import me.eglp.gv2.util.command.Command;
import me.eglp.gv2.util.command.CommandInvokedEvent;
import me.eglp.gv2.util.command.slash.SlashCommandHelper;
import me.eglp.gv2.util.command.text.CommandHandler;
import me.eglp.gv2.util.command.text.CommandParser;
import me.eglp.gv2.util.command.text.CommandParser.ParsedCommand;
import me.eglp.gv2.util.command.text.argument.CommandArgument;
import me.eglp.gv2.util.queue.GraphiteQueue;
import me.eglp.gv2.util.queue.GraphiteTaskInfo;
import me.eglp.gv2.util.scripting.GraphiteScript;
import me.eglp.gv2.util.scripting.ScriptExecutionException;
import me.eglp.gv2.util.scripting.ScriptTimeoutError;
import me.eglp.gv2.util.scripting.object.JSCommandInvokedEvent;
import me.eglp.gv2.util.webinterface.js.JavaScriptEnum;
import me.eglp.gv2.util.webinterface.js.JavaScriptValue;
import me.eglp.gv2.util.webinterface.js.WebinterfaceObject;
import me.mrletsplay.mrcore.json.converter.JSONPrimitiveStringConvertible;
import net.dv8tion.jda.api.EmbedBuilder;

@JavaScriptEnum
public enum CommandActionType implements WebinterfaceObject, JSONPrimitiveStringConvertible {

	DISCORD_CHANNEL_MESSAGE(
			"Send Message On Discord",
			event -> {
				GraphiteGuildMessageChannel ch = (GraphiteGuildMessageChannel) event.getParameter("channel");
				ch.sendMessage((String) event.getParameter("message"));
			},
			new CommandActionProperty("channel", "Channel", CommandParameterType.TEXT_CHANNEL),
			new CommandActionProperty("message", "Message", CommandParameterType.STRING)
		),
	DISCORD_PRIVATE_MESSAGE(
			"Send Message To User",
			event -> {
				GraphiteUser u = (GraphiteUser) event.getParameter("user");
				if(u.isBot()) return;
				u.openPrivateChannel().sendMessage((String) event.getParameter("message"));
			},
			new CommandActionProperty("user", "User", CommandParameterType.USER),
			new CommandActionProperty("message", "Message", CommandParameterType.STRING)
		),
	DISCORD_COLORED_CHANNEL_MESSAGE(
			"Send Colored Message On Discord",
			event -> {
				GraphiteGuildMessageChannel ch = (GraphiteGuildMessageChannel) event.getParameter("channel");
				ch.sendMessage(new EmbedBuilder()
						.setDescription((String) event.getParameter("message"))
						.setColor((Color) event.getParameter("color"))
						.build());
			},
			new CommandActionProperty("channel", "Channel", CommandParameterType.TEXT_CHANNEL),
			new CommandActionProperty("message", "Message", CommandParameterType.STRING),
			new CommandActionProperty("color", "Color", CommandParameterType.COLOR)
		),
	DISCORD_COLORED_PRIVATE_MESSAGE(
			"Send Colored Message To User",
			event -> {
				GraphiteUser u = (GraphiteUser) event.getParameter("user");
				if(u.isBot()) return;
				u.openPrivateChannel().sendMessage(new EmbedBuilder()
						.setDescription((String) event.getParameter("message"))
						.setColor((Color) event.getParameter("color"))
						.build());
			},
			new CommandActionProperty("user", "User", CommandParameterType.USER),
			new CommandActionProperty("message", "Message", CommandParameterType.STRING),
			new CommandActionProperty("color", "Color", CommandParameterType.COLOR)
		),
	EXECUTE_COMMAND(
			"Execute command",
			event -> {
				String redirectedCommand = (String) event.getParameter("command");

				Command c = CommandHandler.getCommandExact(redirectedCommand);
				if(c == null) throw new CustomCommandExecutionException(event, "Unknown command");

				CommandInvokedEvent origEv = event.getCommandEvent();
				Map<String, Object> options = new HashMap<>();
				if(!SlashCommandHelper.gatherOptions(origEv.getSender(), new CommandArgument[0], origEv.getChannel(), Collections.emptyList(), () -> c.sendCommandHelp(origEv.getChannel()), options, c.getOptions())) return;
				CommandInvokedEvent ev = CommandInvokedEvent.copyOfEvent(
					event.getCommandEvent(),
					c,
						options);
				c.action(ev);
			},
			new CommandActionProperty("command", "Command", CommandParameterType.STRING)
		),
	EXECUTE_COMMAND_WITH_ARGUMENTS(
			"Execute command with arguments",
			event -> {
				String redirectedCommand = (String) event.getParameter("command");

				Command c = CommandHandler.getCommandExact(redirectedCommand);
				if(c == null) throw new CustomCommandExecutionException(event, "Unknown command");

				CommandInvokedEvent origEv = event.getCommandEvent();
				ParsedCommand parsed = CommandParser.parse(origEv.getGuild(), origEv.getPrefixUsed(), (String) event.getParameter("arguments"));
				Map<String, Object> options = new HashMap<>();
				if(!SlashCommandHelper.gatherOptions(origEv.getSender(), parsed.getArgs(), origEv.getChannel(), Collections.emptyList(), () -> c.sendCommandHelp(origEv.getChannel()), options, c.getOptions())) return;
				CommandInvokedEvent ev = CommandInvokedEvent.copyOfEvent(
					event.getCommandEvent(),
					c,
					options);
				c.action(ev);
			},
			new CommandActionProperty("command", "Command", CommandParameterType.STRING),
			new CommandActionProperty("arguments", "Arguments", CommandParameterType.STRING)
		),
	CREATE_BACKUP(
			"Create Backup",
			event -> {
				if(!event.getGuild().canCreateBackup()) {
					throw new CustomCommandExecutionException(event, "Backup limit reached");
				}

				event.getGuild().createBackup(null, 0, false);
			}
		),
	RESTORE_BACKUP(
			"Restore Backup",
			event -> {
				EnumSet<RestoreSelector> params = EnumSet.copyOf(Arrays.stream(RestoreSelector.values())
						.filter(r -> (boolean) event.getParameter(r.name()))
						.collect(Collectors.toList()));
				String bid = (String) event.getParameter("backup-id");
				GuildBackup bu = event.getGuild().getBackupByName(bid);
				if(bu == null) throw new CustomCommandExecutionException(event, "Invalid backup id");
				GraphiteQueue q = Graphite.getQueue();
				if(q.isHeavyBusy()) throw new CustomCommandExecutionException(event, "Heavy queue is busy");
				q.queueHeavy(event.getGuild(), new GraphiteTaskInfo(GuildBackup.TASK_ID, "Restoring backup (CustomCommand)"), () -> {
					try {
						bu.restore(event.getGuild(), null, params); // Backups created via command actions don't have messages
					}catch(Exception e) {
						GraphiteCustomCommand.sendErrorMessage(event.getCommandEvent().getChannel(), e);
					}
				});
			},
			combine(
				new CommandActionProperty[] {new CommandActionProperty("backup-id", "Backup Name", CommandParameterType.STRING)},
				Arrays.stream(RestoreSelector.values())
					.map(p -> new CommandActionProperty(p.name(), "Restore " + p.getFriendlyName() + "?", CommandParameterType.BOOLEAN))
					.toArray(CommandActionProperty[]::new)
			)
		),
	GRANT_PERMISSION(
			"Grant permission to member",
			event -> {
				String perm = (String) event.getParameter("permission");
				GraphiteMember m = event.getGuild().getMember((GraphiteUser) event.getParameter("member"));
				if(m.isBot()) return;
				m.getMemberPermissions().addPermission(perm);
			},
			new CommandActionProperty("member", "Member", CommandParameterType.USER),
			new CommandActionProperty("permission", "Permission", CommandParameterType.STRING)
		),
	REVOKE_PERMISSION(
			"Revoke permission from member",
			event -> {
				String perm = (String) event.getParameter("permission");
				GraphiteMember m = event.getGuild().getMember((GraphiteUser) event.getParameter("member"));
				if(m.isBot()) return;
				m.getMemberPermissions().removePermission(perm);
			},
			new CommandActionProperty("member", "Member", CommandParameterType.USER),
			new CommandActionProperty("permission", "Permission", CommandParameterType.STRING)
		),
	GRANT_ROLE_PERMISSION(
			"Grant permission to role",
			event -> {
				String perm = (String) event.getParameter("permission");
				GraphiteRole r = (GraphiteRole) event.getParameter("role");
				r.getRolePermissions().addPermission(perm);
			},
			new CommandActionProperty("role", "Role", CommandParameterType.ROLE),
			new CommandActionProperty("permission", "Permission", CommandParameterType.STRING)
		),
	REVOKE_ROLE_PERMISSION(
			"Revoke permission from role",
			event -> {
				String perm = (String) event.getParameter("permission");
				GraphiteRole r = (GraphiteRole) event.getParameter("role");
				r.getRolePermissions().removePermission(perm);
			},
			new CommandActionProperty("role", "Role", CommandParameterType.ROLE),
			new CommandActionProperty("permission", "Permission", CommandParameterType.STRING)
		),
	GRANT_EVERYONE_PERMISSION(
			"Grant permission to @everyone",
			event -> {
				String perm = (String) event.getParameter("permission");
				event.getGuild().getPermissionManager().getEveryonePermissions().addPermission(perm);
			},
			new CommandActionProperty("permission", "Permission", CommandParameterType.STRING)
		),
	REVOKE_EVERYONE_PERMISSION(
			"Revoke permission from @everyone",
			event -> {
				String perm = (String) event.getParameter("permission");
				event.getGuild().getPermissionManager().getEveryonePermissions().removePermission(perm);
			},
			new CommandActionProperty("permission", "Permission", CommandParameterType.STRING)
		),
	CALL_COMMAND_SCRIPT(
			"Call command script",
			event -> {
				String sc = (String) event.getParameter("script");
				String data = (String) event.getParameter("data");
				GraphiteScript s = event.getGuild().getScripts().getScript(sc);
				if(s == null) throw new CustomCommandExecutionException(event, "Script \"" + sc + "\" doesn't exist or failed to load");
				GraphiteQueue q = Graphite.getQueue();
				if(q.isHeavyBusy()) throw new CustomCommandExecutionException(event, "Heavy queue is busy");
				q.queueHeavy(event.getGuild(), new GraphiteTaskInfo("run-script", "Executing script (CustomCommand)"), () -> {
					try {
						s.call("onCommand", new JSCommandInvokedEvent(event, data));
					}catch(WrappedException e) {
						GraphiteCustomCommand.sendErrorMessage(event.getCommandEvent().getChannel(), new CustomCommandExecutionException(event, "Error in script:\n" + e.getWrappedException().getMessage() + " (" + e.sourceName() + "#" + e.lineNumber() + ")"));
					}catch(RhinoException | ScriptExecutionException | ScriptTimeoutError e) {
						GraphiteCustomCommand.sendErrorMessage(event.getCommandEvent().getChannel(), new CustomCommandExecutionException(event, "Error in script:\n" + e.getMessage()));
					}
				});
			},
			new CommandActionProperty("script", "Script", CommandParameterType.STRING),
			new CommandActionProperty("data", "Script Data", CommandParameterType.STRING)
		),
	ADD_ROLE(
			"Add role to member",
			event -> {
				GraphiteMember m = event.getGuild().getMember((GraphiteUser) event.getParameter("member"));
				if(m.isBot()) return;
				GraphiteRole r = (GraphiteRole) event.getParameter("role");
				event.getGuild().getJDAGuild().addRoleToMember(m.getMember(), r.getJDARole()).queue();
			},
			new CommandActionProperty("member", "Member", CommandParameterType.USER),
			new CommandActionProperty("role", "Role", CommandParameterType.ROLE)
		),
	REMOVE_ROLE(
			"Remove role from member",
			event -> {
				GraphiteMember m = event.getGuild().getMember((GraphiteUser) event.getParameter("member"));
				if(m.isBot()) return;
				GraphiteRole r = (GraphiteRole) event.getParameter("role");
				event.getGuild().getJDAGuild().removeRoleFromMember(m.getMember(), r.getJDARole()).queue();
			},
			new CommandActionProperty("member", "Member", CommandParameterType.USER),
			new CommandActionProperty("role", "Role", CommandParameterType.ROLE)
		)
	;

	@JavaScriptValue(getter = "getDescription")
	private String description;

	private Consumer<CustomCommandInvokedEvent> run;

	@JavaScriptValue(getter = "getProperties")
	private List<CommandActionProperty> properties;

	private CommandActionType(String description, Consumer<CustomCommandInvokedEvent> run, CommandActionProperty... properties) {
		this.description = description;
		this.run = run;
		this.properties = Arrays.asList(properties);
	}

	public String getDescription() {
		return description;
	}

	public void run(CustomCommandInvokedEvent event) throws CustomCommandExecutionException {
		run.accept(event);
	}

	public List<CommandActionProperty> getProperties() {
		return properties;
	}

	public CommandActionProperty getProperty(String name) {
		return properties.stream().filter(p -> p.getName().equals(name)).findFirst().orElse(null);
	}

	@Override
	public String toJSONPrimitive() {
		return name();
	}

	public static CommandActionType decodePrimitive(Object  value) {
		return valueOf((String) value);
	}

	@SuppressWarnings("unchecked")
	private static <T> T[] combine(T[] a, T... b) {
		T[] a2 = (T[]) Array.newInstance(a.getClass().getComponentType(), a.length + b.length);
		System.arraycopy(a, 0, a2, 0, a.length);
		System.arraycopy(b, 0, a2, a.length, b.length);
		return a2;
	}

}

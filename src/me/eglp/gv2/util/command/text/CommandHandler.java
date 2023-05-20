package me.eglp.gv2.util.command.text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.guild.GraphiteGuildMessageChannel;
import me.eglp.gv2.guild.alias.CommandAlias;
import me.eglp.gv2.guild.customcommand.GraphiteCustomCommand;
import me.eglp.gv2.main.DebugCategory;
import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.main.GraphiteDebug;
import me.eglp.gv2.user.GraphitePrivateChannel;
import me.eglp.gv2.user.GraphiteUser;
import me.eglp.gv2.util.base.GraphiteMessageChannel;
import me.eglp.gv2.util.command.Command;
import me.eglp.gv2.util.command.CommandCategory;
import me.eglp.gv2.util.command.CommandInvokedEvent;
import me.eglp.gv2.util.command.SpecialExecute;
import me.eglp.gv2.util.command.SpecialHelp;
import me.eglp.gv2.util.command.slash.SlashCommandHelper;
import me.eglp.gv2.util.command.text.CommandParser.ParsedCommand;
import me.eglp.gv2.util.command.text.argument.CommandArgument;
import me.eglp.gv2.util.event.custom.impl.GraphiteMessageReceivedEvent;
import me.eglp.gv2.util.exception.MissingPermissionException;
import me.eglp.gv2.util.lang.DefaultMessage;
import me.eglp.gv2.util.queue.QueueTask;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandHandler {

	private static List<Command> commands = new ArrayList<>();

	public static void handleCommand(MessageReceivedEvent event, String prefix, String commandLine) {
		if(commandLine.isEmpty()) return;
		GraphiteGuild guild = event.isFromGuild() ? Graphite.getGuild(event.getGuild()) : null;
		ParsedCommand parsed = CommandParser.parse(guild, prefix, commandLine);
		GraphiteMessageChannel<?> channel = Graphite.getMessageChannel(event.getChannel());
		GraphiteUser author = Graphite.getUser(event.getAuthor());

		if(event.isFromGuild() && !((GraphiteGuildMessageChannel) channel).canWrite()) {
			if(event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
				GraphitePrivateChannel ch = author.openPrivateChannel();
				if(ch == null) return; // WTF, wer verbietet einem Bot denn bitte zu sprechen? Leute
				DefaultMessage.ERROR_CANT_WRITE.sendMessage(ch);
			}
			return;
		}

		boolean isInGame = event.isFromType(ChannelType.PRIVATE) && Graphite.getMinigames().isInGame(author);

		Command tmpCmd = getCommand(parsed.getArgs()[0].getRaw());

		if(tmpCmd == null) {
			CommandAlias alias = guild != null ? guild.getCustomCommandsConfig().getCommandAlias(parsed.getArgs()[0].getRaw()) : null;
			if(alias != null) tmpCmd = CommandHandler.getCommandExact(alias.getForCommand());
		}

		Command c = tmpCmd;

		if(c == null) {
			GraphiteCustomCommand cc = guild.getCustomCommandsConfig().getCustomCommandByName(parsed.getArgs()[0].getRaw());

			if(cc == null) {
				Graphite.getCustomListener().fire(new GraphiteMessageReceivedEvent(event));
				return;
			}

			if(cc.getPermission() != null && !guild.getPermissionManager().hasPermission(guild.getMember(event.getMember()), cc.getPermission())) {
				DefaultMessage.ERROR_NO_PERMISSION.sendMessage(channel, "permission", cc.getPermission());
				Graphite.getCustomListener().fire(new GraphiteMessageReceivedEvent(event, cc, false));
				return;
			}

			if(guild.isQueueBusy()) {
				event.getChannel().sendMessage(DefaultMessage.ERROR_SERVER_BUSY.getFor(guild)).queue();
				Graphite.getCustomListener().fire(new GraphiteMessageReceivedEvent(event, cc, false));
				return;
			}

			CommandArgument[] cArgs = Arrays.copyOfRange(parsed.getArgs(), 1, parsed.getArgs().length);
			try {
				Map<String, Object> options = new HashMap<>();
				if(!SlashCommandHelper.gatherOptions(author, cArgs, channel, event.getMessage().getAttachments(), () -> cc.sendCommandHelp(channel), options, cc.getOptions())) return;
				CommandInvokedEvent ev = CommandInvokedEvent.ofCustomMessageEvent(cc, event, author, guild == null ? author : guild, channel, parsed.getPrefix(), options);
				Graphite.getCustomListener().fire(new GraphiteMessageReceivedEvent(event, cc, true));
				QueueTask<?> t = guild.getResponsibleQueue().queue(guild, () -> cc.invoke(ev));
				t.exceptionally(exc -> {
					if(t.isCancelled()) return null;
					DefaultMessage.ERROR_EXCEPTION.sendMessage(guild.getGuildMessageChannel(event.getGuildChannel()), "error_message", exc.getMessage());
					return null;
				});
			}catch(Exception e) {
				GraphiteDebug.log(DebugCategory.COMMAND_HANDLER, e);
				channel.sendMessage("Oops, error: " + e.getClass().getName());
			}

			Graphite.getCustomListener().fire(new GraphiteMessageReceivedEvent(event));
			return;
		}

		if(guild != null && c.getModule() != null && !guild.getConfig().hasModuleEnabled(c.getModule())) {
			Graphite.getCustomListener().fire(new GraphiteMessageReceivedEvent(event, c, false));
			return;
		}

		CommandArgument[] args = parsed.getArgs();
		int i = 1;
		while(i < args.length) {
			String s = args[i].getRaw();
			Command sC = c.getSubCommands().stream().filter(sub -> sub.getName().equalsIgnoreCase(s) || sub.getAliases().stream().anyMatch(a -> a.equalsIgnoreCase(s))).findFirst().orElse(null);
			if(sC == null) break;
			i++;
			c = sC;
		}

		if(guild != null && !guild.hasPermissions(c.getRequiredPermissions())) {
			DefaultMessage.ERROR_LACKING_PERMISSION.sendMessage(channel, "permission", c.getRequiredPermissions().stream().map(p -> p.getName()).collect(Collectors.joining(", ")));
			return;
		}

		if(guild != null && c.getPermission() != null && !guild.getPermissionManager().hasPermission(guild.getMember(event.getMember()), c.getPermission())) {
			DefaultMessage.ERROR_NO_PERMISSION.sendMessage(channel, "permission", c.getPermission());
			Graphite.getCustomListener().fire(new GraphiteMessageReceivedEvent(event, c, false));
			return;
		}

		SpecialExecute ex = c.getAnnotation(SpecialExecute.class);
		if(event.isFromGuild() && !c.allowsServer()) {
			DefaultMessage.ERROR_COMMAND_PRIVATE_ONLY.sendMessage(channel);
			Graphite.getCustomListener().fire(new GraphiteMessageReceivedEvent(event, c, false));
			return;
		}

		if(!event.isFromGuild() && !c.allowsPrivate()) {
			DefaultMessage.ERROR_COMMAND_SERVER_ONLY.sendMessage(channel);
			Graphite.getCustomListener().fire(new GraphiteMessageReceivedEvent(event, c, false));
			return;
		}

		if(isInGame && !c.allowsInGame()) {
			Graphite.getCustomListener().fire(new GraphiteMessageReceivedEvent(event, c, false));
			return;
		}

		if(!c.getSubCommands().isEmpty()) {
			c.sendCommandHelp(channel);
			Graphite.getCustomListener().fire(new GraphiteMessageReceivedEvent(event, c, false));
			return;
		}

		CommandArgument[] cArgs = Arrays.copyOfRange(args, i, args.length);

		Map<String, Object> options = new HashMap<>();
		final Command fC = c;
		if(!SlashCommandHelper.gatherOptions(author, cArgs, channel, event.getMessage().getAttachments(), () -> fC.sendCommandHelp(channel), options, c.getOptions())) return;
		CommandInvokedEvent e = CommandInvokedEvent.ofMessageEvent(c, event, author, guild == null ? author : guild, channel, parsed.getPrefix(), options);

		if(guild != null) {
			if((ex != null && ex.bypassQueue()) || c.isBypassQueue()) {
				final Command c2 = c;
				Graphite.getCustomListener().fire(new GraphiteMessageReceivedEvent(event, c2, true));
				Graphite.getScheduler().execute(() -> c2.action(e));
				return;
			}

			if(guild.isQueueBusy()) {
				event.getChannel().sendMessage(DefaultMessage.ERROR_SERVER_BUSY.getFor(guild)).queue();
				Graphite.getCustomListener().fire(new GraphiteMessageReceivedEvent(event, c, false));
				return;
			}

			final Command c2 = c;
			Graphite.getCustomListener().fire(new GraphiteMessageReceivedEvent(event, c, true));
			QueueTask<?> t = guild.getResponsibleQueue().queue(guild, () -> c2.action(e));
			t.exceptionally(exc -> {
				if(t.isCancelled()) return null;
				if(exc instanceof MissingPermissionException) {
					MissingPermissionException e2 = (MissingPermissionException) exc;
					DefaultMessage.ERROR_LACKING_PERMISSION.sendMessage(guild.getGuildMessageChannel(event.getGuildChannel()), "permission", Arrays.stream(e2.getPermissions()).map(Permission::getName).collect(Collectors.joining(", ")));
					return null;
				}

				GraphiteDebug.log(DebugCategory.COMMAND_HANDLER, exc);
				DefaultMessage.ERROR_EXCEPTION.sendMessage(guild.getGuildMessageChannel(event.getGuildChannel()), "error_message", exc.getMessage());
				return null;
			});
		}else {
			try {
				Graphite.getCustomListener().fire(new GraphiteMessageReceivedEvent(event, c, true));
				c.action(e);
			}catch(Exception exc) {
				GraphiteDebug.log(DebugCategory.COMMAND_HANDLER, exc);
				GraphitePrivateChannel ch = Graphite.getUser(event.getAuthor()).openPrivateChannel();
				if(ch == null) return;
				DefaultMessage.ERROR_EXCEPTION.sendMessage(ch, "error_message", String.valueOf(exc.getMessage()));
			}
		}
	}

	public static void registerCommand(Command command) {
		if(command.isBeta() && !Graphite.getBotInfo().isBeta()) return;
		List<Command> c = getCommands();
		c.add(command);
	}

	public static List<Command> getCommands() {
		return commands;
	}

	public static Command getCommandByPath(String... path) {
		Command c = getCommands().stream().filter(sc -> sc.getName().equalsIgnoreCase(path[0]) || sc.getAliases().stream().anyMatch(a -> a.equalsIgnoreCase(path[0]))).findFirst().orElse(null);
		if(c == null) return null;
		if(path.length == 1) return c;
		return getSubCommand(c, new ArrayList<>(Arrays.stream(path).skip(1).collect(Collectors.toList())));
	}

	private static Command getSubCommand(Command parent, List<String> path) {
		String s = path.remove(0);
		Command c = parent.getSubCommands().stream()
				.filter(sc -> sc.getName().equalsIgnoreCase(s) || sc.getAliases().stream().anyMatch(a -> a.equalsIgnoreCase(s)))
				.findFirst().orElse(null);
		if(c == null) return null;
		if(path.isEmpty()) return c;
		return getSubCommand(c, path);
	}

	public static Command getCommand(String name) {
		return getCommands().stream().filter(c -> c.getName().equalsIgnoreCase(name) || c.getAliases().stream().anyMatch(a -> a.equalsIgnoreCase(name))).findFirst().orElse(null);
	}

	public static Command getCommandExact(String name) {
		if(name.isBlank()) return null;
		List<String> parts = new ArrayList<>(Arrays.asList(name.toLowerCase().split(" ")));

		Command c = null;
		while(!parts.isEmpty()) {
			String cmd = parts.remove(0);
			c = c == null ? getCommand(cmd) : c.getSubCommands().stream()
					.filter(cm -> cm.getName().equals(cmd) || cm.getAliases().contains(cmd))
					.findFirst().orElse(null);

			if(c == null) return null;
		}

		return c;
	}

	public static void addCommands(Command parent, Map<CommandCategory, List<Command>> commands) {
		SpecialHelp sH = parent.getAnnotation(SpecialHelp.class);
		if((sH == null || !sH.hideSelf()) && parent.getSubCommands().isEmpty()) {
			List<Command> cs = commands.getOrDefault(parent.getCategory(), new ArrayList<>());
			cs.add(parent);
			commands.put(parent.getCategory(), cs);
		}
		for(Command c : parent.getSubCommands()) addCommands(c, commands);
	}

}

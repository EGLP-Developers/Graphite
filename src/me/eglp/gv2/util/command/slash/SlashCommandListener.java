package me.eglp.gv2.util.command.slash;

import java.util.Arrays;
import java.util.HashMap;
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
import me.eglp.gv2.util.command.CommandInvokedEvent;
import me.eglp.gv2.util.command.SpecialExecute;
import me.eglp.gv2.util.command.text.CommandHandler;
import me.eglp.gv2.util.event.AnnotationEventHandler;
import me.eglp.gv2.util.event.EventHandler;
import me.eglp.gv2.util.exception.MissingPermissionException;
import me.eglp.gv2.util.lang.DefaultMessage;
import me.eglp.gv2.util.queue.QueueTask;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class SlashCommandListener implements AnnotationEventHandler {

	private static Map<String, Command> slashCommands = new HashMap<>();

	public static void registerSlashCommand(String commandPath, Command command) {
		slashCommands.put(commandPath, command);
	}

	public static Command getSlashCommand(String commandPath) {
		return slashCommands.get(commandPath);
	}

	@EventHandler
	public void onSlashCommand(SlashCommandInteractionEvent event) {
		Command tmpCmd = slashCommands.get(event.getFullCommandName());
		GraphiteGuild guild = event.isFromGuild() ? Graphite.getGuild(event.getGuild()) : null;
		GraphiteMessageChannel<?> channel = Graphite.getMessageChannel(event.getChannel());
		GraphiteUser author = Graphite.getUser(event.getUser());

		if(tmpCmd == null) {
			CommandAlias alias = guild != null ? guild.getCustomCommandsConfig().getCommandAlias(event.getFullCommandName()) : null;
			if(alias != null) tmpCmd = CommandHandler.getCommandExact(alias.getForCommand());
		}

		Command cmd = tmpCmd;

		if(cmd == null) {
			if(guild != null) {
				GraphiteCustomCommand cc = guild.getCustomCommandsConfig().getCustomCommandByName(event.getFullCommandName());
				if(cc != null) {
					if(guild != null && cc.getPermission() != null && !guild.getPermissionManager().hasPermission(guild.getMember(event.getMember()), cc.getPermission())) {
						event.reply(DefaultMessage.ERROR_NO_PERMISSION.createMessage(guild, "permission", cc.getPermission())).queue();
						return;
					}

					if(guild.isQueueBusy()) {
						event.reply(DefaultMessage.ERROR_SERVER_BUSY.createMessage(guild)).queue();
						return;
					}

					QueueTask<?> t = guild.getResponsibleQueue().queue(guild, () -> {
						cc.invoke(CommandInvokedEvent.ofCustomSlashEvent(cc, event, author, guild == null ? author : guild, Graphite.getMessageChannel(event.getChannel())));
					});

					t.exceptionally(exc -> {
						if(t.isCancelled()) return null;
						if(exc instanceof MissingPermissionException) {
							MissingPermissionException e2 = (MissingPermissionException) exc;
							event.reply(DefaultMessage.ERROR_LACKING_PERMISSION.createMessage(guild, "permission", Arrays.stream(e2.getPermissions()).map(Permission::getName).collect(Collectors.joining(", ")))).queue();
							return null;
						}

						GraphiteDebug.log(DebugCategory.COMMAND_HANDLER, exc);
						event.reply(DefaultMessage.ERROR_EXCEPTION.createMessage(guild, "error_message", exc.getMessage())).queue();
						return null;
					});

					return;
				}
			}

			event.reply("Invalid command!").setEphemeral(true).queue();
			return;
		}

		if(event.isFromGuild() && !((GraphiteGuildMessageChannel) channel).canWrite()) {
			if(event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
				GraphitePrivateChannel ch = author.openPrivateChannel();
				if(ch == null) return; // WTF, wer verbietet einem Bot denn bitte zu sprechen? Leute
				DefaultMessage.ERROR_CANT_WRITE.sendMessage(ch);
			}
			return;
		}

		if(guild != null && cmd.getModule() != null && !guild.getConfig().hasModuleEnabled(cmd.getModule())) {
			event.reply("Module disabled").setEphemeral(true).queue();
			return;
		}

		if(guild != null && !guild.hasPermissions(cmd.getRequiredPermissions())) {
			event.reply(DefaultMessage.ERROR_LACKING_PERMISSION.createMessage(guild, "permission", cmd.getRequiredPermissions().stream().map(p -> p.getName()).collect(Collectors.joining(", ")))).queue();
			return;
		}

		if(guild != null && cmd.getPermission() != null && !guild.getPermissionManager().hasPermission(guild.getMember(event.getMember()), cmd.getPermission())) {
			event.reply(DefaultMessage.ERROR_NO_PERMISSION.createMessage(guild, "permission", cmd.getPermission())).queue();
			return;
		}

		if(event.isFromGuild() && !cmd.allowsServer()) {
			event.reply(DefaultMessage.ERROR_COMMAND_PRIVATE_ONLY.createMessage(guild)).queue();
			return;
		}

		if(!event.isFromGuild() && !cmd.allowsPrivate()) {
			event.reply(DefaultMessage.ERROR_COMMAND_SERVER_ONLY.createMessage(guild)).queue();
			return;
		}

		CommandInvokedEvent e = CommandInvokedEvent.ofSlashEvent(cmd, event, author, guild == null ? author : guild, channel);
		if(guild != null) {
			SpecialExecute ex = cmd.getAnnotation(SpecialExecute.class);
			if((ex != null && ex.bypassQueue()) || cmd.isBypassQueue()) {
				final Command c2 = cmd;
				Graphite.getScheduler().execute(() -> c2.action(e));
				if(!event.isAcknowledged()) {
					DefaultMessage.ERROR_NO_SLASH_COMMAND_REPLY.reply(e, "discord_url", Graphite.getMainBotInfo().getLinks().getDiscord());
				}
				return;
			}

			if(guild.isQueueBusy()) {
				event.getChannel().sendMessage(DefaultMessage.ERROR_SERVER_BUSY.getFor(guild)).queue();
				return;
			}

			QueueTask<?> t = guild.getResponsibleQueue().queue(guild, () -> {
				cmd.action(e);
				if(!event.isAcknowledged()) {
					DefaultMessage.ERROR_NO_SLASH_COMMAND_REPLY.reply(e, "discord_url", Graphite.getMainBotInfo().getLinks().getDiscord());
				}
			});

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
				cmd.action(e);
			}catch(Exception exc) {
				GraphiteDebug.log(DebugCategory.COMMAND_HANDLER, exc);
				GraphitePrivateChannel ch = e.getAuthor().openPrivateChannel();
				if(ch == null) return;
				DefaultMessage.ERROR_EXCEPTION.sendMessage(ch, "error_message", String.valueOf(exc.getMessage()));
			}
		}
	}

}

package me.eglp.gv2.commands.info;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.guild.customcommand.GraphiteCustomCommand;
import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.command.Command;
import me.eglp.gv2.util.command.CommandCategory;
import me.eglp.gv2.util.command.CommandInvokedEvent;
import me.eglp.gv2.util.command.SpecialExecute;
import me.eglp.gv2.util.command.SpecialHelp;
import me.eglp.gv2.util.command.text.CommandHandler;
import me.eglp.gv2.util.lang.DefaultLocaleString;
import me.eglp.gv2.util.lang.DefaultMessage;
import me.eglp.gv2.util.selfcheck.SpecialSelfcheck;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class CommandHelp extends Command {

	public CommandHelp() {
		super(null, CommandCategory.INFO, "help");
		setDescription(DefaultLocaleString.COMMAND_HELP_DESCRIPTION);
		setUsage(DefaultLocaleString.COMMAND_HELP_USAGE);
	}

	@SpecialHelp(hideSelf = true)
	@SpecialExecute(allowPrivate = true)
	@SpecialSelfcheck(needsPermission = false)
	@Override
	public void action(CommandInvokedEvent event) {
		String command = (String) event.getOption("command");

		if(command == null) {
			Map<CommandCategory, List<Command>> commands = new HashMap<>();
			for(Command c : CommandHandler.getCommands()) addCommands(c, commands);
			removeUnwantedCommands(event.getGuild(), commands);
			EmbedBuilder b = new EmbedBuilder();
			b.setColor(Color.DARK_GRAY);
			b.setAuthor(Graphite.getBotInfo().getName(), null, Graphite.getIconUrl());
			b.setDescription(DefaultLocaleString.COMMAND_HELP_MESSAGE_GENERAL_INFO.getFor(event.getSender(),
					"prefix", event.getSender().getPrefix(),
					"invite", Graphite.getInviteUrl(),
					"discord", Graphite.getMainBotInfo().getLinks().getDiscord(),
					"webinterface", Graphite.getMainBotInfo().getWebsite().getWebinterfaceURL(),
					"website", Graphite.getMainBotInfo().getWebsite().getBaseURL(),
					"patreon", Graphite.getMainBotInfo().getLinks().getPatreon(),
					"bot", Graphite.getBotInfo().getName()));

			Map<String, List<String>> helpCategories = new HashMap<>();
			commands.forEach((cat, cmds) -> {
				helpCategories.put(
						cat.getName().getFor(event.getSender()),
						cmds.stream()
							.sorted(Comparator.comparing(c -> c.getFullName()))
							.map(c -> event.getPrefixUsed() + c.getFullName()).collect(Collectors.toList()));
			});

			if(event.isFromGuild()) {
				List<GraphiteCustomCommand> ccs = event.getGuild().getCustomCommandsConfig().getCustomCommands();
				if(!ccs.isEmpty()) {
					helpCategories.put(
						"CustomCommands",
						ccs.stream()
							.sorted(Comparator.comparing(c -> c.getName()))
							.map(m -> "`" + event.getPrefixUsed() + m.getName() + "`").collect(Collectors.toList()));
				}
			}

			helpCategories.entrySet().stream()
				.sorted(Comparator.<Map.Entry<String, List<String>>>comparingInt(en -> en.getValue().size()).reversed())
				.forEach(en -> b.addField(en.getKey(), en.getValue().stream().map(e -> "`" + e + "`").collect(Collectors.joining("\n")), true));

			while((b.getFields().size() % 3) != 0) b.addBlankField(true);

			if(event.isFromGuild()) {
				event.getAuthorChannel().sendMessage(b.build(), t -> event.getChannel().sendMessage(b.build()));
				if(event.getGuild() != null) DefaultMessage.COMMAND_HELP_SENT.reply(event);
			}else {
				event.reply(b.build());
			}
		}else {
			Command c = CommandHandler.getCommandByPath(command.split(" "));
			if(c == null) {
				if(event.isFromGuild()) {
					GraphiteCustomCommand cc = event.getGuild().getCustomCommandsConfig().getCustomCommandByName(command);
					if(cc != null) {
						event.reply(cc.getCommandHelpFor(event.getSender()));
						return;
					}
				}

				DefaultMessage.COMMAND_HELP_INVALID_COMMAND.reply(event);
				return;
			}

			event.reply(c.getCommandHelpFor(event.getSender(), true));
		}
	}

	private void removeUnwantedCommands(GraphiteGuild guild, Map<CommandCategory, List<Command>> commands) {
		commands.forEach((cat, cmds) -> {
			cmds.removeIf(c -> {
				if(guild != null
					&& c.getModule() != null
					&& !guild.getConfig().hasModuleEnabled(c.getModule())) return true;

				// If the command category only contains one top-level command, then children will be visible
				// Otherwise, only top-level commands are visible by default
				boolean categoryOnlyTopLevel = commands.get(cat).stream().filter(cmd -> cmd.getParent() == null).count() > 1;

				boolean hidden = !c.getSubCommands().isEmpty() && !categoryOnlyTopLevel;

				SpecialHelp sH = c.getAnnotation(SpecialHelp.class);
				if(sH != null) hidden = sH.hideSelf();

				if(c.getParent() != null) {
					boolean hiddenChild = categoryOnlyTopLevel;

					SpecialHelp parentSH = c.getParent().getAnnotation(SpecialHelp.class);
					if(parentSH != null && parentSH.hideSubCommands()) return true;

					if(hiddenChild) return true;
				}

				return hidden;
			});
		});

		commands.keySet().removeIf(cat -> commands.get(cat).isEmpty());
	}

	private void addCommands(Command parent, Map<CommandCategory, List<Command>> commands) {
		List<Command> cs = commands.getOrDefault(parent.getCategory(), new ArrayList<>());
		cs.add(parent);
		commands.put(parent.getCategory(), cs);

		for(Command c : parent.getSubCommands()) addCommands(c, commands);
	}

	@Override
	public List<OptionData> getOptions() {
		return Arrays.asList(
				new OptionData(OptionType.STRING, "command", "The command to check the help for")
			);
	}

}

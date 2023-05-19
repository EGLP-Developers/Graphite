package me.eglp.gv2.console.command;

import me.eglp.gv2.console.AbstractConsoleCommand;
import me.eglp.gv2.main.Graphite;
import me.mrletsplay.mrcore.command.event.CommandInvokedEvent;
import me.mrletsplay.mrcore.command.option.impl.DefaultCommandOption;

public class CommandStats extends AbstractConsoleCommand {

	public CommandStats() {
		super("stats");
		setDescription("Shows number of guilds and user count");
		setUsage("stats [bot]");
		addOption(DefaultCommandOption.HELP);
	}

	@Override
	public void action(CommandInvokedEvent event) {
		if(event.getParsedCommand().isOptionPresent(DefaultCommandOption.HELP) || event.getParsedCommand().getArguments().length > 1) {
			sendCommandInfo(event.getSender());
			return;
		}

		event.getSender().sendMessage("Guild count: " + Graphite.getGuildCount());
		event.getSender().sendMessage("User count: " + Graphite.getShards().stream().flatMap(s -> s.getJDA().getUsers().stream()).distinct().count());
	}

}

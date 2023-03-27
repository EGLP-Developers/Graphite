package me.eglp.gv2.console.command;

import java.util.stream.Collectors;

import me.eglp.gv2.console.AbstractConsoleCommand;
import me.eglp.gv2.console.ConsoleCommandProvider;
import me.mrletsplay.mrcore.command.Command;
import me.mrletsplay.mrcore.command.event.CommandInvokedEvent;
import me.mrletsplay.mrcore.command.option.impl.DefaultCommandOption;

public class CommandHelp extends AbstractConsoleCommand {

	public CommandHelp() {
		super("help");
		addAlias("?");
		setDescription("Lists all available commands");
		addOption(DefaultCommandOption.HELP);
	}
	
	@Override
	public void action(CommandInvokedEvent event) {
		if(event.getParsedCommand().isOptionPresent(DefaultCommandOption.HELP) || event.getParsedCommand().getArguments().length != 0) {
			sendCommandInfo(event.getSender());
			return;
		}
		event.getSender().sendMessage("Available commands " + ConsoleCommandProvider.INSTANCE.getCommands().stream().map(Command::getName).collect(Collectors.joining(", ")));
	}
	
}

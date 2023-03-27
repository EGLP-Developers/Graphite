package me.eglp.gv2.console.command;

import me.eglp.gv2.console.AbstractConsoleCommand;
import me.eglp.gv2.main.Graphite;
import me.mrletsplay.mrcore.command.event.CommandInvokedEvent;
import me.mrletsplay.mrcore.command.option.CommandOption;
import me.mrletsplay.mrcore.command.option.impl.DefaultCommandOption;

public class CommandShutdown extends AbstractConsoleCommand {

	private static final CommandOption<?> RESTART = createCommandOption("r", "restart");
	
	public CommandShutdown() {
		super("shutdown");
		setDescription("Stops Graphite");
		addOption(DefaultCommandOption.HELP);
		addOption(RESTART);
	}
	
	@Override
	public void action(CommandInvokedEvent event) {
		if(event.getParsedCommand().isOptionPresent(DefaultCommandOption.HELP) || event.getParsedCommand().getArguments().length != 0) {
			sendCommandInfo(event.getSender());
			return;
		}
		boolean restart = event.getParsedCommand().isOptionPresent(RESTART);
		if(restart) {
			Graphite.restart();
		}else {
			Graphite.shutdown(true);
		}
	}
	
}

package me.eglp.gv2.console.command;

import me.eglp.gv2.console.AbstractConsoleCommand;
import me.eglp.gv2.main.Graphite;
import me.mrletsplay.mrcore.command.event.CommandInvokedEvent;
import me.mrletsplay.mrcore.command.option.CommandOption;
import me.mrletsplay.mrcore.command.option.impl.DefaultCommandOption;

public class CommandWIKick extends AbstractConsoleCommand {
	
	private static final CommandOption<?> ALL = createCommandOption("a", "all");

	public CommandWIKick() {
		super("wikick");
		setDescription("Kick a user/all users from the webinterface");
		addOption(DefaultCommandOption.HELP);
		addOption(ALL);
	}
	
	@Override
	public void action(CommandInvokedEvent event) {
		if(event.getParsedCommand().isOptionPresent(DefaultCommandOption.HELP)) {
			sendCommandInfo(event.getSender());
			return;
		}
		
		if(event.getParsedCommand().isOptionPresent(ALL)) {
			Graphite.getWebinterface().kickAll();
			event.getSender().sendMessage("Kicked all users from the webinterface");
		}else {
			if(event.getArguments().length != 1) {
				sendCommandInfo(event.getSender());
				return;
			}
			
			String id = event.getArguments()[0];
			Graphite.getWebinterface().kick(id);
			event.getSender().sendMessage("Kicked " + id + " from the webinterface");
		}
	}
	
}

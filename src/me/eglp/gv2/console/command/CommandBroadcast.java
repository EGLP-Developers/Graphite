package me.eglp.gv2.console.command;

import java.util.Arrays;
import java.util.stream.Collectors;

import me.eglp.gv2.console.AbstractConsoleCommand;
import me.eglp.gv2.main.Graphite;
import me.mrletsplay.mrcore.command.event.CommandInvokedEvent;
import me.mrletsplay.mrcore.command.option.CommandOption;
import me.mrletsplay.mrcore.command.option.impl.DefaultCommandOption;

public class CommandBroadcast extends AbstractConsoleCommand{
	
	private static final CommandOption<?> ALL = createCommandOption("a", "all");

	public CommandBroadcast() {
		super("broadcast");
		setDescription("Boradcast to logged in wi users");
		setUsage("broadcast <--all | user> <message>");
		addOption(DefaultCommandOption.HELP);
		addOption(ALL);
	}

	@Override
	public void action(CommandInvokedEvent event) {
		if(event.getParsedCommand().isOptionPresent(DefaultCommandOption.HELP)) {
			sendCommandInfo(event.getSender());
			return;
		}
		
		if(event.isOptionPresent(ALL)) {
			String msg = Arrays.stream(event.getArguments()).collect(Collectors.joining(" "));
			Graphite.getWebinterface().broadcastToAll(msg);
			event.getSender().sendMessage("Broadcast message send to " + Graphite.getWebinterface().getWebSocketServer().getConnections().size() + " user(s). Message: " + msg);
		}else {
			if(event.getArguments().length < 1) {
				sendCommandInfo(event.getSender());
				return;
			}
			
			String id = event.getArguments()[0];
			String msg = Arrays.stream(event.getArguments()).skip(1).collect(Collectors.joining(" "));
			Graphite.getWebinterface().broadcastToUser(id, msg);
			event.getSender().sendMessage("Broadcast message send to " + Graphite.getUser(id).getName() + ". Message: " + msg);
		}
	}

}

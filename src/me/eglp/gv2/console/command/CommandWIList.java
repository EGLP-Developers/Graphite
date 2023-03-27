package me.eglp.gv2.console.command;

import org.java_websocket.WebSocket;

import me.eglp.gv2.console.AbstractConsoleCommand;
import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.webinterface.session.WebinterfaceSession;
import me.mrletsplay.mrcore.command.event.CommandInvokedEvent;
import me.mrletsplay.mrcore.command.option.impl.DefaultCommandOption;

public class CommandWIList extends AbstractConsoleCommand {
	
	public CommandWIList() {
		super("wilist");
		setDescription("List all users currently logged in on the webinterface");
		addOption(DefaultCommandOption.HELP);
	}
	
	@Override
	public void action(CommandInvokedEvent event) {
		if(event.getParsedCommand().isOptionPresent(DefaultCommandOption.HELP)) {
			sendCommandInfo(event.getSender());
			return;
		}
		
		for(WebSocket w : Graphite.getWebinterface().getWebSocketServer().getConnections()) {
			WebinterfaceSession s = w.getAttachment();
			event.getSender().sendMessage("User " + s.getUserID() + " (" + s.getUser().getDiscordUser().getName() + "#" + s.getUser().getDiscordUser().getDiscriminator() + ") with session id: " + s.getID());
		}
	}
	
}

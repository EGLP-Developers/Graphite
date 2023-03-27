package me.eglp.gv2.console.command;

import me.eglp.gv2.console.AbstractConsoleCommand;
import me.eglp.gv2.main.Graphite;
import me.mrletsplay.mrcore.command.event.CommandInvokedEvent;
import me.mrletsplay.mrcore.command.option.impl.DefaultCommandOption;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

public class CommandWhoIs extends AbstractConsoleCommand {
	
	public CommandWhoIs() {
		super("whois");
		setDescription("Shows the username/guild name by its id");
		addOption(DefaultCommandOption.HELP);
	}
	
	@Override
	public void action(CommandInvokedEvent event) {
		if(event.getParsedCommand().isOptionPresent(DefaultCommandOption.HELP)) {
			sendCommandInfo(event.getSender());
			return;
		}
		String[] args = event.getParsedCommand().getArguments();
		if(args.length != 2) {
			event.getSender().sendMessage("Usage: whois <User/Guild> <ID>");
			return;
		}
		if(args[0].equalsIgnoreCase("user")) {
			User u = Graphite.getJDAUser(args[1]);
			if(u!=null) {
				event.getSender().sendMessage("Tag: "+u.getName()+"#"+u.getDiscriminator());
			}else {
				event.getSender().sendMessage("User not found");
			}
		}else if(args[0].equalsIgnoreCase("guild")) {
			Guild g = Graphite.getJDAGuild(args[1]);
			if(g!=null) {
				event.getSender().sendMessage("Name: "+g.getName());
			}else {
				event.getSender().sendMessage("Guild not found");
			}
		}else {
			event.getSender().sendMessage("Usage: whois <User/Guild> <ID>");
		}
	}

}

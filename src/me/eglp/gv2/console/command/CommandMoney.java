package me.eglp.gv2.console.command;

import me.eglp.gv2.console.AbstractConsoleCommand;
import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.user.GraphiteUser;
import me.eglp.gv2.util.economy.GraphiteEconomy;
import me.mrletsplay.mrcore.command.event.CommandInvokedEvent;
import me.mrletsplay.mrcore.command.option.impl.DefaultCommandOption;

public class CommandMoney extends AbstractConsoleCommand{

	public CommandMoney() {
		super("money");
		setDescription("Manage da money");
		addOption(DefaultCommandOption.HELP);
	}

	@Override
	public void action(CommandInvokedEvent event) {
		if(event.getParsedCommand().isOptionPresent(DefaultCommandOption.HELP)) {
			sendCommandInfo(event.getSender());
			return;
		}
		String[] args = event.getParsedCommand().getArguments();
		if(args.length < 2 || args.length > 3) {
			event.getSender().sendMessage("Usage: money <add/remove/set/get> <user id> <amount>");
			return;
		}

		GraphiteUser u = Graphite.getUser(args[1]);
		if(u == null) {
			event.getSender().sendMessage("User doesn't exist");
			return;
		}
		GraphiteEconomy ec = Graphite.getEconomy();
		if(args[0].equalsIgnoreCase("add")) {
			int amount = Integer.valueOf(args[2]);
			ec.addMoney(u, amount);
			event.getSender().sendMessage("Added " + amount + " amount of money to " + u.getName());
		}else if(args[0].equalsIgnoreCase("remove")) {
			int amount = Integer.valueOf(args[2]);
			if((ec.getMoney(u) - amount) < 0) {
				event.getSender().sendMessage("Don't do that");
				return;
			}
			ec.withdrawMoney(u, amount);
			event.getSender().sendMessage("Removed " + amount + " amount of money from " + u.getName());
		}else if(args[0].equalsIgnoreCase("set")) {
			int amount = Integer.valueOf(args[2]);
			if(amount < 0) {
				event.getSender().sendMessage("Don't do that");
				return;
			}
			ec.setMoney(u, amount);
			event.getSender().sendMessage("Set the amount of money from " + u.getName() + " to " + amount);
		}else if(args[0].equalsIgnoreCase("get")) {
			int m = ec.getMoney(u);
			event.getSender().sendMessage("The user " + u.getName() + " currently has " + m + " amount of money");
		}else {
			event.getSender().sendMessage("Usage: money <add/remove/set/get> <user id> <amount>");
		}
	}

}
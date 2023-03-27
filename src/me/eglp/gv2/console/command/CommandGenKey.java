package me.eglp.gv2.console.command;

import java.util.Arrays;
import java.util.Date;
import java.util.stream.Collectors;

import me.eglp.gv2.console.AbstractConsoleCommand;
import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.lang.GraphiteTimeParser;
import me.eglp.gv2.util.premium.PremiumKey;
import me.eglp.gv2.util.premium.PremiumKeyType;
import me.eglp.gv2.util.premium.PremiumLevel;
import me.mrletsplay.mrcore.command.event.CommandInvokedEvent;
import me.mrletsplay.mrcore.command.option.impl.DefaultCommandOption;

public class CommandGenKey extends AbstractConsoleCommand {
	
	public CommandGenKey() {
		super("genkey");
		setDescription("Generates a premium key");
		addOption(DefaultCommandOption.HELP);
	}
	
	@Override
	public void action(CommandInvokedEvent event) {
		if(event.getParsedCommand().isOptionPresent(DefaultCommandOption.HELP)) {
			sendCommandInfo(event.getSender());
			return;
		}
		String[] args = event.getParsedCommand().getArguments();
		if(args.length < 2) {
			event.getSender().sendMessage("Usage: genkey <Premiumlevel> <type> [...]");
			return;
		}
		PremiumLevel lvl;
		try {
			lvl= PremiumLevel.valueOf(args[0].toUpperCase());
		}catch(IllegalArgumentException e) {
			event.getSender().sendMessage("Invalid level (Valid: "+Arrays.toString(PremiumLevel.values())+")");
			return;
		}
		PremiumKeyType type;
		try {
			type= PremiumKeyType.valueOf(args[1].toUpperCase());
		}catch(IllegalArgumentException e) {
			event.getSender().sendMessage("Invalid type (Valid: "+Arrays.toString(PremiumKeyType.values())+")");
			return;
		}
		PremiumKey key = null;
		switch(type) {
			case PATREON:
			{
				event.getSender().sendMessage("Can't generate a patreon key!");
				return;
			}
			case PERMANENT:
			{
				key = Graphite.getPremium().generatePermanentKey(lvl);
				break;
			}
			case TEMPORARY:
			{
				long dur = GraphiteTimeParser.parseDuration(null, Arrays.stream(args).skip(2).collect(Collectors.joining(" ")));
				if(dur == -1) {
					event.getSender().sendMessage("Invalid duration");
					return;
				}
				long exp = System.currentTimeMillis() + dur;
				event.getSender().sendMessage("Key will expire at " + new Date(exp).toString());
				key = Graphite.getPremium().generateTemporaryKey(lvl, exp);
				break;
			}
			default:
				event.getSender().sendMessage("Unexpected unsupported type");
				return;
		}
		event.getSender().sendMessage("Your #fancy " + lvl + " key: " + key.getID());
	}

}

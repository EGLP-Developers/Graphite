package me.eglp.gv2.console.command;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import me.eglp.gv2.console.AbstractConsoleCommand;
import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.premium.PremiumKey;
import me.eglp.gv2.util.premium.TemporaryPremiumKey;
import me.mrletsplay.mrcore.command.event.CommandInvokedEvent;
import me.mrletsplay.mrcore.command.option.impl.DefaultCommandOption;

public class CommandListKeys extends AbstractConsoleCommand {
	
	public CommandListKeys() {
		super("listkeys");
		addAlias("lk");
		setDescription("Lists all premium keys");
		addOption(DefaultCommandOption.HELP);
	}
	
	@Override
	public void action(CommandInvokedEvent event) {
		if(event.getParsedCommand().isOptionPresent(DefaultCommandOption.HELP) || event.getParsedCommand().getArguments().length != 0) {
			sendCommandInfo(event.getSender());
			return;
		}
		List<String> ss = new ArrayList<>();
		for(PremiumKey k : Graphite.getPremium().getPremiumKeys()) {
			String s = k.getID()+": "+k.getPremiumLevel();
			s += "; type=" + k.getKeyType();
			if(k.getOwner() != null) s += "; owned by " + k.getOwner().getID() + " (" + k.getOwner().getName() + "#" + k.getOwner().getDiscriminator() + ")";
			if(k.isInUse()) s += "; in use on " + k.getRedeemedGuild().getID() + " (" + k.getRedeemedGuild().getName() + ")";
			if(k.isTemporary()) s += "; expires at "+LocalDateTime.ofEpochSecond(Math.floorDiv(((TemporaryPremiumKey) k).getExpiresAt(), 1000), 0, ZoneOffset.UTC);
			ss.add(s);
		}
		event.getSender().sendMessage(ss.stream().collect(Collectors.joining("\n")));
	}

}

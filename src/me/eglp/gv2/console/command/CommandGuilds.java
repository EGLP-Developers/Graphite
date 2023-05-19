package me.eglp.gv2.console.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import me.eglp.gv2.console.AbstractConsoleCommand;
import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.main.Graphite;
import me.mrletsplay.mrcore.command.event.CommandInvokedEvent;
import me.mrletsplay.mrcore.command.option.impl.DefaultCommandOption;

public class CommandGuilds extends AbstractConsoleCommand {

	public CommandGuilds() {
		super("guilds");
		setDescription("Shows number of guilds + guild names/ids (w/ respective member count)");
		addOption(DefaultCommandOption.HELP);
	}

	@Override
	public void action(CommandInvokedEvent event) {
		if(event.getParsedCommand().isOptionPresent(DefaultCommandOption.HELP) || event.getParsedCommand().getArguments().length != 0) {
			sendCommandInfo(event.getSender());
			return;
		}

		event.getSender().sendMessage("Guild count: " + Graphite.getGuildCount());

		List<GraphiteGuild> guilds = new ArrayList<>(Graphite.getGuilds());
		Collections.sort(guilds, Comparator.comparingInt(g -> g.getJDAGuild().getMemberCount()));
		for(GraphiteGuild guild : guilds) {
			event.getSender().sendMessage(guild.getName() + ": " + guild.getID() + " (Member count: " + guild.getJDAGuild().getMemberCount() + ")");
		}
	}

}

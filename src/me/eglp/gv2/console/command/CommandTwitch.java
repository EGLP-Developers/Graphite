package me.eglp.gv2.console.command;

import java.util.List;
import java.util.stream.Collectors;

import me.eglp.gv2.console.AbstractConsoleCommand;
import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.multiplex.bot.GlobalBot;
import me.eglp.twitch.entity.TwitchUser;
import me.mrletsplay.mrcore.command.event.CommandInvokedEvent;
import me.mrletsplay.mrcore.command.option.impl.DefaultCommandOption;

public class CommandTwitch  extends AbstractConsoleCommand {

	public CommandTwitch() {
		super("twitch");
		setDescription("Twitch stuff");
		addOption(DefaultCommandOption.HELP);
	}

	@Override
	public void action(CommandInvokedEvent event) {
		if(event.getParsedCommand().isOptionPresent(DefaultCommandOption.HELP)) {
			sendCommandInfo(event.getSender());
			return;
		}
		String[] args = event.getParsedCommand().getArguments();
		if(args.length < 1) {
			event.getSender().sendMessage("Usage: twitch <streamer-name/streamer-id>");
			return;
		}
		if(args[0].equalsIgnoreCase("id")) {
			TwitchUser u2 = Graphite.getTwitch().getTwitchAPI().getUserByID(args[1]);
			if(u2 == null) {
				event.getSender().sendMessage("Streamer doesn't exist");
				return;
			}
			event.getSender().sendMessage("Twitch User: " + u2.getDisplayName() + "; User ID: " + u2.getID());
		}else if(args[0].equalsIgnoreCase("name")) {
			TwitchUser u = Graphite.getTwitch().getTwitchAPI().getUserByName(args[1]);
			if(u == null) {
				event.getSender().sendMessage("Streamer doesn't exist");
				return;
			}
			event.getSender().sendMessage("Twitch User: " + u.getDisplayName() + "; User ID: " + u.getID());
		}else if(args[0].equalsIgnoreCase("list")){
			List<GraphiteGuild> guilds = Graphite.withBot(GlobalBot.INSTANCE, () -> Graphite.getGuilds());
			guilds.forEach(g -> {
				event.getSender().sendMessage(g.getTwitchConfig().getTwitchUsers().stream().map(tU -> "[" + g.getID() + "] " + g.getName() + " > \n" + tU.getTwitchUser().getDisplayName() + " / " + tU.getTwitchUser().getID()).collect(Collectors.joining("\n ")));
			});
		}else {
			sendCommandInfo(event.getSender());
		}
	}

}

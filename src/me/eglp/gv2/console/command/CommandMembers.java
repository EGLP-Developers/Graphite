package me.eglp.gv2.console.command;

import me.eglp.gv2.console.AbstractConsoleCommand;
import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.main.Graphite;
import me.mrletsplay.mrcore.command.event.CommandInvokedEvent;
import me.mrletsplay.mrcore.command.option.impl.DefaultCommandOption;
import net.dv8tion.jda.api.entities.Member;

public class CommandMembers extends AbstractConsoleCommand {

	public CommandMembers() {
		super("members");
		setDescription("Shows all the members of a guild");
		addOption(DefaultCommandOption.HELP);
	}

	@Override
	public void action(CommandInvokedEvent event) {
		if(event.getParsedCommand().isOptionPresent(DefaultCommandOption.HELP)) {
			sendCommandInfo(event.getSender());
			return;
		}
		String[] args = event.getParsedCommand().getArguments();
		if(args.length == 0) {
			event.getSender().sendMessage("Usage: members <guild id>");
			return;
		}
		GraphiteGuild guild = Graphite.getGuild(args[0]);
		if(guild == null) {
			event.getSender().sendMessage("Guild not found");
			return;
		}

		if(guild.getJDAGuild().getMemberCount() > 100) {
			event.getSender().sendMessage("Guild has >100 members");
			return;
		}

		for(Member m : guild.getJDAGuild().loadMembers().get()) {
			event.getSender().sendMessage(m.getUser().getName() + "#" + m.getUser().getDiscriminator() + ": " + m.getUser().getId());
		}
	}

}

package me.eglp.gv2.commands.premium;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.apis.patreon.PatreonPledge;
import me.eglp.gv2.util.command.Command;
import me.eglp.gv2.util.command.CommandCategory;
import me.eglp.gv2.util.command.CommandInvokedEvent;
import me.eglp.gv2.util.lang.DefaultLocaleString;
import me.eglp.gv2.util.lang.DefaultMessage;
import me.eglp.gv2.util.selfcheck.SpecialSelfcheck;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

public class CommandDonators extends Command {

	public CommandDonators() {
		super(null, CommandCategory.PREMIUM, "donators");
		setDescription(DefaultLocaleString.COMMAND_DONATORS_DESCRIPTION);
		setUsage(DefaultLocaleString.COMMAND_DONATORS_USAGE);
	}

	@SpecialSelfcheck(needsPermission = false)
	@Override
	public void action(CommandInvokedEvent event) {
		if(Graphite.getMainBotInfo().isBeta()) {
			event.reply("Sorry but you can't use this feature in bot beta mode");
			return;
		}
		
		List<PatreonPledge> pp = Graphite.getPatreon().getPledges();
		if(pp.isEmpty()) {
			DefaultMessage.COMMAND_DONATORS_EMPTY.reply(event, "patreon", Graphite.getMainBotInfo().getLinks().getPatreon());
			return;
		}
		
		String donators = pp.stream()
			.map(u -> u.getPatron().getFullName() + " (Discord: " + u.getPatron().getDiscordUser().getName() + ")")
			.collect(Collectors.joining("\n"));
		
		MessageCreateBuilder mb = new MessageCreateBuilder();
		mb.addContent(DefaultLocaleString.COMMAND_DONATORS_TITLE.getFor(event.getAuthor()));
		mb.addContent("```\n" + donators + "\n```");
		event.reply(mb.build());
	}

	@Override
	public List<OptionData> getOptions() {
		return Collections.emptyList();
	}

}

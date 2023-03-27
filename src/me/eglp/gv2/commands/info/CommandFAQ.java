package me.eglp.gv2.commands.info;

import java.util.Collections;
import java.util.List;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.command.Command;
import me.eglp.gv2.util.command.CommandCategory;
import me.eglp.gv2.util.command.CommandInvokedEvent;
import me.eglp.gv2.util.lang.DefaultLocaleString;
import me.eglp.gv2.util.selfcheck.SpecialSelfcheck;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class CommandFAQ extends Command{
	
	public CommandFAQ() {
		super(null, CommandCategory.INFO, "faq");
		setDescription(DefaultLocaleString.COMMAND_FAQ_DESCRIPTION);
		setUsage(DefaultLocaleString.COMMAND_FAQ_USAGE);
	}
	
	@SpecialSelfcheck(needsPermission = false)
	@Override
	public void action(CommandInvokedEvent event) {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setDescription("Our [FAQ](" + Graphite.getMainBotInfo().getWebsite().getFAQURL() + "):heart:");
		event.reply(eb.build());
	}

	@Override
	public List<OptionData> getOptions() {
		return Collections.emptyList();
	}

}

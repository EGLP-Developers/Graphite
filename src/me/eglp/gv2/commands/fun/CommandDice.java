package me.eglp.gv2.commands.fun;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import me.eglp.gv2.guild.GraphiteModule;
import me.eglp.gv2.util.command.Command;
import me.eglp.gv2.util.command.CommandCategory;
import me.eglp.gv2.util.command.CommandInvokedEvent;
import me.eglp.gv2.util.emote.JDAEmote;
import me.eglp.gv2.util.lang.DefaultLocaleString;
import me.eglp.gv2.util.selfcheck.SpecialSelfcheck;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class CommandDice extends Command {

	public CommandDice() {
		super(GraphiteModule.FUN, CommandCategory.FUN, "dice");
		addAlias("roll");
		setAllowPrivate(true);
		setDescription(DefaultLocaleString.COMMAND_DICE_DESCRIPTION);
		setUsage(DefaultLocaleString.COMMAND_DICE_USAGE);
	}

	@SpecialSelfcheck(needsPermission = false)
	@Override
	public void action(CommandInvokedEvent event) {
		int r = new Random().nextInt(6) + 1;
		event.reply(JDAEmote.getDiceNumber(r).getUnicode());
	}

	@Override
	public List<OptionData> getOptions() {
		return Collections.emptyList();
	}

}

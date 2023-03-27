package me.eglp.gv2.commands.info;

import java.util.Collections;
import java.util.List;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.command.Command;
import me.eglp.gv2.util.command.CommandCategory;
import me.eglp.gv2.util.command.CommandInvokedEvent;
import me.eglp.gv2.util.command.SpecialExecute;
import me.eglp.gv2.util.lang.DefaultLocaleString;
import me.eglp.gv2.util.lang.DefaultMessage;
import me.eglp.gv2.util.selfcheck.SpecialSelfcheck;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class CommandInvite extends Command {

	public CommandInvite() {
		super(null, CommandCategory.INFO, "invite");
		setDescription(DefaultLocaleString.COMMAND_INVITE_DESCRIPTION);
		setUsage(DefaultLocaleString.COMMAND_INVITE_USAGE);
	}

	@SpecialExecute(allowPrivate = true)
	@SpecialSelfcheck(needsPermission = false)
	@Override
	public void action(CommandInvokedEvent event) {
		DefaultMessage.COMMAND_INVITE_MESSAGE.reply(event, "invite_url", Graphite.getInviteUrl());
	}

	@Override
	public List<OptionData> getOptions() {
		return Collections.emptyList();
	}

}

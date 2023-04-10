package me.eglp.gv2.commands.admin;

import java.util.Collections;
import java.util.List;

import me.eglp.gv2.util.command.Command;
import me.eglp.gv2.util.command.CommandCategory;
import me.eglp.gv2.util.command.CommandInvokedEvent;
import me.eglp.gv2.util.command.slash.DeferredReply;
import me.eglp.gv2.util.lang.DefaultLocaleString;
import me.eglp.gv2.util.permission.DefaultPermissions;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class CommandUpdateSlashCommands extends Command {
	
	public CommandUpdateSlashCommands() {
		super(null, CommandCategory.ADMIN, "updateslashcommands");
		setDescription(DefaultLocaleString.COMMAND_UPDATESLASHCOMMANDS_DESCRIPTION);
		setUsage(DefaultLocaleString.COMMAND_UPDATESLASHCOMMANDS_USAGE);
		setPermission(DefaultPermissions.ADMIN_UPDATESLASHCOMMANDS);
	}
	
	@Override
	public void action(CommandInvokedEvent event) {
		DeferredReply r = event.deferReply("Updating commands");
		event.getGuild().getJDAGuild().updateCommands().complete();
		event.getGuild().getCustomCommandsConfig().updateSlashCommands();
		r.editOriginal("Done!");
	}

	@Override
	public List<OptionData> getOptions() {
		return Collections.emptyList();
	}

}

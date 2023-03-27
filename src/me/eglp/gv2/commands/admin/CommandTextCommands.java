package me.eglp.gv2.commands.admin;

import java.util.Collections;
import java.util.List;

import me.eglp.gv2.util.command.Command;
import me.eglp.gv2.util.command.CommandCategory;
import me.eglp.gv2.util.command.CommandInvokedEvent;
import me.eglp.gv2.util.command.ParentCommand;
import me.eglp.gv2.util.lang.DefaultLocaleString;
import me.eglp.gv2.util.lang.DefaultMessage;
import me.eglp.gv2.util.permission.DefaultPermissions;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class CommandTextCommands extends ParentCommand {

	public CommandTextCommands() {
		super(null, CommandCategory.ADMIN, "textcommands");
		setDescription(DefaultLocaleString.COMMAND_TEXTCOMMANDS_DESCRIPTION);
		
		addSubCommand(new Command(this, "enable") {
			
			@Override
			public void action(CommandInvokedEvent event) {
				event.getGuild().getConfig().setTextCommands(true);
				DefaultMessage.COMMAND_TEXTCOMMANDS_ENABLE_MESSAGE.reply(event, "prefix", event.getGuild().getPrefix());
			}
			
			@Override
			public List<OptionData> getOptions() {
				return Collections.emptyList();
			}
		})
		.setDescription(DefaultLocaleString.COMMAND_TEXTCOMMANDS_ENABLE_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_TEXTCOMMANDS_ENABLE_USAGE)
		.setPermission(DefaultPermissions.ADMIN_TEXTCOMMANDS);
		
		addSubCommand(new Command(this, "disable") {
			
			@Override
			public void action(CommandInvokedEvent event) {
				event.getGuild().getConfig().setTextCommands(false);
				DefaultMessage.COMMAND_TEXTCOMMANDS_DISABLE_MESSAGE.reply(event);
			}
			
			@Override
			public List<OptionData> getOptions() {
				return Collections.emptyList();
			}
		})
		.setDescription(DefaultLocaleString.COMMAND_TEXTCOMMANDS_DISABLE_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_TEXTCOMMANDS_DISABLE_USAGE)
		.setPermission(DefaultPermissions.ADMIN_TEXTCOMMANDS);
	}
	
}

package me.eglp.gv2.commands.admin;

import java.util.Arrays;
import java.util.List;

import me.eglp.gv2.util.base.guild.config.GuildConfig;
import me.eglp.gv2.util.command.Command;
import me.eglp.gv2.util.command.CommandCategory;
import me.eglp.gv2.util.command.CommandInvokedEvent;
import me.eglp.gv2.util.lang.DefaultLocaleString;
import me.eglp.gv2.util.lang.DefaultMessage;
import me.eglp.gv2.util.permission.DefaultPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class CommandPrefix extends Command {
	
	public CommandPrefix() {
		super(null, CommandCategory.ADMIN, "prefix");
		setPermission(DefaultPermissions.ADMIN_PREFIX);
		setDescription(DefaultLocaleString.COMMAND_PREFIX_DESCRIPTION);
		setUsage(DefaultLocaleString.COMMAND_PREFIX_USAGE);
	}
	
	@Override
	public void action(CommandInvokedEvent event) {
		String prefix = (String) event.getOption("prefix");
		String newPrefix = prefix;
		if(!GuildConfig.PREFIX_PATTERN.matcher(newPrefix).matches()) {
			DefaultMessage.COMMAND_PREFIX_INVALID.reply(event);
			return;
		}
		
		event.getGuild().getConfig().setPrefix(newPrefix);
		DefaultMessage.COMMAND_PREFIX_SUCCESS.reply(event, "prefix", newPrefix);
	}
	
	@Override
	public List<OptionData> getOptions() {
		return Arrays.asList(
				new OptionData(OptionType.STRING, "prefix", "The new prefix for the bot", true)
			);
	}

}

package me.eglp.gv2.commands.admin;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import me.eglp.gv2.util.command.Command;
import me.eglp.gv2.util.command.CommandCategory;
import me.eglp.gv2.util.command.CommandInvokedEvent;
import me.eglp.gv2.util.lang.DefaultLocaleString;
import me.eglp.gv2.util.lang.DefaultMessage;
import me.eglp.gv2.util.permission.DefaultPermissions;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.AutoCompleteQuery;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class CommandTimezone extends Command {

	public CommandTimezone() {
		super(null, CommandCategory.ADMIN, "timezone");
		setDescription(DefaultLocaleString.COMMAND_TIMEZONE_DESCRIPTION);
		setUsage(DefaultLocaleString.COMMAND_TIMEZONE_USAGE);
		setPermission(DefaultPermissions.ADMIN_TIMEZONE);
	}
	
	@Override
	public void action(CommandInvokedEvent event) {
		String zone = (String) event.getOption("timezone");
		
		ZoneId zoneId;
		try {
			zoneId = ZoneId.of(zone);
		}catch(DateTimeException e) {
			DefaultMessage.COMMAND_TIMEZONE_INVALID_TIMEZONE.reply(event);
			return;
		}
		
		event.getGuild().getConfig().setTimezone(zoneId);
		DefaultMessage.COMMAND_TIMEZONE_MESSAGE.reply(event, "timezone", zoneId.getId());
	}

	@Override
	public List<OptionData> getOptions() {
		return Arrays.asList(
			new OptionData(OptionType.STRING, "timezone", "The timezone to use", true).setAutoComplete(true)
		);
	}
	
	@Override
	public List<Choice> complete(CommandAutoCompleteInteractionEvent event) {
		AutoCompleteQuery q = event.getFocusedOption();
		if(q.getName().equals("timezone")) {
			return ZoneId.getAvailableZoneIds().stream()
				.map(z -> new Choice(z, z))
				.collect(Collectors.toList());
		}
		
		return Collections.emptyList();
	}

}

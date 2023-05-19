package me.eglp.gv2.commands.moderation;

import java.util.Arrays;
import java.util.List;

import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.guild.GraphiteModule;
import me.eglp.gv2.guild.config.GuildReportsConfig;
import me.eglp.gv2.user.GraphiteUser;
import me.eglp.gv2.util.command.Command;
import me.eglp.gv2.util.command.CommandCategory;
import me.eglp.gv2.util.command.CommandInvokedEvent;
import me.eglp.gv2.util.lang.DefaultLocaleString;
import me.eglp.gv2.util.lang.DefaultMessage;
import me.eglp.gv2.util.selfcheck.SpecialSelfcheck;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class CommandReport extends Command {

	public CommandReport() {
		super(GraphiteModule.MODERATION, CommandCategory.MODERATION, "report");
		setDescription(DefaultLocaleString.COMMAND_REPORT_DESCRIPTION);
		setUsage(DefaultLocaleString.COMMAND_REPORT_USAGE);
	}

	@SpecialSelfcheck(needsPermission = false)
	@Override
	public void action(CommandInvokedEvent event) {
		GraphiteUser user = (GraphiteUser) event.getOption("user");
		if(user.isBot()) {
			DefaultMessage.ERROR_IS_BOT.reply(event);
			return;
		}

		GraphiteGuild g = event.getGuild();
		GuildReportsConfig c = g.getReportsConfig();
		if(c.hasReported(event.getAuthor(), user)) {
			DefaultMessage.COMMAND_REPORT_ALREADY_REPORTED.reply(event);
			return;
		}

		if(event.getAuthor().equals(user)) {
			DefaultMessage.COMMAND_REPORT_CANT_REPORT_SELF.reply(event);
			return;
		}

		String reason = (String)event.getOption("reason");
		c.createReport(event.getAuthor(), user, reason);

		DefaultMessage.COMMAND_REPORT_SUCCESS.reply(event, "user", user.getAsMention(), "reason", reason);
	}

	@Override
	public List<OptionData> getOptions() {
		return Arrays.asList(
				new OptionData(OptionType.USER, "user", "The user you want to report", true),
				new OptionData(OptionType.STRING, "reason", "The description why you reported this user", true)
			);
	}

}

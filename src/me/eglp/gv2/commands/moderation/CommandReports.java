package me.eglp.gv2.commands.moderation;

import java.awt.Color;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import me.eglp.gv2.util.base.guild.GraphiteGuild;
import me.eglp.gv2.util.base.guild.GraphiteModule;
import me.eglp.gv2.util.base.guild.GuildReport;
import me.eglp.gv2.util.base.guild.config.GuildReportsConfig;
import me.eglp.gv2.util.command.CommandCategory;
import me.eglp.gv2.util.command.CommandInvokedEvent;
import me.eglp.gv2.util.command.ParentCommand;
import me.eglp.gv2.util.command.Command;
import me.eglp.gv2.util.lang.DefaultLocaleString;
import me.eglp.gv2.util.lang.DefaultMessage;
import me.eglp.gv2.util.permission.DefaultPermissions;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class CommandReports extends ParentCommand {

	public CommandReports() {
		super(GraphiteModule.MODERATION, CommandCategory.MODERATION, "reports");
		setDescription(DefaultLocaleString.COMMAND_REPORTS_DESCRIPTION);
		setPermission(DefaultPermissions.MODERATION_REPORTS);
		
		addSubCommand(new Command(this, "list") {
			
			@Override
			public void action(CommandInvokedEvent event) {
				GraphiteGuild g = event.getGuild();
				GuildReportsConfig c = g.getReportsConfig();
				if(c.getReports().isEmpty()) {
					DefaultMessage.COMMAND_REPORTS_NO_REPORTS.reply(event);
					return;
				}
				
				EmbedBuilder eb = new EmbedBuilder();
				eb.setColor(Color.ORANGE);
				int index = 0;
				for(GuildReport r : c.getReports()) {
					eb.addField(
							DefaultLocaleString.COMMAND_REPORTS_FIELD_HEAD.getFor(event.getSender(), 
									"reporter", r.getReporter().getName(),
									"reported", r.getReported().getName(),
									"index", String.valueOf(index)),
							
							DefaultLocaleString.COMMAND_REPORTS_FIELD_VALUES.getFor(event.getSender(),
									"reason", r.getReason()), false);
					index++;
				}
				eb.setFooter(DefaultLocaleString.COMMAND_REPORTS_FIELD_FOOTER.getFor(event.getSender(), "reports", ""+c.getReports().size()), null);
				event.reply(eb.build());
			}
			
			@Override
			public List<OptionData> getOptions() {
				return Collections.emptyList();
			}
		})
		.setPermission(DefaultPermissions.MODERATION_REPORTS_LIST)
		.setDescription(DefaultLocaleString.COMMAND_REPORTS_LIST_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_REPORTS_LIST_USAGE);
		
		addSubCommand(new Command(this, "remove") {
			
			@Override
			public void action(CommandInvokedEvent event) {
				GraphiteGuild g = event.getGuild();
				GuildReportsConfig c = g.getReportsConfig();
				List<GuildReport> reports = c.getReports();
				long index = (long) event.getOption("index");
				if(index < 0 || index >= reports.size()) {
					DefaultMessage.ERROR_OUT_OF_BOUNDS.reply(event);
					return;
				}
				
				c.removeReport(reports.get((int) index));
				
				DefaultMessage.COMMAND_REPORTS_REMOVE_SUCCESS.reply(event,
						"index", String.valueOf(index));
			}
			
			@Override
			public List<OptionData> getOptions() {
				return Arrays.asList(
						new OptionData(OptionType.INTEGER, "index", "Index of the report to remove", true)
					);
			}
		})
		.setPermission(DefaultPermissions.MODERATION_REPORTS_REMOVE)
		.setUsage(DefaultLocaleString.COMMAND_REPORTS_REMOVE_USAGE)
		.setDescription(DefaultLocaleString.COMMAND_REPORTS_REMOVE_DESCRIPTION);
	}

}

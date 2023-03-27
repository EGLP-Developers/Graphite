package me.eglp.gv2.commands.moderation;

import java.awt.Color;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import me.eglp.gv2.util.base.guild.GraphiteGuild;
import me.eglp.gv2.util.base.guild.GraphiteModule;
import me.eglp.gv2.util.base.guild.chatreport.GuildChatReport;
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

public class CommandChatReports extends ParentCommand {

	public CommandChatReports() {
		super(GraphiteModule.MODERATION, CommandCategory.MODERATION, "chatreports");
		setDescription(DefaultLocaleString.COMMAND_CHATREPORTS_DESCRIPTION);
		setPermission(DefaultPermissions.MODERATION_CHATREPORTS);
		
		addSubCommand(new Command(this, "list") {
			
			@Override
			public void action(CommandInvokedEvent event) {
				GraphiteGuild g = event.getGuild();
				GuildReportsConfig c = g.getReportsConfig();
				if(c.getChatReports().isEmpty()) {
					DefaultMessage.COMMAND_CHATREPORTS_LIST_NO_REPORTS.reply(event);
					return;
				}
				
				EmbedBuilder eb = new EmbedBuilder();
				eb.setColor(Color.ORANGE);
				int index = 0;
				for(GuildChatReport r : c.getChatReports()) {
					eb.appendDescription(DefaultLocaleString.COMMAND_CHATREPORTS_LIST_FIELD_HEAD.getFor(event.getSender(), 
									"reporter", r.getReporter(),
									"index", String.valueOf(index)));
					index++;
				}
				eb.setFooter(DefaultLocaleString.COMMAND_CHATREPORTS_LIST_FIELD_FOOTER.getFor(event.getSender(), "chatreports", ""+c.getChatReports().size()), null);
				event.reply(eb.build());
			}
			
			@Override
			public List<OptionData> getOptions() {
				return Collections.emptyList();
			}
		})
		.setPermission(DefaultPermissions.MODERATION_CHATREPORTS_LIST)
		.setDescription(DefaultLocaleString.COMMAND_CHATREPORTS_LIST_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_CHATREPORTS_LIST_USAGE);
		
		addSubCommand(new Command(this, "remove") {
			
			@Override
			public void action(CommandInvokedEvent event) {
				GraphiteGuild g = event.getGuild();
				GuildReportsConfig c = g.getReportsConfig();
				List<GuildChatReport> reports = c.getChatReports();
				long index = (long) event.getOption("index");
				if(index < 0 || index >= reports.size()) {
					DefaultMessage.ERROR_OUT_OF_BOUNDS.reply(event);
					return;
				}
				
				c.removeChatReport(c.getChatReports().get((int) index));
				
				DefaultMessage.COMMAND_CHATREPORTS_REMOVE_SUCCESS.reply(event,
						"index", String.valueOf(index));
			}
			
			@Override
			public List<OptionData> getOptions() {
				return Arrays.asList(
						new OptionData(OptionType.INTEGER, "index", "Index of the report to remove", true)
					);
			}
		})
		.setPermission(DefaultPermissions.MODERATION_CHATREPORTS_REMOVE)
		.setUsage(DefaultLocaleString.COMMAND_CHATREPORTS_REMOVE_USAGE)
		.setDescription(DefaultLocaleString.COMMAND_CHATREPORTS_REMOVE_DESCRIPTION);
	}

}

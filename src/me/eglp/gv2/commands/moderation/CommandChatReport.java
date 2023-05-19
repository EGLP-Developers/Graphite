package me.eglp.gv2.commands.moderation;

import java.util.Collections;
import java.util.List;

import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.guild.GraphiteModule;
import me.eglp.gv2.guild.config.GuildReportsConfig;
import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.command.Command;
import me.eglp.gv2.util.command.CommandCategory;
import me.eglp.gv2.util.command.CommandInvokedEvent;
import me.eglp.gv2.util.lang.DefaultLocaleString;
import me.eglp.gv2.util.lang.DefaultMessage;
import me.eglp.gv2.util.selfcheck.SpecialSelfcheck;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class CommandChatReport extends Command {

	public CommandChatReport() {
		super(GraphiteModule.MODERATION, CommandCategory.MODERATION, "chatreport");
		setDescription(DefaultLocaleString.COMMAND_CHATREPORT_DESCRIPTION);
		setUsage(DefaultLocaleString.COMMAND_CHATREPORT_USAGE);
	}

	@SpecialSelfcheck(needsPermission = false)
	@Override
	public void action(CommandInvokedEvent event) {
		GraphiteGuild g = event.getGuild();
		GuildReportsConfig c = g.getReportsConfig();
		if(c.getChatReportKey() == null) {
			DefaultMessage.COMMAND_CHATREPORT_NOT_ENABLED.reply(event, "webinterface", Graphite.getMainBotInfo().getWebsite().getWebinterfaceURL());
			return;
		}

		List<Message> messages = event.getChannel().getJDAChannel().getHistory().retrievePast(100).complete();
		Collections.reverse(messages);

		c.createChatReport(event.getAuthor().getID(), event.getChannel().getJDAChannel(), messages);

		DefaultMessage.COMMAND_CHATREPORT_SUCCESS.reply(event, "webinterface", Graphite.getMainBotInfo().getWebsite().getWebinterfaceURL());
	}

	@Override
	public List<OptionData> getOptions() {
		return Collections.emptyList();
	}

}

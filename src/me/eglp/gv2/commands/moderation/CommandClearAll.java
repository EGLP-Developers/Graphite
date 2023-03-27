package me.eglp.gv2.commands.moderation;

import java.util.Collections;
import java.util.List;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.base.guild.GraphiteModule;
import me.eglp.gv2.util.command.CommandCategory;
import me.eglp.gv2.util.command.CommandInvokedEvent;
import me.eglp.gv2.util.command.slash.DeferredReply;
import me.eglp.gv2.util.command.Command;
import me.eglp.gv2.util.lang.DefaultLocaleString;
import me.eglp.gv2.util.lang.DefaultMessage;
import me.eglp.gv2.util.permission.DefaultPermissions;
import me.eglp.gv2.util.queue.GraphiteQueue;
import me.eglp.gv2.util.queue.GraphiteTaskInfo;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class CommandClearAll extends Command {
	
	private static final String CLEARALL_TASK_ID = "clearall";
	
	public CommandClearAll() {
		super(GraphiteModule.MODERATION, CommandCategory.MODERATION, "clearall");
		setDescription(DefaultLocaleString.COMMAND_CLEARALL_DESCRIPTION);
		setUsage(DefaultLocaleString.COMMAND_CLEARALL_USAGE);
		setPermission(DefaultPermissions.MODERATION_CLEARALL);
		requirePermissions(Permission.MESSAGE_MANAGE);
	}
	
	@Override
	public void action(CommandInvokedEvent event) {
		DeferredReply r = event.deferReply();
		String messageID = r.getMessage().getId();
		GraphiteQueue q = Graphite.getQueue(event.getGuild());
		if(q.isHeavyBusy()) r.editOriginal(DefaultMessage.OTHER_HEAVY_BUSY.createEmbed(event.getSender(), "patreon", Graphite.getMainBotInfo().getLinks().getPatreon()));
		q.queueHeavy(event.getGuild(), new GraphiteTaskInfo(CLEARALL_TASK_ID, "Deleting messages (clearall)"), () -> event.getTextChannel().clearAll(true, messageID))
			.thenRun(() -> r.editOriginal(DefaultMessage.COMMAND_CLEARALL_SUCCESS.createEmbed(event.getSender())));
	}
	
	@Override
	public List<OptionData> getOptions() {
		return Collections.emptyList();
	}

}

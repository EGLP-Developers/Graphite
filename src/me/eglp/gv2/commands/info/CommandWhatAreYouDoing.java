package me.eglp.gv2.commands.info;

import java.util.Collections;
import java.util.List;

import me.eglp.gv2.util.command.Command;
import me.eglp.gv2.util.command.CommandCategory;
import me.eglp.gv2.util.command.CommandInvokedEvent;
import me.eglp.gv2.util.command.SpecialExecute;
import me.eglp.gv2.util.lang.DefaultLocaleString;
import me.eglp.gv2.util.queue.GraphiteQueue;
import me.eglp.gv2.util.selfcheck.SpecialSelfcheck;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class CommandWhatAreYouDoing extends Command {

	public CommandWhatAreYouDoing() {
		super(null, CommandCategory.INFO, "whatareyoudoing");
		setDescription(DefaultLocaleString.COMMAND_WHATAREYOUDOING_DESCRIPTION);
		setUsage(DefaultLocaleString.COMMAND_WHATAREYOUDOING_USAGE);
		addAlias("wayd");
		addAlias("wyd");
	}

	@SpecialExecute(bypassQueue = true)
	@SpecialSelfcheck(needsPermission = false)
	@Override
	public void action(CommandInvokedEvent event) {
		EmbedBuilder b = new EmbedBuilder();
		b.setTitle("Current Status");
		GraphiteQueue q = event.getGuild().getResponsibleQueue();
		b.addField("Guild Busy", event.getGuild().isQueueBusy() ? "yes" : "no", true);
		b.addField("Guild Heavy Busy", q.isHeavyBusy(event.getGuild()) ? "yes" : "no", true);
		b.addField("Assigned Queue", q.getName(), false);
		b.addField("Queue Busy", q.isBusy() ? "yes" : "no", true);
		b.addField("Heavy Queue Busy", q.isHeavyBusy() ? "yes" : "no", true);
		b.addField("Current Heavy Task", q.isHeavyBusy(event.getGuild()) ? q.getHeavyTask(event.getGuild()).getName() + (q.getHeavyTask(event.getGuild()).isRunning() ? "" : " (Waiting in queue)") : "none", false);
		event.reply(b.build());
	}

	@Override
	public List<OptionData> getOptions() {
		return Collections.emptyList();
	}
	
}

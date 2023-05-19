package me.eglp.gv2.commands.moderation;

import java.util.Arrays;
import java.util.List;

import me.eglp.gv2.guild.GraphiteModule;
import me.eglp.gv2.main.Graphite;
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
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class CommandClear extends Command{

	private static final String CLEAR_TASK_ID = "clear";

	public CommandClear() {
		super(GraphiteModule.MODERATION, CommandCategory.MODERATION, "clear");
		setDescription(DefaultLocaleString.COMMAND_CLEAR_DESCRIPTION);
		setUsage(DefaultLocaleString.COMMAND_CLEAR_USAGE);
		setPermission(DefaultPermissions.MODERATION_CLEAR);
		requirePermissions(Permission.MESSAGE_MANAGE);
	}

	@Override
	public void action(CommandInvokedEvent event) {
		long amount = event.hasOption("amount") ? (long) event.getOption("amount") : 1;
		if(amount <= 0 || amount > 99) {
			DefaultMessage.COMMAND_CLEAR_INVALID_AMOUNT.reply(event);
			return;
		}

		final int fAmount = (int) (event.isUsingSlashCommand() ? amount : amount + 1);

		DeferredReply r = event.deferReply();
		String messageID = r.getMessage().getId();
		GraphiteQueue q = Graphite.getQueue();
		if(q.isHeavyBusy()) r.editOriginal(DefaultMessage.OTHER_HEAVY_BUSY.createEmbed(event.getSender(), "patreon", Graphite.getMainBotInfo().getLinks().getPatreon()));
		q.queueHeavy(event.getGuild(), new GraphiteTaskInfo(CLEAR_TASK_ID, "Deleting messages (clear)"), () -> event.getTextChannel().clear((int) fAmount, messageID))
			.thenRun(() -> r.editOriginal(DefaultMessage.COMMAND_CLEAR_SUCCESS.createEmbed(event.getSender(), "amount", ""+fAmount)));
	}

	@Override
	public List<OptionData> getOptions() {
		return Arrays.asList(
				new OptionData(OptionType.INTEGER, "amount", "The amount of messages to remove", false)
			);
	}

}

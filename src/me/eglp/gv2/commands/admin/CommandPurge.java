package me.eglp.gv2.commands.admin;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.base.guild.GraphiteTextChannel;
import me.eglp.gv2.util.command.Command;
import me.eglp.gv2.util.command.CommandCategory;
import me.eglp.gv2.util.command.CommandInvokedEvent;
import me.eglp.gv2.util.input.ButtonInput;
import me.eglp.gv2.util.lang.DefaultLocaleString;
import me.eglp.gv2.util.lang.DefaultMessage;
import me.eglp.gv2.util.permission.DefaultPermissions;
import me.eglp.gv2.util.queue.GraphiteQueue;
import me.eglp.gv2.util.queue.GraphiteTaskInfo;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;

public class CommandPurge extends Command {
	
	private static final String PURGE_TASK_ID = "purge";

	public CommandPurge() {
		super(null, CommandCategory.ADMIN, "purge");
		setDescription(DefaultLocaleString.COMMAND_PURGE_DESCRIPTION);
		setUsage(DefaultLocaleString.COMMAND_PURGE_USAGE);
		setPermission(DefaultPermissions.ADMIN_PURGE);
		requirePermissions(Permission.MANAGE_CHANNEL, Permission.MANAGE_ROLES);
	}

	@Override
	public void action(CommandInvokedEvent event) {
		String what = (String) event.getOption("what");
		
		Guild g = event.getGuild().getJDAGuild();
		
		ButtonInput<Integer> inp = new ButtonInput<Integer>(event.getAuthor(), ev -> {
			if(ev.getItem() == -1) {
				GraphiteQueue q = Graphite.getQueue(event.getGuild());
				if(q.isHeavyBusy()) DefaultMessage.OTHER_HEAVY_BUSY.reply(event, "patreon", Graphite.getMainBotInfo().getLinks().getPatreon());
				q.queueHeavy(event.getGuild(), new GraphiteTaskInfo(PURGE_TASK_ID, "Purging guild (purge)"), () -> {
					if(what.equalsIgnoreCase("channels")) {
						g.getChannels().forEach(ch -> {
							if(ch.equals(g.getRulesChannel()) || ch.equals(g.getCommunityUpdatesChannel())) return;
							ch.delete().complete();
						});
						
						GraphiteTextChannel tc = Graphite.getTextChannel(g.createTextChannel("default").complete());
						tc.sendMessage(DefaultMessage.COMMAND_PURGE_SUCCESS);
					}else if(what.equals("roles")) {
						g.getRoles().forEach(r -> {
							if(r.isManaged() || r.isPublicRole()) return;
							r.delete().complete();
						});
						
						DefaultMessage.COMMAND_PURGE_SUCCESS.reply(event);
					}
				});
			}else if(ev.getItem() == -2) {
				ev.markCancelled();
			}
		})
		.expireAfter(1, TimeUnit.MINUTES)
		.autoRemove(true)
		.removeMessage(false);
		
		inp.addOption(ButtonStyle.DANGER, "Purge", -1);
		inp.addOption(ButtonStyle.SECONDARY, "Cancel", -2);
		inp.replyEphemeral(event, DefaultLocaleString.COMMAND_PURGE_WARNING, "what", what);
	}
	
	@Override
	public List<OptionData> getOptions() {
		return Arrays.asList(
				new OptionData(OptionType.STRING, "what", "What to purge", true)
					.addChoice("Roles", "roles")
					.addChoice("Channels", "channels")
			);
	}

}

package me.eglp.gv2.commands.info;

import java.util.Collections;
import java.util.List;

import me.eglp.gv2.multiplex.GraphiteMultiplex;
import me.eglp.gv2.util.command.Command;
import me.eglp.gv2.util.command.CommandCategory;
import me.eglp.gv2.util.command.CommandInvokedEvent;
import me.eglp.gv2.util.command.SpecialExecute;
import me.eglp.gv2.util.lang.DefaultLocaleString;
import me.eglp.gv2.util.selfcheck.SpecialSelfcheck;
import me.eglp.gv2.util.voting.GraphiteVoteSource;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class CommandUpvote extends Command {

	public CommandUpvote() {
		super(null, CommandCategory.INFO, "upvote");
		addAlias("vote");
		setDescription(DefaultLocaleString.COMMAND_UPVOTE_DESCRIPTION);
		setUsage(DefaultLocaleString.COMMAND_UPVOTE_USAGE);
	}
	
	@SpecialExecute(allowPrivate = true)
	@SpecialSelfcheck(needsPermission = false)
	@Override
	public void action(CommandInvokedEvent event) {
		EmbedBuilder b = new EmbedBuilder();
		b.setTitle(DefaultLocaleString.COMMAND_UPVOTE_TITLE.getFor(event.getSender()));
		for(GraphiteVoteSource vs : GraphiteMultiplex.getCurrentBot().getVoteSources()) {
			b.addField(DefaultLocaleString.COMMAND_UPVOTE_FIELD_TITLE.getFor(event.getSender(), "site_name", vs.getName()), DefaultLocaleString.COMMAND_UPVOTE_FIELD_DESCRIPTION.getFor(event.getSender(), "upvote_url", vs.getUpvoteURL(GraphiteMultiplex.getCurrentBot()), "site_name", vs.getName()), false);
		}
		event.reply(b.build());
	}

	@Override
	public List<OptionData> getOptions() {
		return Collections.emptyList();
	}

}

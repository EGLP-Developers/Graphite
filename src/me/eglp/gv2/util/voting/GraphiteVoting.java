package me.eglp.gv2.util.voting;

import me.eglp.gv2.guild.GraphiteTextChannel;
import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.user.GraphiteUser;
import me.eglp.gv2.util.emote.JDAEmote;
import me.eglp.gv2.util.settings.MiscellaneousSettings;
import net.dv8tion.jda.api.EmbedBuilder;

public class GraphiteVoting {

	private GraphiteTextChannel voteChannel;

	public GraphiteVoting() {
		MiscellaneousSettings misc = Graphite.getMainBotInfo().getMiscellaneous();
		if(misc.getUpvotesChannelID() != null) {
			voteChannel = Graphite.getGuild(misc.getMessageServerID()).getTextChannelByID(misc.getUpvotesChannelID());
		}
	}

	public void addVotes(GraphiteVoteSource source, GraphiteUser user, int votes) {
		if(voteChannel != null && user.isAvailable()) {
			voteChannel.sendMessage(new EmbedBuilder()
					.setDescription(String.format("%s has upvoted the bot on [%s](%s)" + JDAEmote.DOLLARONEN.getUnicode(),
							user.getName(),
							source.getName(),
							source.getUpvoteURL()))
					.setAuthor(user.getName(), null, user.getJDAUser().getAvatarUrl())
					.build());
		}
	}

	public GraphiteVoteSource getVoteSource(String id) {
		return getVoteSources().stream()
				.filter(vs -> vs.getIdentifier().equals(id))
				.findFirst().orElse(null);
	}

}

package me.eglp.gv2.util.voting;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.multiplex.bot.GlobalBot;
import me.eglp.gv2.multiplex.bot.MultiplexBot;
import me.eglp.gv2.util.base.guild.GraphiteTextChannel;
import me.eglp.gv2.util.base.user.GraphiteUser;
import me.eglp.gv2.util.emote.JDAEmote;
import me.eglp.gv2.util.settings.MiscellaneousSettings;
import net.dv8tion.jda.api.EmbedBuilder;

public class GraphiteVoting {
	
	private GraphiteTextChannel voteChannel;
	
	public GraphiteVoting() {
		MiscellaneousSettings misc = Graphite.getMainBotInfo().getMiscellaneous();
		if(misc.getUpvotesChannelID() != null) {
			voteChannel = Graphite.withBot(GlobalBot.INSTANCE, () -> Graphite.getGuild(misc.getMessageServerID()).getTextChannelByID(misc.getUpvotesChannelID()));
		}
	}
	
	public void addVotes(GraphiteVoteSource source, GraphiteUser user, int votes) {
		int money = votes * 5;
		Graphite.getEconomy().addMoney(user, money);
		
		if(voteChannel != null && user.isAvailable()) {
			voteChannel.sendMessage(new EmbedBuilder()
					.setDescription(String.format("%s has upvoted the bot on [%s](%s) and got `%s`" + JDAEmote.DOLLARONEN.getUnicode(),
							user.getName(),
							source.getName(),
							source.getUpvoteURL(source.getBot()),
							money))
					.setAuthor(user.getName(), null, user.getJDAUser().getAvatarUrl())
					.build());
		}
	}
	
	public GraphiteVoteSource getVoteSource(MultiplexBot bot, String id) {
		return bot.getVoteSources().stream()
				.filter(vs -> vs.getIdentifier().equals(id))
				.findFirst().orElse(null);
	}

}

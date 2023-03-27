package me.eglp.gv2.util.apis.discordbotlistcom;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.multiplex.bot.MultiplexBot;
import me.eglp.gv2.util.base.user.GraphiteUser;
import me.eglp.gv2.util.voting.GraphiteVoteSource;
import me.eglp.gv2.util.voting.InvalidVoteException;
import me.mrletsplay.mrcore.json.JSONObject;

public class DiscordBotListComVoteSource implements GraphiteVoteSource {
	
	private MultiplexBot bot;
	private String voteSecret;
	
	public DiscordBotListComVoteSource(MultiplexBot bot, String voteSecret) {
		this.bot = bot;
		this.voteSecret = voteSecret;
	}
	
	@Override
	public MultiplexBot getBot() {
		return bot;
	}
	
	@Override
	public String getName() {
		return "discordbotlist.com";
	}
	
	@Override
	public String getIdentifier() {
		return "discordbotlistcom";
	}
	
	@Override
	public String getUpvoteURL(MultiplexBot bot) {
		return "https://discordbotlist.com/bots/" + bot.getID();
	}
	
	@Override
	public void onVote(JSONObject requestData) {
		if(!requestData.getString("vote_secret").equals(voteSecret)) throw new InvalidVoteException("Invalid vote");
		String userID = requestData.getString("userID");
		GraphiteUser user = Graphite.getGlobalUser(userID); // because premium features are available for Multiplex bots as well
		if(user == null) throw new InvalidVoteException("Invalid user");

		Graphite.getVoting().addVotes(this, user, 2);
	}

}

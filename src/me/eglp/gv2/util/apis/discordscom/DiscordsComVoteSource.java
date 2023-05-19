package me.eglp.gv2.util.apis.discordscom;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.multiplex.bot.MultiplexBot;
import me.eglp.gv2.user.GraphiteUser;
import me.eglp.gv2.util.voting.GraphiteVoteSource;
import me.eglp.gv2.util.voting.InvalidVoteException;
import me.mrletsplay.mrcore.json.JSONObject;

public class DiscordsComVoteSource implements GraphiteVoteSource {

	private MultiplexBot bot;
	private String voteSecret;

	public DiscordsComVoteSource(MultiplexBot bot, String voteSecret) {
		this.bot = bot;
		this.voteSecret = voteSecret;
	}

	@Override
	public MultiplexBot getBot() {
		return bot;
	}

	@Override
	public String getName() {
		return "discords.com";
	}

	@Override
	public String getIdentifier() {
		return "discordscom";
	}

	@Override
	public String getUpvoteURL(MultiplexBot bot) {
		return "https://discords.com/bots/bot/" + bot.getID();
	}

	@Override
	public void onVote(JSONObject requestData) {
		if(!requestData.getString("vote_secret").equals(voteSecret)) throw new InvalidVoteException("Invalid vote");
		String userID = requestData.getString("userID");
		String type = requestData.getString("type");
		GraphiteUser user = Graphite.getGlobalUser(userID); // because premium features are available for Multiplex bots as well
		if(user == null) throw new InvalidVoteException("Invalid user");

		if("vote".equals(type)) {
			Graphite.getVoting().addVotes(this, user, 2);
		}else {
			Graphite.log("Test Vote @ DiscordsCom: " + requestData.toString());
		}
	}

}

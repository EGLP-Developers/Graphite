package me.eglp.gv2.util.apis.discordbotlistcom;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.user.GraphiteUser;
import me.eglp.gv2.util.voting.GraphiteVoteSource;
import me.eglp.gv2.util.voting.InvalidVoteException;
import me.mrletsplay.mrcore.json.JSONObject;

public class DiscordBotListComVoteSource implements GraphiteVoteSource {

	private String voteSecret;

	public DiscordBotListComVoteSource(String voteSecret) {
		this.voteSecret = voteSecret;
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
	public String getUpvoteURL() {
		return "https://discordbotlist.com/bots/" + Graphite.getBotID();
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

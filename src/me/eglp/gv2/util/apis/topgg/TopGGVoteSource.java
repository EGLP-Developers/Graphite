package me.eglp.gv2.util.apis.topgg;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.user.GraphiteUser;
import me.eglp.gv2.util.voting.GraphiteVoteSource;
import me.eglp.gv2.util.voting.InvalidVoteException;
import me.mrletsplay.mrcore.json.JSONObject;

public class TopGGVoteSource implements GraphiteVoteSource {

	private String voteSecret;

	public TopGGVoteSource(String voteSecret) {
		this.voteSecret = voteSecret;
	}

	@Override
	public void onVote(JSONObject requestData) {
		if(!requestData.getString("vote_secret").equals(voteSecret)) throw new InvalidVoteException("Invalid vote");
		String userID = requestData.getString("userID");
		boolean isWeekend = requestData.getBoolean("isWeekend");
		boolean isReal = requestData.getBoolean("real");
		GraphiteUser user = Graphite.getGlobalUser(userID); // because premium features are available for Multiplex bots as well
		if(user == null) throw new InvalidVoteException("Invalid user");

		if(isReal) {
			Graphite.getVoting().addVotes(this, user, isWeekend ? 2 : 1);
		}else {
			Graphite.log("Test Vote @ TopGG: " + requestData.toString());
		}
	}

	@Override
	public String getName() {
		return "top.gg";
	}

	@Override
	public String getIdentifier() {
		return "topgg";
	}

	@Override
	public String getUpvoteURL() {
		return "https://top.gg/bot/" + Graphite.getBotID() + "/vote";
	}

}

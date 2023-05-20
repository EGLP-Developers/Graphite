package me.eglp.gv2.util.voting;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.user.GraphiteUser;
import me.mrletsplay.mrcore.json.JSONObject;

public class BetaVoteSource implements GraphiteVoteSource {

	public BetaVoteSource() {}

	@Override
	public String getName() {
		return "BetaVote(TM)";
	}

	@Override
	public String getIdentifier() {
		return "beta";
	}

	@Override
	public String getUpvoteURL() {
		return "http://" + Graphite.getBotInfo().getWebsite().getBaseURL() + "/api/vote/beta?bot=" + getIdentifier() + "&user=";
	}

	@Override
	public void onVote(JSONObject requestData) {
		String userID = requestData.getString("userID");
		GraphiteUser user = Graphite.getUser(userID);
		if(user == null) throw new InvalidVoteException("Invalid user");

		Graphite.getVoting().addVotes(this, user, 0);
	}

}

package me.eglp.gv2.util.voting;

import me.mrletsplay.mrcore.json.JSONObject;

public interface GraphiteVoteSource {

	public String getName();

	public String getIdentifier();

	public String getUpvoteURL();

	public void onVote(JSONObject requestData);

}

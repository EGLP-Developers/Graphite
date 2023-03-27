package me.eglp.gv2.util.voting;

import me.eglp.gv2.multiplex.bot.MultiplexBot;
import me.mrletsplay.mrcore.json.JSONObject;

public interface GraphiteVoteSource {
	
	public MultiplexBot getBot();

	public String getName();
	
	public String getIdentifier();
	
	public String getUpvoteURL(MultiplexBot bot);
	
	public void onVote(JSONObject requestData);
	
}

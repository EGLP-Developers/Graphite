package me.eglp.gv2.util.apis.discordscom;

import java.net.UnknownHostException;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.multiplex.bot.MultiplexBot;
import me.eglp.gv2.util.apis.GraphiteStatisticsCollector;
import me.mrletsplay.mrcore.http.HttpRequest;
import me.mrletsplay.mrcore.http.data.JSONObjectData;
import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.mrcore.misc.FriendlyException;

public class DiscordsComStatistics implements GraphiteStatisticsCollector {

	private MultiplexBot bot;
	private String token;
	
	public DiscordsComStatistics(MultiplexBot bot, String token) {
		this.bot = bot;
		this.token = token;
	}

	@Override
	public void sendStatistics() {
		JSONObject stats = new JSONObject();
		stats.put("server_count", Graphite.getGuildCount());
		
		try {
			HttpRequest.createGeneric("POST", "https://discords.com/bots/api/bot/" + bot.getID())
				.setHeader("Authorization", token)
				.setHeader("Content-Type", "application/json")
				.setData(JSONObjectData.of(stats))
				.execute();
		}catch(FriendlyException e) {
			if(e.getCause() instanceof UnknownHostException) {
				Graphite.log("discords.com is unreachable");
			}
		}
	}

}

package me.eglp.gv2.util.apis.discordbotlistcom;

import java.net.UnknownHostException;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.apis.GraphiteStatisticsCollector;
import me.mrletsplay.mrcore.http.HttpRequest;
import me.mrletsplay.mrcore.http.data.URLEncodedData;
import me.mrletsplay.mrcore.misc.FriendlyException;

public class DiscordBotListComStatistics implements GraphiteStatisticsCollector {

	private String token;

	public DiscordBotListComStatistics(String token) {
		this.token = token;
	}

	@Override
	public void sendStatistics() {
		try {
			HttpRequest.createPost("https://discordbotlist.com/api/bots/" + Graphite.getBotID() + "/stats")
				.setHeader("Authorization", token)
				.setHeader("Content-Type", "application/json")
				.setData(new URLEncodedData().set("guilds", ""+Graphite.getGuildCount()))
				.execute();
		}catch(FriendlyException e) {
			if(e.getCause() instanceof UnknownHostException) {
				Graphite.log("discordbotlist.com is unreachable");
			}
		}
	}

}

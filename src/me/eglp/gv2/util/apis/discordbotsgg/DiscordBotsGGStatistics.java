package me.eglp.gv2.util.apis.discordbotsgg;

import java.net.UnknownHostException;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.apis.GraphiteStatisticsCollector;
import me.mrletsplay.mrcore.http.HttpRequest;
import me.mrletsplay.mrcore.http.data.JSONObjectData;
import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.mrcore.misc.FriendlyException;

public class DiscordBotsGGStatistics implements GraphiteStatisticsCollector {

	private String token;

	public DiscordBotsGGStatistics(String token) {
		this.token = token;
	}

	@Override
	public void sendStatistics() {
		JSONObject json = new JSONObject();
		json.put("guildCount", Graphite.getGuildCount());

		try {
			HttpRequest.createGeneric("POST", "https://discord.bots.gg/api/v1/bots/" + Graphite.getBotID() + "/stats")
				.setHeader("Authorization", token)
				.setHeader("Content-Type", "application/json")
				.setData(JSONObjectData.of(json))
				.execute();
		}catch(FriendlyException e) {
			if(e.getCause() instanceof UnknownHostException) {
				Graphite.log("discord.bots.gg is unreachable");
			}
		}
	}

}

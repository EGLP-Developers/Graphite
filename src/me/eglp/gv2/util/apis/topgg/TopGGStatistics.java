package me.eglp.gv2.util.apis.topgg;

import org.discordbots.api.client.DiscordBotListAPI;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.multiplex.bot.MultiplexBot;
import me.eglp.gv2.util.apis.GraphiteStatisticsCollector;

public class TopGGStatistics implements GraphiteStatisticsCollector {
	
	private DiscordBotListAPI dblAPI;

	public TopGGStatistics(MultiplexBot bot, String token) {
		dblAPI = new DiscordBotListAPI.Builder()
				.token(token)
				.botId(bot.getID())
				.build();
	}
	
	@Override
	public void sendStatistics() {
		dblAPI.setStats(Graphite.getGuildCount());
	}
	
}

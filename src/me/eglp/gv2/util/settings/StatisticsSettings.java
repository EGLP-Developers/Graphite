package me.eglp.gv2.util.settings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import me.eglp.gv2.multiplex.bot.MultiplexBot;
import me.eglp.gv2.util.apis.GraphiteStatisticsCollector;
import me.eglp.gv2.util.apis.discordbotlistcom.DiscordBotListComStatistics;
import me.eglp.gv2.util.apis.discordbotsgg.DiscordBotsGGStatistics;
import me.eglp.gv2.util.apis.discordscom.DiscordsComStatistics;
import me.eglp.gv2.util.apis.topgg.TopGGStatistics;
import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONConvertible;
import me.mrletsplay.mrcore.json.converter.JSONValue;

public class StatisticsSettings implements JSONConvertible {
	
	@JSONValue
	private boolean enable;
	
	@JSONValue("top.gg")
	private String topggToken;
	
	@JSONValue("discord.bots.gg")
	private String discordbotsggToken;
	
	@JSONValue("discords.com")
	private String discordscomToken;
	
	@JSONValue("discordbotlist.com")
	private String discordbotlistcomToken;
	
	@JSONConstructor
	private StatisticsSettings() {}
	
	public boolean isEnabled() {
		return enable;
	}

	public String getTopggToken() {
		return topggToken;
	}
	
	public String getDiscordbotsggToken() {
		return discordbotsggToken;
	}

	public String getDiscordscomToken() {
		return discordscomToken;
	}

	public String getDiscordbotlistcomToken() {
		return discordbotlistcomToken;
	}
	
	public List<Function<MultiplexBot, GraphiteStatisticsCollector>> getStatisticsCollectors() {
		if(!enable) return Collections.emptyList();
		
		List<Function<MultiplexBot, GraphiteStatisticsCollector>> c = new ArrayList<>();
		
		if(topggToken != null) {
			c.add(bot -> new TopGGStatistics(bot, topggToken));
		}
		
		if(discordbotsggToken != null) {
			c.add(bot -> new DiscordBotsGGStatistics(bot, topggToken));
		}
		
		if(discordscomToken != null) {
			c.add(bot -> new DiscordsComStatistics(bot, topggToken));
		}
		
		if(discordbotlistcomToken != null) {
			c.add(bot -> new DiscordBotListComStatistics(bot, topggToken));
		}
		
		return c;
	}
	
	public static StatisticsSettings createDefault() {
		StatisticsSettings s = new StatisticsSettings();
		s.enable = false;
		s.topggToken = "top.gg token";
		s.discordbotsggToken = "discord.bots.gg token";
		s.discordscomToken = "discords.com token";
		s.discordbotlistcomToken = "discordbotlist.com token";
		return s;
	}

}

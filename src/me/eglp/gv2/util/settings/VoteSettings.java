package me.eglp.gv2.util.settings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import me.eglp.gv2.multiplex.bot.MultiplexBot;
import me.eglp.gv2.util.apis.discordbotlistcom.DiscordBotListComVoteSource;
import me.eglp.gv2.util.apis.discordscom.DiscordsComVoteSource;
import me.eglp.gv2.util.apis.topgg.TopGGVoteSource;
import me.eglp.gv2.util.voting.BetaVoteSource;
import me.eglp.gv2.util.voting.GraphiteVoteSource;
import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONConvertible;
import me.mrletsplay.mrcore.json.converter.JSONValue;

public class VoteSettings implements JSONConvertible {
	
	@JSONValue
	private boolean enable;
	
	@JSONValue("top.gg")
	private String topggVoteSecret;
	
	@JSONValue("discords.com")
	private String discordscomVoteSecret;
	
	@JSONValue("discordbotlist.com")
	private String discordbotlistcomVoteSecret;
	
	@JSONValue
	private boolean enableBetaVoteSource;
	
	@JSONConstructor
	private VoteSettings() {}
	
	public boolean isEnabled() {
		return enable;
	}

	public String getTopggVoteSecret() {
		return topggVoteSecret;
	}

	public String getDiscordscomVoteSecret() {
		return discordscomVoteSecret;
	}

	public String getDiscordbotlistcomVoteSecret() {
		return discordbotlistcomVoteSecret;
	}
	
	public List<Function<MultiplexBot, GraphiteVoteSource>> getVoteSources() {
		if(enable) return Collections.emptyList();
		
		List<Function<MultiplexBot, GraphiteVoteSource>> c = new ArrayList<>();
		
		if(topggVoteSecret != null) {
			c.add(bot -> new TopGGVoteSource(bot, topggVoteSecret));
		}
		
		if(discordscomVoteSecret != null) {
			c.add(bot -> new DiscordsComVoteSource(bot, discordscomVoteSecret));
		}
		
		if(discordbotlistcomVoteSecret != null) {
			c.add(bot -> new DiscordBotListComVoteSource(bot, topggVoteSecret));
		}
		
		if(enableBetaVoteSource) {
			c.add(bot -> new BetaVoteSource(bot));
		}
		
		return c;
	}
	
	public static VoteSettings createDefault() {
		VoteSettings s = new VoteSettings();
		s.enable = false;
		s.topggVoteSecret = "top.gg secret";
		s.discordscomVoteSecret = "discords.com secret";
		s.discordbotlistcomVoteSecret = "discordbotlist.com secret";
		s.enableBetaVoteSource = false;
		return s;
	}
	
}

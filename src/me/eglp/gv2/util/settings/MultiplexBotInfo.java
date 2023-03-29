package me.eglp.gv2.util.settings;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import me.eglp.gv2.multiplex.GraphiteFeature;
import me.eglp.gv2.multiplex.bot.MultiplexBot;
import me.eglp.gv2.util.apis.GraphiteStatisticsCollector;
import me.eglp.gv2.util.voting.GraphiteVoteSource;
import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONConvertible;
import me.mrletsplay.mrcore.json.converter.JSONValue;

public class MultiplexBotInfo implements JSONConvertible {

	@JSONValue
	private int hierarchyIndex;
	
	@JSONValue
	private String
		identifier,
		name,
		defaultPrefix,
		token,
		clientSecret;
	
	@JSONValue
	private int shardCount;
	
	@JSONValue
	private GraphiteFeature[] features;
	
	@JSONValue
	private StatisticsSettings statisticsSettings;
	
	@JSONValue
	private VoteSettings voteSettings;
	
	@JSONConstructor
	protected MultiplexBotInfo() {}
	
	public MultiplexBotInfo(int hierarchyIndex, String identifier, String name, String defaultPrefix, String token, String clientSecret, int numShards, StatisticsSettings statisticsSettings, VoteSettings voteSettings, GraphiteFeature... features) {
		this.hierarchyIndex = hierarchyIndex;
		this.identifier = identifier;
		this.name = name;
		this.defaultPrefix = defaultPrefix;
		this.token = token;
		this.clientSecret = clientSecret;
		this.shardCount = numShards;
		this.statisticsSettings = statisticsSettings;
		this.voteSettings = voteSettings;
		this.features = features;
	}
	
	public MultiplexBotInfo(int hierarchyIndex, String identifier, String name, String defaultPrefix, String token, String clientSecret, int numShards, GraphiteFeature... features) {
		this(hierarchyIndex,identifier, name, defaultPrefix, token, clientSecret, numShards, null, null, features);
	}
	
	public int getHierarchyIndex() {
		return hierarchyIndex;
	}
	
	public String getIdentifier() {
		return identifier;
	}
	
	public String getName() {
		return name;
	}
	
	public String getDefaultPrefix() {
		return defaultPrefix;
	}
	
	public String getToken() {
		return token;
	}
	
	public String getClientSecret() {
		return clientSecret;
	}
	
	public int getShardCount() {
		return shardCount;
	}
	
	public List<Function<MultiplexBot, GraphiteStatisticsCollector>> getStatisticsCollectors() {
		return statisticsSettings != null ? statisticsSettings.getStatisticsCollectors() : Collections.emptyList();
	}
	
	public List<Function<MultiplexBot, GraphiteVoteSource>> getVoteSources() {
		return voteSettings != null ? voteSettings.getVoteSources() : Collections.emptyList();
	}
	
	public List<GraphiteFeature> getFeatures() {
		return Arrays.asList(features);
	}
	
	public boolean hasFeaturesAvailable(List<GraphiteFeature> features) {
		return features.stream().allMatch(f -> isFeatureEnabled(f));
	}
	
	public boolean hasFeaturesAvailable(GraphiteFeature... features) {
		return hasFeaturesAvailable(Arrays.asList(features));
	}
	
	public boolean isFeatureEnabled(GraphiteFeature feature) {
		return getFeatures().contains(feature);
	}
	
	public boolean isMainBot() {
		return this instanceof MainBotInfo;
	}
	
	public void validate(List<String> errors) {
		if(identifier == null) errors.add("Bot identifier is null");
		if(name == null) errors.add("Name for bot is null");
		if(token == null) errors.add("Token for bot is nul");
		if(clientSecret == null) errors.add("Client secret for bot is null");
		if(shardCount <= 0) errors.add("Shard count must be greater than zero");
	}
	
	public static MultiplexBotInfo createDefault() {
		return new MultiplexBotInfo(1, "mymultiplexbot", "My Multiplex Bot", "mmp-", "TOKEN", "SECRET", 1, StatisticsSettings.createDefault(), VoteSettings.createDefault(), GraphiteFeature.DEFAULT, GraphiteFeature.BACKUPS);
	}
	
}

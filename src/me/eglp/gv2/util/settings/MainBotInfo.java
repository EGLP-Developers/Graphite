package me.eglp.gv2.util.settings;

import java.util.List;

import me.eglp.gv2.multiplex.GraphiteFeature;
import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONValue;

public class MainBotInfo extends MultiplexBotInfo {
	
	@JSONValue
	private String fileLocation;
	
	@JSONValue
	private boolean isBeta;
	
	@JSONValue
	private MultiplexBotInfo[] multiplexBots;
	
	@JSONValue
	private MySQLSettings mySQL;
	
	@JSONValue
	private PatreonSettings patreon;
	
	@JSONValue
	private TwitchSettings twitch;
	
	@JSONValue
	private GeniusSettings genius;
	
	@JSONValue
	private RedditSettings reddit;
	
	@JSONValue
	private TwitterSettings twitter;
	
	@JSONValue
	private WebsiteSettings website;
	
	@JSONValue
	private LinksSettings links;
	
	@JSONValue
	private AmongUsSettings amongUs;
	
	@JSONValue
	private SpotifySettings spotify;
	
	@JSONValue
	private MiscellaneousSettings miscellaneous;
	
	@JSONConstructor
	private MainBotInfo() {}
	
	public MainBotInfo(String identifier, String name, String fileLocation, String defaultPrefix, String token, String clientSecret, boolean isBeta, int numShards, StatisticsSettings statisticsSettings, VoteSettings voteSettings, MySQLSettings mySQL, PatreonSettings patreon, TwitchSettings twitch, GeniusSettings genius, RedditSettings reddit, TwitterSettings twitter, WebsiteSettings website, LinksSettings links, AmongUsSettings amongUs, SpotifySettings spotify, MiscellaneousSettings miscellaneous, MultiplexBotInfo... multiplexBots) {
		super(0, identifier, name, defaultPrefix, token, clientSecret, numShards, statisticsSettings, voteSettings, GraphiteFeature.values());
		this.fileLocation = fileLocation;
		this.isBeta = isBeta;
		this.multiplexBots = multiplexBots;
		this.mySQL = mySQL;
		this.patreon = patreon;
		this.twitch = twitch;
		this.genius = genius;
		this.reddit = reddit;
		this.twitter = twitter;
		this.website = website;
		this.links = links;
		this.amongUs = amongUs;
		this.spotify = spotify;
		this.miscellaneous = miscellaneous;
	}
	
	public String getFileLocation() {
		return fileLocation;
	}
	
	public boolean isBeta() {
		return isBeta;
	}
	
	public MultiplexBotInfo[] getMultiplexBots() {
		return multiplexBots;
	}
	
	public MySQLSettings getMySQL() {
		return mySQL;
	}
	
	public PatreonSettings getPatreon() {
		return patreon;
	}
	
	public TwitchSettings getTwitch() {
		return twitch;
	}
	
	public GeniusSettings getGenius() {
		return genius;
	}
	
	public RedditSettings getReddit() {
		return reddit;
	}
	
	public TwitterSettings getTwitter() {
		return twitter;
	}
	
	public WebsiteSettings getWebsite() {
		return website;
	}
	
	public LinksSettings getLinks() {
		return links;
	}
	
	public AmongUsSettings getAmongUs() {
		return amongUs;
	}
	
	public SpotifySettings getSpotify() {
		return spotify;
	}
	
	public MiscellaneousSettings getMiscellaneous() {
		return miscellaneous;
	}
	
	@Override
	public void validate(List<String> errors) {
		super.validate(errors);
		if(fileLocation == null) errors.add("File location is null");
		if(mySQL == null) errors.add("MySQL config is missing");
		
		if(isFeatureEnabled(GraphiteFeature.TWITCH) && twitch == null) errors.add("Twitch feature is enabled, but not configured");
		if(twitch != null) twitch.validate(errors);
		
		if(isFeatureEnabled(GraphiteFeature.MUSIC) && genius == null) errors.add("Music feature is enabled, but Genius config is missing");
		if(genius != null) genius.validate(errors);
		
		if(isFeatureEnabled(GraphiteFeature.REDDIT) && reddit == null) errors.add("Twitch feature is enabled, but not configured");
		if(reddit != null) reddit.validate(errors);
		
		if(isFeatureEnabled(GraphiteFeature.TWITTER) && twitter == null) errors.add("Twitch feature is enabled, but not configured");
		if(twitter != null) twitter.validate(errors);
		
		if(website == null) errors.add("Website config is missing");
		if(links == null) errors.add("Links config is missing");
		if(isFeatureEnabled(GraphiteFeature.FUN) && amongUs == null) errors.add("Fun feature is enabled, but AmongUs config is missing");
		if(isFeatureEnabled(GraphiteFeature.MUSIC) && spotify == null) errors.add("Music feature is enabled, but Spotify config is missing");
		if(miscellaneous == null) errors.add("Miscellaneous config is missing");
	}
	
	public static MainBotInfo createDefault() {
		return new MainBotInfo(
				"mybot",
				"My Bot",
				"MyBot",
				"bot-",
				"TOKEN",
				"SECRET",
				false,
				1,
				StatisticsSettings.createDefault(),
				VoteSettings.createDefault(),
				MySQLSettings.createDefault(),
				PatreonSettings.createDefault(),
				TwitchSettings.createDefault(),
				GeniusSettings.createDefault(),
				RedditSettings.createDefault(),
				TwitterSettings.createDefault(),
				WebsiteSettings.createDefault(),
				LinksSettings.createDefault(),
				AmongUsSettings.createDefault(),
				SpotifySettings.createDefault(),
				MiscellaneousSettings.createDefault(),
				MultiplexBotInfo.createDefault());
	}

}

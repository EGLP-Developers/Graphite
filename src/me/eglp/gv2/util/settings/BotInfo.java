package me.eglp.gv2.util.settings;

import java.util.List;

import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONValue;

public class BotInfo {

	@JSONValue
	private String
		identifier,
		name,
		defaultPrefix,
		token,
		clientSecret,
		fileLocation;

	@JSONValue
	private int shardCount;

	@JSONValue
	private boolean isBeta;

	@JSONValue
	private StatisticsSettings statisticsSettings;

	@JSONValue
	private VoteSettings voteSettings;

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
	private BotInfo() {}

	public BotInfo(String identifier, String name, String defaultPrefix, String token, String clientSecret, String fileLocation, int shardCount, boolean isBeta, StatisticsSettings statisticsSettings, VoteSettings voteSettings, MySQLSettings mySQL, PatreonSettings patreon, TwitchSettings twitch, GeniusSettings genius, RedditSettings reddit, TwitterSettings twitter, WebsiteSettings website, LinksSettings links, AmongUsSettings amongUs, SpotifySettings spotify, MiscellaneousSettings miscellaneous) {
		this.identifier = identifier;
		this.name = name;
		this.defaultPrefix = defaultPrefix;
		this.token = token;
		this.clientSecret = clientSecret;
		this.fileLocation = fileLocation;
		this.shardCount = shardCount;
		this.isBeta = isBeta;
		this.statisticsSettings = statisticsSettings;
		this.voteSettings = voteSettings;
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

	public void validate(List<String> errors) {
		if(fileLocation == null) errors.add("File location is null");
		if(mySQL == null) errors.add("MySQL config is missing");

		if(twitch == null) errors.add("Twitch not configured");
		if(twitch != null) twitch.validate(errors);

		if(genius == null) errors.add("Genius config is missing");
		if(genius != null) genius.validate(errors);

		if(reddit == null) errors.add("Reddit not configured");
		if(reddit != null) reddit.validate(errors);

		if(twitter == null) errors.add("Twitter not configured");
		if(twitter != null) twitter.validate(errors);

		if(website == null) errors.add("Website config is missing");
		if(links == null) errors.add("Links config is missing");
		if(amongUs == null) errors.add("AmongUs config is missing");
		if(spotify == null) errors.add("Spotify config is missing");
		if(miscellaneous == null) errors.add("Miscellaneous config is missing");
	}

	public static BotInfo createDefault() {
		return new BotInfo(
				"mybot",
				"My Bot",
				"bot-",
				"TOKEN",
				"SECRET",
				"mybot",
				1,
				false,
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
				MiscellaneousSettings.createDefault());
	}

}

package me.eglp.gv2.multiplex.bot;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import javax.security.auth.login.LoginException;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.main.GraphiteShard;
import me.eglp.gv2.multiplex.GraphiteMultiplex;
import me.eglp.gv2.multiplex.MultiplexCache;
import me.eglp.gv2.multiplex.MultiplexExecutor;
import me.eglp.gv2.util.apis.GraphiteStatisticsCollector;
import me.eglp.gv2.util.settings.MultiplexBotInfo;
import me.eglp.gv2.util.voting.GraphiteVoteSource;
import me.eglp.gv2.util.webinterface.js.JavaScriptGetter;
import me.eglp.gv2.util.webinterface.js.WebinterfaceObject;
import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.mrcore.misc.FriendlyException;
import me.mrletsplay.mrcore.misc.MiscUtils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDA.Status;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.ApplicationInfo;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

public class MultiplexBot implements WebinterfaceObject{

	protected MultiplexBotInfo botInfo;
	protected List<GraphiteShard> shards;
	private List<GraphiteStatisticsCollector> statisticsCollectors;
	private List<GraphiteVoteSource> voteSources;
	protected MultiplexCache cache;
	protected String description, avatarURL, invite;
	protected ApplicationInfo applicationInfo;
	
	protected MultiplexBot() {}
	
	public MultiplexBot(MultiplexBotInfo botInfo) {
		this.botInfo = botInfo;
		try {
			this.shards = createShards(botInfo);
		} catch (LoginException e) {
			throw new FriendlyException("Failed to create multiplex bot \"" + botInfo.getName() + "\"", e);
		}
		this.statisticsCollectors = new ArrayList<>();
		this.voteSources = new ArrayList<>();
		this.cache = new MultiplexCache(this);
		this.applicationInfo = shards.get(0).getJDA().retrieveApplicationInfo().complete();
		this.description = applicationInfo.getDescription();
		this.avatarURL = shards.get(0).getJDA().getSelfUser().getEffectiveAvatarUrl();
		this.invite = applicationInfo.getInviteUrl(Permission.ADMINISTRATOR);
	}
	
	private List<GraphiteShard> createShards(MultiplexBotInfo botInfo) throws LoginException {
		GraphiteMultiplex.setCurrentBot(this);
		List<GraphiteShard> shards = new ArrayList<>();
		MultiplexExecutor executor = new MultiplexExecutor(this);
		
		List<GatewayIntent> intents = new ArrayList<>();
		intents.add(GatewayIntent.GUILD_MEMBERS);
		intents.add(GatewayIntent.MESSAGE_CONTENT);
//		NONBETA Implement after discord enables intends
//		if(botInfo.getFeatures().contains(GraphiteFeature.STATISTICS)) intents.add(GatewayIntent.GUILD_PRESENCES);
		
		List<CacheFlag> flags = new ArrayList<>();
		flags.add(CacheFlag.VOICE_STATE);
//		if(botInfo.getFeatures().contains(GraphiteFeature.STATISTICS)) flags.add(CacheFlag.ONLINE_STATUS);
		
		JDABuilder builder = JDABuilder.createDefault(botInfo.getToken())
				.setStatus(OnlineStatus.ONLINE)
				.setActivity(Activity.listening("/help | " + botInfo.getName()))
				.addEventListeners(Graphite.getJDAListener())
				.setCallbackPool(executor, true)
				.setGatewayPool(executor, true)
				.setRateLimitPool(executor, true)
				.enableIntents(intents)
				.setChunkingFilter(ChunkingFilter.NONE)
				.setLargeThreshold(50)
				.setMemberCachePolicy(MemberCachePolicy.ALL)
				.enableCache(flags)
				.setAutoReconnect(true);
		
		for (int i = 0; i < botInfo.getShardCount(); i++) {
			Graphite.log("Creating shard: " + (i + 1) + "/" + botInfo.getShardCount());
			JDA jda = builder.useSharding(i, botInfo.getShardCount()).build();
			registerJDA(jda);
			shards.add(new GraphiteShard(i, jda));
		}
		
		return shards;
	}
	
	public void registerJDA(JDA jda) {
		GraphiteMultiplex.registerJDA(this, jda);
	}
	
	public void awaitLoad() {
		for(GraphiteShard shard : shards) {
			try {
				shard.getJDA().awaitReady();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		for(Function<MultiplexBot, GraphiteStatisticsCollector> s : botInfo.getStatisticsCollectors()) {
			statisticsCollectors.add(s.apply(this));
		}
		
		for(Function<MultiplexBot, GraphiteVoteSource> v : botInfo.getVoteSources()) {
			voteSources.add(v.apply(this));
		}
	}
	
	public void startStatisticsCollectors() {
		if(!statisticsCollectors.isEmpty()) {
			Graphite.getScheduler().scheduleAtFixedRate("send-statistics/" + botInfo.getIdentifier(), () -> {
				Graphite.withBot(this, () -> statisticsCollectors.forEach(s -> s.sendStatistics()));
			}, 10 * 60 * 1000);
		}
	}
	
	public void shutdown() {
		Graphite.log("Shutting down bot " + botInfo.getName());
		shards.forEach(s -> {
			s.getJDA().shutdown();
			while(!s.getJDA().getStatus().equals(Status.SHUTDOWN)) {
				Graphite.log("Awaiting shutdown for " + botInfo.getName());
				MiscUtils.runSafely(() -> Thread.sleep(100));
			}
		});
	}
	
	public boolean isOnline() {
		return shards != null && shards.stream().allMatch(s -> s.getJDA().getStatus().equals(Status.CONNECTED));
	}
	
	@JavaScriptGetter(name = "getIdentifier", returning = "identifier")
	public String getIdentifier() {
		return botInfo.getIdentifier();
	}
	
	public boolean isMainBot() {
		return botInfo.isMainBot();
	}
	
	public MultiplexBotInfo getBotInfo() {
		return botInfo;
	}
	
	public List<GraphiteShard> getShards() {
		return shards;
	}
	
	public List<GraphiteVoteSource> getVoteSources() {
		return voteSources;
	}
	
	public MultiplexCache getCache() {
		return cache;
	}
	
	@JavaScriptGetter(name = "getDescription", returning = "description")
	public String getDescription() {
		return description;
	}
	
	@JavaScriptGetter(name = "getAvatarURL", returning = "avatar_url")
	public String getAvatarURL() {
		return avatarURL;
	}
	
	@JavaScriptGetter(name = "getInvite", returning = "invite")
	public String getInvite() {
		return invite;
	}

	@JavaScriptGetter(name = "getID", returning = "id")
	public String getID() {
		return shards.get(0).getJDA().getSelfUser().getId();
	}

	@JavaScriptGetter(name = "getDefaultPrefix", returning = "prefix")
	public String getDefaultPrefix() {
		return getBotInfo().getDefaultPrefix();
	}

	@JavaScriptGetter(name = "getName", returning = "name")
	public String getName() {
		return getBotInfo().getName();
	}
	
	@Override
	public void preSerializeWI(JSONObject object) {
		object.put("identifier", getIdentifier());
		object.put("description", getDescription());
		object.put("avatar_url", getAvatarURL());
		object.put("invite", getInvite());
		object.put("id", getID());
		object.put("prefix", getDefaultPrefix());
		object.put("name", getName());
	}

}

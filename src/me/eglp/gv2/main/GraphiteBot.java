package me.eglp.gv2.main;

import java.util.ArrayList;
import java.util.List;

import javax.security.auth.login.LoginException;

import me.eglp.gv2.util.settings.BotInfo;
import me.mrletsplay.mrcore.misc.FriendlyException;
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

public class GraphiteBot {

	protected static BotInfo botInfo;
	protected static List<GraphiteShard> shards;
	protected static String description, avatarURL, invite;
	protected static ApplicationInfo applicationInfo;

	protected static void start(BotInfo info) {
		botInfo = info;

		try {
			shards = createShards(info);
		} catch (LoginException e) {
			throw new FriendlyException("Failed to create bot", e);
		}

		applicationInfo = shards.get(0).getJDA().retrieveApplicationInfo().complete();
		description = applicationInfo.getDescription();
		avatarURL = shards.get(0).getJDA().getSelfUser().getEffectiveAvatarUrl();
		invite = applicationInfo.getInviteUrl(Permission.ADMINISTRATOR);
	}

	private static List<GraphiteShard> createShards(BotInfo botInfo) throws LoginException {
		List<GraphiteShard> shards = new ArrayList<>();

		List<GatewayIntent> intents = new ArrayList<>();
		intents.add(GatewayIntent.GUILD_MEMBERS);
		intents.add(GatewayIntent.MESSAGE_CONTENT);

		List<CacheFlag> flags = new ArrayList<>();
		flags.add(CacheFlag.VOICE_STATE);

		JDABuilder builder = JDABuilder.createDefault(botInfo.getToken())
				.setStatus(OnlineStatus.ONLINE)
				.setActivity(Activity.listening("/help | " + botInfo.getName()))
				.addEventListeners(Graphite.getJDAListener())
				.enableIntents(intents)
				.setChunkingFilter(ChunkingFilter.NONE)
				.setLargeThreshold(50)
				.setMemberCachePolicy(MemberCachePolicy.ALL)
				.enableCache(flags)
				.setAutoReconnect(true);

		for (int i = 0; i < botInfo.getShardCount(); i++) {
			Graphite.log("Creating shard: " + (i + 1) + "/" + botInfo.getShardCount());
			JDA jda = builder.useSharding(i, botInfo.getShardCount()).build();
			shards.add(new GraphiteShard(i, jda));
		}

		return shards;
	}

	public static void awaitLoad() {
		for(GraphiteShard shard : shards) {
			try {
				shard.getJDA().awaitReady();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public static void startStatisticsCollectors() {
		var statisticsCollectors = botInfo.getStatisticsSettings().getStatisticsCollectors();

		if(!statisticsCollectors.isEmpty()) {
			Graphite.getScheduler().scheduleAtFixedRate("send-statistics/" + botInfo.getIdentifier(), () -> {
				statisticsCollectors.forEach(s -> s.sendStatistics());
			}, 10 * 60 * 1000);
		}
	}

	public static void shutdown() {
		Graphite.log("Shutting down bot " + botInfo.getName());
		shards.forEach(s -> {
			s.getJDA().shutdown();

			while(!s.getJDA().getStatus().equals(Status.SHUTDOWN)) {
				Graphite.log("Awaiting shutdown for " + botInfo.getName());

				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
	}

}

package me.eglp.gv2.main;

import java.util.ArrayList;
import java.util.List;

import javax.security.auth.login.LoginException;

import me.eglp.gv2.commands.admin.CommandLocale;
import me.eglp.gv2.commands.admin.CommandModule;
import me.eglp.gv2.commands.admin.CommandPermission;
import me.eglp.gv2.commands.admin.CommandPrefix;
import me.eglp.gv2.commands.admin.CommandPurge;
import me.eglp.gv2.commands.admin.CommandTextCommands;
import me.eglp.gv2.commands.admin.CommandTimezone;
import me.eglp.gv2.commands.admin.CommandUpdateSlashCommands;
import me.eglp.gv2.commands.backups.CommandBackup;
import me.eglp.gv2.commands.channel_management.CommandAutoChannel;
import me.eglp.gv2.commands.channel_management.CommandCategoryCopy;
import me.eglp.gv2.commands.channel_management.CommandUserChannel;
import me.eglp.gv2.commands.fun.CommandAmongUs;
import me.eglp.gv2.commands.fun.CommandChuckNorris;
import me.eglp.gv2.commands.fun.CommandCoinFlip;
import me.eglp.gv2.commands.fun.CommandDice;
import me.eglp.gv2.commands.fun.CommandEightBall;
import me.eglp.gv2.commands.fun.CommandMeme;
import me.eglp.gv2.commands.fun.CommandMinigame;
import me.eglp.gv2.commands.fun.CommandPoll;
import me.eglp.gv2.commands.fun.CommandReminder;
import me.eglp.gv2.commands.fun.CommandTCRandom;
import me.eglp.gv2.commands.fun.CommandTTS;
import me.eglp.gv2.commands.fun.CommandVCRandom;
import me.eglp.gv2.commands.greeter.CommandFarewell;
import me.eglp.gv2.commands.greeter.CommandGreeting;
import me.eglp.gv2.commands.info.CommandAbout;
import me.eglp.gv2.commands.info.CommandChannelInfo;
import me.eglp.gv2.commands.info.CommandEasterEggs;
import me.eglp.gv2.commands.info.CommandEmoteInfo;
import me.eglp.gv2.commands.info.CommandFAQ;
import me.eglp.gv2.commands.info.CommandHelp;
import me.eglp.gv2.commands.info.CommandInvite;
import me.eglp.gv2.commands.info.CommandUpvote;
import me.eglp.gv2.commands.info.CommandUserInfo;
import me.eglp.gv2.commands.info.CommandWhatAreYouDoing;
import me.eglp.gv2.commands.moderation.CommandChatMute;
import me.eglp.gv2.commands.moderation.CommandChatReport;
import me.eglp.gv2.commands.moderation.CommandChatReports;
import me.eglp.gv2.commands.moderation.CommandChatUnmute;
import me.eglp.gv2.commands.moderation.CommandClear;
import me.eglp.gv2.commands.moderation.CommandClearAll;
import me.eglp.gv2.commands.moderation.CommandJail;
import me.eglp.gv2.commands.moderation.CommandModRole;
import me.eglp.gv2.commands.moderation.CommandReport;
import me.eglp.gv2.commands.moderation.CommandReports;
import me.eglp.gv2.commands.moderation.CommandSupport;
import me.eglp.gv2.commands.moderation.CommandTempBan;
import me.eglp.gv2.commands.moderation.CommandTempChatMute;
import me.eglp.gv2.commands.moderation.CommandTempJail;
import me.eglp.gv2.commands.moderation.CommandTempVoiceMute;
import me.eglp.gv2.commands.moderation.CommandUnban;
import me.eglp.gv2.commands.moderation.CommandUnjail;
import me.eglp.gv2.commands.moderation.CommandVoiceUnmute;
import me.eglp.gv2.commands.music.CommandMusic;
import me.eglp.gv2.commands.record.CommandRecord;
import me.eglp.gv2.commands.reddit.CommandReddit;
import me.eglp.gv2.commands.role_management.CommandAccessrole;
import me.eglp.gv2.commands.role_management.CommandAutorole;
import me.eglp.gv2.commands.role_management.CommandBotrole;
import me.eglp.gv2.commands.role_management.CommandGetrole;
import me.eglp.gv2.commands.scripting.CommandScript;
import me.eglp.gv2.commands.test.CommandTest;
import me.eglp.gv2.commands.twitch.CommandTwitch;
import me.eglp.gv2.commands.twitter.CommandTwitter;
import me.eglp.gv2.util.command.Command;
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

	public static List<Command> getConfiguredCommands() {
		List<Command> cmds = new ArrayList<>();

		cmds.addAll(List.of(
			new CommandHelp(),
			new CommandAbout(),
			new CommandPermission(),
			new CommandInvite(),
			new CommandPrefix(),
			new CommandLocale(),
			new CommandUpvote(),
			new CommandWhatAreYouDoing(),
			new CommandPurge(),
			new CommandTest(),
			new CommandTextCommands(),
			new CommandUpdateSlashCommands(),
			new CommandTimezone(),
			new CommandBackup(),
			new CommandEightBall(),
			new CommandChuckNorris(),
			new CommandMinigame(),
			new CommandMeme(),
			new CommandCoinFlip(),
			new CommandDice(),
			new CommandVCRandom(),
			new CommandTCRandom(),
			new CommandPoll(),
			new CommandTTS(),
			new CommandReminder(),
			new CommandMusic(),
			new CommandChatReports(),
			new CommandChatReport(),
			new CommandTempVoiceMute(),
			new CommandClearAll(),
			new CommandTempJail(),
			new CommandTempBan(),
			new CommandSupport(),
			new CommandReports(),
			new CommandUnjail(),
			new CommandVoiceUnmute(),
			new CommandUnban(),
			new CommandReport(),
			new CommandClear(),
			new CommandJail(),
			new CommandCategoryCopy(),
			new CommandModRole(),
			new CommandChatMute(),
			new CommandChatUnmute(),
			new CommandTempChatMute(),
			new CommandFarewell(),
			new CommandGreeting(),
			new CommandAccessrole(),
			new CommandAutorole(),
			new CommandGetrole(),
			new CommandBotrole(),
			new CommandAutoChannel(),
			new CommandUserChannel(),
			new CommandChannelInfo(),
			new CommandFAQ(),
			new CommandUserInfo(),
			new CommandEmoteInfo(),
			new CommandEasterEggs(),
			new CommandRecord(),
			new CommandScript(),
			new CommandModule()
		));

		if(Graphite.getBotInfo().getAmongUs().isEnabled()) {
			cmds.add(new CommandAmongUs());
		}

		if(Graphite.getBotInfo().getTwitch().isEnabled()) {
			cmds.add(new CommandTwitch());
		}

		if(Graphite.getBotInfo().getTwitter().isEnabled()) {
			cmds.add(new CommandTwitter());
		}

		if(Graphite.getBotInfo().getReddit().isEnabled()) {
			cmds.add(new CommandReddit());
		}

		return cmds;
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

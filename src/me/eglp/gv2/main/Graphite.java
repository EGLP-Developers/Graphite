package me.eglp.gv2.main;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.StopWatch;
import org.mozilla.javascript.ContextFactory;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;

import club.minnced.opus.util.OpusLibrary;
import me.eglp.gv2._test.Tests;
import me.eglp.gv2.console.ConsoleInputListener;
import me.eglp.gv2.guild.GraphiteAudioChannel;
import me.eglp.gv2.guild.GraphiteCategory;
import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.guild.GraphiteGuildChannel;
import me.eglp.gv2.guild.GraphiteGuildMessageChannel;
import me.eglp.gv2.guild.GraphiteMember;
import me.eglp.gv2.guild.GraphiteNewsChannel;
import me.eglp.gv2.guild.GraphiteRole;
import me.eglp.gv2.guild.GraphiteTextChannel;
import me.eglp.gv2.guild.GraphiteVoiceChannel;
import me.eglp.gv2.guild.automod.AutoModMySQL;
import me.eglp.gv2.guild.automod.badwords.BadWords;
import me.eglp.gv2.guild.automod.discord_invites.DiscordInvites;
import me.eglp.gv2.guild.automod.excessive_caps.ExcessiveCaps;
import me.eglp.gv2.guild.automod.excessive_emoji.ExcessiveEmoji;
import me.eglp.gv2.guild.automod.excessive_mentions.ExcessiveMentions;
import me.eglp.gv2.guild.automod.excessive_spoilers.ExcessiveSpoilers;
import me.eglp.gv2.guild.automod.external_links.ExternalLinks;
import me.eglp.gv2.guild.automod.repeated_text.RepeatedText;
import me.eglp.gv2.guild.automod.zalgo.Zalgo;
import me.eglp.gv2.guild.config.GuildGreeterConfig;
import me.eglp.gv2.user.GraphitePrivateChannel;
import me.eglp.gv2.user.GraphiteUser;
import me.eglp.gv2.util.FileManager;
import me.eglp.gv2.util.GraphiteDataManager;
import me.eglp.gv2.util.GraphiteUtil;
import me.eglp.gv2.util.LoadTimes;
import me.eglp.gv2.util.amongus.GraphiteAmongUs;
import me.eglp.gv2.util.apis.genius.GraphiteGenius;
import me.eglp.gv2.util.apis.reddit.GraphiteReddit;
import me.eglp.gv2.util.apis.spotify.GraphiteSpotify;
import me.eglp.gv2.util.apis.twitch.GraphiteTwitch;
import me.eglp.gv2.util.base.GraphiteLocalizable;
import me.eglp.gv2.util.base.GraphiteMessageChannel;
import me.eglp.gv2.util.command.Command;
import me.eglp.gv2.util.command.slash.CommandCompleteListener;
import me.eglp.gv2.util.command.slash.SlashCommandListener;
import me.eglp.gv2.util.command.text.CommandHandler;
import me.eglp.gv2.util.command.text.CommandListener;
import me.eglp.gv2.util.economy.GraphiteEconomy;
import me.eglp.gv2.util.event.GraphiteJDAListener;
import me.eglp.gv2.util.event.GraphiteListener;
import me.eglp.gv2.util.event.SingleEventHandler;
import me.eglp.gv2.util.game.GraphiteMinigames;
import me.eglp.gv2.util.game.impl.rpg.RPG;
import me.eglp.gv2.util.lang.DefaultLocaleString;
import me.eglp.gv2.util.lang.DefaultMessage;
import me.eglp.gv2.util.lang.LocalizedString;
import me.eglp.gv2.util.lang.LocalizedTimeUnit;
import me.eglp.gv2.util.mysql.GraphiteMySQL;
import me.eglp.gv2.util.queue.GraphiteQueue;
import me.eglp.gv2.util.scripting.GraphiteContextFactory;
import me.eglp.gv2.util.selfcheck.Selfcheck;
import me.eglp.gv2.util.settings.BotInfo;
import me.eglp.gv2.util.settings.GraphiteSettings;
import me.eglp.gv2.util.stats.GraphiteStatistic;
import me.eglp.gv2.util.stats.GraphiteStatistics;
import me.eglp.gv2.util.stats.element.GuildStatisticsElement;
import me.eglp.gv2.util.voting.GraphiteVoteSource;
import me.eglp.gv2.util.voting.GraphiteVoting;
import me.eglp.gv2.util.webinterface.GraphiteWebinterface;
import me.eglp.gv2.util.webinterface.base.GraphiteWebinterfaceUser;
import me.eglp.gv2.util.website.GraphiteWebsiteEndpoint;
import me.mrletsplay.mrcore.config.CustomConfig;
import me.mrletsplay.mrcore.config.impl.yaml.YAMLFileCustomConfig;
import me.mrletsplay.mrcore.misc.FriendlyException;
import me.mrletsplay.mrcore.misc.LookupList;
import me.mrletsplay.mrcore.misc.SingleLookupList;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ApplicationInfo;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.NewsChannel;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.events.role.RoleDeleteEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

public class Graphite {

	// TODO: disable commands if it is not configured (e.g. CommandAmongUs)

	private static PrintStream defaultSysOut, defaultSysErr;

	private static GraphiteSettings settings;
	protected static BotInfo botInfo;
	private static List<GraphiteOption> options;

	protected static GraphiteJDAListener jdaListener;
	protected static GraphiteListener customListener;
	protected static CommandListener commandListener;
	protected static GraphiteScheduler scheduler;
	protected static GraphiteWebinterface webinterface;
	protected static GraphiteWebsiteEndpoint websiteEndpoint;
	protected static GraphiteTwitch twitch;
	protected static GraphiteMySQL mysql;
	protected static GraphiteReddit reddit;
	protected static GraphiteMinigames minigames;
	protected static GraphiteEconomy economy;
	protected static GraphiteGenius genius;
	protected static GraphiteVoting voting;
	protected static GraphiteSpotify spotify;
	protected static GraphiteAmongUs amongUs;
	protected static GraphiteStatistics statistics;
	protected static long startedAt;
	protected static AudioPlayerManager audioPlayerManager;
	protected static FileManager fileManager;
	protected static GraphiteDataManager dataManager;
	protected static GraphiteLogger logger;

	protected static boolean isOnline;
	protected static boolean isShutdown;
	protected static boolean selfcheckMode;

	protected static LookupList<String, GraphiteGuild> cachedGuilds;
	protected static GraphiteQueue queue;

	protected static List<Class<? extends Enum<? extends LocalizedString>>> defaultMessages;

	public static void main(String[] args) throws Exception {
		if(args.length == 0) throw new FriendlyException("Need path to settings.json");
		settings = new GraphiteSettings(args[0]);

		options = new ArrayList<>();
		for(int i = 1; i < args.length; i++) {
			final int fI = i;
			GraphiteOption o = Arrays.stream(GraphiteOption.values())
					.filter(op -> op.getName().equalsIgnoreCase(args[fI]))
					.findFirst().orElse(null);
			if(o != null) options.add(o);
		}

		if(settings.isDefaultCreated()) {
			System.out.println("Created a default configuration, please edit it accordingly");
			return;
		}

		List<String> errors = settings.validate();
		if(!errors.isEmpty()) {
			System.out.println("The configuration contains errors:");
			errors.forEach(e -> System.out.println("- " + e));
			return;
		}

		botInfo = settings.getBotInfo();

		if(hasOption(GraphiteOption.SELFCHECK)) {
			selfcheckMode = true;
		}

		defaultSysOut = System.out;
		defaultSysErr = System.err;
		try {
			createShardsAndStart();
		}catch(Exception e) {
			e.printStackTrace();
			System.out.println("Failed to start bot");
			return;
		}

		Runtime.getRuntime().addShutdownHook(new Thread(() -> Graphite.shutdown(true)));
	}

	private static void createShardsAndStart() throws Exception {
		StopWatch sw = StopWatch.createStarted();
		isOnline = false;
		isShutdown = false;
		jdaListener = new GraphiteJDAListener();
		customListener = new GraphiteListener();
		fileManager = new FileManager(new File(botInfo.getFileLocation()));
		GraphiteDebug.init();

		File f = fileManager.getLogFile();
		try {
			logger = new GraphiteLogger(f);
			System.setOut(new OutputInterceptor(System.out, logger::log));
			System.setErr(new OutputInterceptor(System.err, logger::log));
		}catch(IOException e) {
			e.printStackTrace();
		}

		log("Initializing bot...");
		GraphiteBot.start(botInfo);

		sw.stop();
		start(sw.getTime(TimeUnit.MILLISECONDS));
	}

	public static void start(long preInitTime) throws Exception {
		StopWatch sw = StopWatch.createStarted();

		if(!ContextFactory.hasExplicitGlobal()) ContextFactory.initGlobal(new GraphiteContextFactory());
		OpusLibrary.loadFromJar();

		cachedGuilds = new SingleLookupList<>(GraphiteGuild::getID);
		defaultMessages = new ArrayList<>();
		defaultMessages.add(DefaultMessage.class);
		defaultMessages.add(DefaultLocaleString.class);
		startedAt = System.currentTimeMillis();

		scheduler = new GraphiteScheduler();
		audioPlayerManager = new DefaultAudioPlayerManager();
		audioPlayerManager.getConfiguration().setFilterHotSwapEnabled(true);
		AudioSourceManagers.registerRemoteSources(audioPlayerManager);

		log("Waiting for all shards to come online");
		long initTime = sw.getTime(TimeUnit.MILLISECONDS);
		sw.reset();
		sw.start();
		GraphiteBot.awaitLoad();

		long connectTime = sw.getTime(TimeUnit.MILLISECONDS);
		sw.reset();
		sw.start();

		GraphiteBot.shards.forEach(s -> {
			CommandListUpdateAction a;
			if(!botInfo.isBeta()) {
				a = s.getJDA().updateCommands();
			}else {
//				s.getJDA().updateCommands().queue();
				Guild g = s.getJDA().getGuildById(botInfo.getMiscellaneous().getTestingServerID());
				if(g == null) {
					Graphite.log(botInfo.getName() + ": Unknown testing server");
					return;
				}
				Graphite.log(botInfo.getName() + ": Testing server is " + g.getName());
				a = g.updateCommands();
			}

			GraphiteBot.getConfiguredCommands().forEach(c -> {
				CommandHandler.registerCommand(c);

				if(c.isBeta() && !botInfo.isBeta()) return;

				SlashCommandData d = Commands.slash(c.getName(), c.getDescription().getFallback());
				d.setDefaultPermissions(c.getPermission() == null ? DefaultMemberPermissions.ENABLED : DefaultMemberPermissions.DISABLED);
				if(!c.getOptions().isEmpty()) d.addOptions(c.getOptions());
				SlashCommandListener.registerSlashCommand(c.getName(), c);

				if(!c.getSubCommands().isEmpty()) {
					for(Command sc : c.getSubCommands()) {
						if(!sc.getSubCommands().isEmpty()) {
							SubcommandGroupData grp = new SubcommandGroupData(sc.getName(), sc.getDescription().getFallback());
							for(Command ssc : sc.getSubCommands()) {
								grp.addSubcommands(new SubcommandData(ssc.getName(), ssc.getDescription().getFallback()).addOptions(ssc.getOptions()));
								SlashCommandListener.registerSlashCommand(c.getName() + " " + sc.getName() + " " + ssc.getName(), ssc);
							}
							d.addSubcommandGroups(grp);
						}else {
							d.addSubcommands(new SubcommandData(sc.getName(), sc.getDescription().getFallback()).addOptions(sc.getOptions()));
							SlashCommandListener.registerSlashCommand(c.getName() + " " + sc.getName(), sc);
						}
					}
				}

				a.addCommands(d);
			});

			a.queue();
		});

		mysql = new GraphiteMySQL();
		dataManager = new GraphiteDataManager();
		mysql.createTables();

		GraphiteSetup.run();

		commandListener = new CommandListener();
		voting = new GraphiteVoting();
		minigames = new GraphiteMinigames();
		twitch = new GraphiteTwitch();
		reddit = new GraphiteReddit();

		if(botInfo.getSpotify().isEnabled()) spotify = new GraphiteSpotify();
		if(botInfo.getGenius().isEnabled()) genius = new GraphiteGenius();

		if(botInfo.getAmongUs().isEnabled()) {
			amongUs = new GraphiteAmongUs();
		}

		economy = new GraphiteEconomy();
		statistics = new GraphiteStatistics();

		webinterface = new GraphiteWebinterface();
		websiteEndpoint = new GraphiteWebsiteEndpoint();
		ConsoleInputListener.init();

		queue = new GraphiteQueue("Donator", 10, 3);

		log("Initializing guilds");

		AtomicInteger num = new AtomicInteger(0);
		AtomicReference<String> currentGuild = new AtomicReference<>("none");

		List<String> allGuildIds = GraphiteBot.shards.stream()
				.flatMap(s -> s.getJDA().getGuilds().stream())
				.map(g -> g.getId())
				.distinct()
				.collect(Collectors.toList());

		ScheduledFuture<?> f = scheduler.getExecutorService().scheduleAtFixedRate(() -> log("Guilds initialized: " + num.get() + "/" + allGuildIds.size() + " (Current guild: " + currentGuild.get() + ")"), 2, 2, TimeUnit.SECONDS);

		LoadTimes totalLoadTimes = new LoadTimes();

		for(String gID : allGuildIds) {
			currentGuild.set(gID);
			GraphiteGuild g = getGuild(getJDAGuild(gID), false);
			totalLoadTimes.add(g.load());
			num.incrementAndGet();
		}

		totalLoadTimes.print();

		f.cancel(false);

		AutoModMySQL.removeOldInfractions();
		GraphiteCommonTasks.start();
		GraphiteCommonListeners.register();

		jdaListener.registerHandler(commandListener);
		jdaListener.registerHandler(new SlashCommandListener());
		jdaListener.registerHandler(new CommandCompleteListener());

		jdaListener.registerHandler(SingleEventHandler.of(GuildLeaveEvent.class, event -> {
			GraphiteGuild g = getGuild(event.getGuild());
			if(g == null) return; // Probably shouldn't ever happen, but who knows
			discardGuild(event.getGuild());
		}));

		jdaListener.registerHandler(SingleEventHandler.of(GuildMemberRemoveEvent.class, event -> {
			GraphiteGuild guild = getGuild(event.getGuild());
			GuildGreeterConfig c = guild.getGreeterConfig();
			getGuild(event.getGuild()).discardMemberByID(event.getUser().getId());
			if(c.getFarewellChannel() != null && c.isFarewellEnabled()) {
				c.getFarewellChannel().sendMessage(c.getFarewellMessage(), "user", event.getUser().getName(), "server", guild.getName());
			}
		}));

		jdaListener.registerHandler(SingleEventHandler.of(RoleDeleteEvent.class, event -> {
			getGuild(event.getGuild()).discardRole(event.getRole());
		}));

		jdaListener.registerHandler(SingleEventHandler.of(ChannelDeleteEvent.class, event -> {
			getGuild(event.getGuild()).discardChannel((GuildChannel) event.getChannel());
		}));

		jdaListener.registerHandler(SingleEventHandler.of(GuildJoinEvent.class, event -> {
			dataManager.addGuild(event.getGuild().getId());
//			Graphite.getGuild(event.getGuild()).getConfig().updateCommandPrivileges();

			Member owner = event.getGuild().getOwner();
			if(owner == null) return; // Only execute if owner exists
			GraphiteMember m = Graphite.getMember(owner);
			GraphiteMessageChannel<?> ch = m.openPrivateChannel();
			if(ch == null) ch = Graphite.getGuild(event.getGuild()).getTextChannels().get(0);

			String prefix = botInfo.getDefaultPrefix();

			EmbedBuilder eb = new EmbedBuilder();

			eb.setColor(Color.WHITE);
			eb.setTitle(DefaultLocaleString.EVENT_SERVER_JOIN_TITLE.getFor(m, "user", m.getName()));
			eb.setDescription(DefaultLocaleString.EVENT_SERVER_JOIN_DESCRIPTION.getFor(m, "discord", botInfo.getLinks().getDiscord(), "website", botInfo.getWebsite().getBaseURL(), "faq", botInfo.getWebsite().getFAQURL()));
			eb.addBlankField(false);
			eb.addField(DefaultLocaleString.EVENT_SERVER_JOIN_FIELD_1_TITLE.getFor(m),
					DefaultLocaleString.EVENT_SERVER_JOIN_FIELD_1_CONTENT.getFor(m, "prefix", prefix, "webinterface", botInfo.getWebsite().getWebinterfaceURL()), false);
			eb.addBlankField(false);
			eb.addField(DefaultLocaleString.EVENT_SERVER_JOIN_FIELD_2_TITLE.getFor(m),
					DefaultLocaleString.EVENT_SERVER_JOIN_FIELD_2_CONTENT.getFor(m, "prefix", prefix), false);
			eb.addBlankField(false);
			eb.addField(DefaultLocaleString.EVENT_SERVER_JOIN_FIELD_3_TITLE.getFor(m),
					DefaultLocaleString.EVENT_SERVER_JOIN_FIELD_3_CONTENT.getFor(m, "prefix", prefix), false);
			eb.addBlankField(false);
			eb.addField(DefaultLocaleString.EVENT_SERVER_JOIN_FIELD_4_TITLE.getFor(m),
					DefaultLocaleString.EVENT_SERVER_JOIN_FIELD_4_CONTENT.getFor(m, "webinterface", botInfo.getWebsite().getWebinterfaceURL()), false);
			eb.addBlankField(false);
			eb.addField(DefaultLocaleString.EVENT_SERVER_JOIN_FIELD_5_TITLE.getFor(m),
					DefaultLocaleString.EVENT_SERVER_JOIN_FIELD_5_CONTENT.getFor(m, "discord", botInfo.getLinks().getDiscord()), false);
			ch.sendMessage(eb.build());
		}));

		jdaListener.registerHandler(SingleEventHandler.of(GuildMemberJoinEvent.class, event -> {
			GraphiteGuild guild = getGuild(event.getGuild());
			GraphiteMember member = getMember(event.getMember());

			GuildGreeterConfig c = guild.getGreeterConfig();

			if(c.isGreetingEnabled()) {
				(c.getGreetingChannel() == null ? getMember(event.getMember()).openPrivateChannel() : c.getGreetingChannel()).sendMessage(c.getGreetingMessage(), "user", member.getName(), "mention", member.getAsMention(), "server", guild.getName());
			}

			GraphiteMember selfM = guild.getMember(event.getGuild().getSelfMember());
			if(member.isBot()) {
				guild.getRolesConfig().getBotRoles().forEach(r -> {
					if(selfM.canInteract(r) && selfM.canInteract(member)) {
						guild.addRoleToMember(member, r);
					}
				});
			}else {
				guild.getRolesConfig().getAutoRoles().forEach(r -> {
					if(selfM.canInteract(r) && selfM.canInteract(member)) {
						guild.addRoleToMember(member, r);
					}
				});
			}
		}));

		jdaListener.registerHandler(SingleEventHandler.of(GuildVoiceUpdateEvent.class, event -> {
			if(event.getChannelLeft() != null) {
				GraphiteGuild guild = getGuild(event.getGuild());
				GraphiteMember member = getMember(event.getMember());
				if(member.getID().equals(getBotID())) {
					if(guild.getMusic().isPlaying()) guild.getMusic().stop();

					if(guild.getRecorder().isRecording()) guild.getRecorder().stop();
				}
			}

			if(event.getChannelJoined() != null) {
				GraphiteGuild guild = getGuild(event.getGuild());
				GraphiteMember member = getMember(event.getMember());
				if(member.getID().equals(getBotID())) {
					boolean deafen = guild.getMusic().isPlaying() && !guild.getRecorder().isRecording();
					guild.getJDAGuild().getAudioManager().setSelfDeafened(deafen);
				}
			}
		}));

		jdaListener.registerHandler(SingleEventHandler.of(MessageReceivedEvent.class, event -> {
			if(!event.isFromGuild()) return;
			GraphiteGuild guild = getGuild(event.getGuild());

			List<GuildStatisticsElement> els = guild.getStatisticsConfig().getStatisticsElements();
			boolean required = els.stream()
					.flatMap(el -> el.getSettings().getStatistics().stream())
					.anyMatch(s -> s == GraphiteStatistic.NEW_MESSAGES);
			if(required) statistics.incrementCumulativeStatistic(guild, GraphiteStatistic.NEW_MESSAGES, null);

			boolean requiredByChannel = els.stream()
					.flatMap(el -> el.getSettings().getStatistics().stream())
					.anyMatch(s -> s == GraphiteStatistic.NEW_MESSAGES_BY_CHANNEL);
			if(requiredByChannel) statistics.incrementCumulativeStatistic(guild, GraphiteStatistic.NEW_MESSAGES_BY_CHANNEL, GraphiteUtil.truncateToLength(event.getChannel().getName(), 64, false));

			boolean requiredEmoji = els.stream()
					.flatMap(el -> el.getSettings().getStatistics().stream())
					.anyMatch(s -> s == GraphiteStatistic.MESSAGE_EMOJI);
			if(requiredEmoji) {
				GraphiteUtil.extractUnicodeEmoji(event.getMessage().getContentRaw()).forEach(e -> statistics.incrementCumulativeStatistic(guild, GraphiteStatistic.MESSAGE_EMOJI, e.getUnicode()));
				Matcher m = GraphiteUtil.CUSTOM_EMOJI_PATTERN.matcher(event.getMessage().getContentRaw());
				while(m.find()) {
					statistics.incrementCumulativeStatistic(guild, GraphiteStatistic.MESSAGE_EMOJI, m.group());
				}
			}
		}));

		jdaListener.registerHandler(SingleEventHandler.of(GuildMemberJoinEvent.class, event -> {
			GraphiteGuild guild = getGuild(event.getGuild());

			List<GuildStatisticsElement> els = guild.getStatisticsConfig().getStatisticsElements();
			boolean required = els.stream()
					.flatMap(el -> el.getSettings().getStatistics().stream())
					.anyMatch(s -> s == GraphiteStatistic.NEW_MEMBERS);
			if(required) statistics.incrementCumulativeStatistic(guild, GraphiteStatistic.NEW_MEMBERS, null);
		}));

		jdaListener.registerHandler(SingleEventHandler.of(MessageReactionAddEvent.class, event -> {
			GraphiteGuild guild = getGuild(event.getGuild());

			List<GuildStatisticsElement> els = guild.getStatisticsConfig().getStatisticsElements();
			boolean requiredEmoji = els.stream()
					.flatMap(el -> el.getSettings().getStatistics().stream())
					.anyMatch(s -> s == GraphiteStatistic.REACTION_EMOJI);

			if(requiredEmoji) {
				Emoji e = event.getEmoji();
				String cat = e.getFormatted();
				statistics.incrementCumulativeStatistic(guild, GraphiteStatistic.REACTION_EMOJI, cat);
			}
		}));

		jdaListener.registerHandler(SingleEventHandler.of(MessageReactionRemoveEvent.class, event -> {
			GraphiteGuild guild = getGuild(event.getGuild());

			List<GuildStatisticsElement> els = guild.getStatisticsConfig().getStatisticsElements();
			boolean requiredEmoji = els.stream()
					.flatMap(el -> el.getSettings().getStatistics().stream())
					.anyMatch(s -> s == GraphiteStatistic.REACTION_EMOJI);

			if(requiredEmoji) {
				Emoji e = event.getEmoji();
				String cat = e.getFormatted();
				statistics.decrementCumulativeStatistic(guild, GraphiteStatistic.REACTION_EMOJI, cat);
			}
		}));

		jdaListener.registerHandler(new BadWords());
		jdaListener.registerHandler(new DiscordInvites());
		jdaListener.registerHandler(new ExcessiveCaps());
		jdaListener.registerHandler(new ExcessiveEmoji());
		jdaListener.registerHandler(new ExcessiveMentions());
		jdaListener.registerHandler(new ExcessiveSpoilers());
		jdaListener.registerHandler(new ExternalLinks());
		jdaListener.registerHandler(new RepeatedText());
		jdaListener.registerHandler(new Zalgo());

		if(botInfo.isBeta()) {
			Tests.runTests();
		}else {
			//CommandHandler.sendCommandsToWebsite();
		}

		if(selfcheckMode) {
			Selfcheck.runFullCheck();
			shutdown(true);
			return;
		}

		GraphiteBot.startStatisticsCollectors();

		Selfcheck.startPeriodicCheck();

		long postInitTime = sw.getTime(TimeUnit.MILLISECONDS);
		sw.stop();

		isOnline = true;

		log("I'm ready to rumble! (PreInit: " + preInitTime + " ms, Init: " + initTime + " ms, Connect: " + connectTime + " ms, PostInit / Tests: " + postInitTime + " ms, Total: " + (preInitTime + initTime + connectTime + postInitTime) + " ms)");
	}

	public static Thread shutdown(boolean exit) {
		if(isShutdown) return null;
		isOnline = false;

		Thread s = new Thread(() -> {
			isShutdown = true;
			Graphite.log("Awaiting shutdown...");
			jdaListener.unregisterAll();
			queue.stop(60);
			webinterface.stop();
			websiteEndpoint.stop();
			scheduler.stop(60);
			RPG.saveGlobalGame();
			if(amongUs != null) amongUs.stop();
			System.setOut(defaultSysOut);
			System.setErr(defaultSysErr);
			mysql.close();
			Graphite.log("Shutdown complete!");
			logger.close();
			if(exit) System.exit(0);
		}, "Shutdown-Thread");
		s.start();
		return s;
	}

	public static boolean isOnline() {
		return isOnline;
	}

	public static void restart() {
		new Thread(() -> {
			Thread s = shutdown(false);
			if(s == null) return;
			try {
				s.join();
				Thread.sleep(1000);
				createShardsAndStart();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}, "Restart-Thread").start();
	}

	public static List<GraphiteOption> getOptions() {
		return options;
	}

	public static boolean hasOption(GraphiteOption option) {
		return options.contains(option);
	}

	public static List<GraphiteShard> getShards() {
		return GraphiteBot.shards;
	}

	public static boolean isOnGuild(String guildID) {
		return getJDAGuild(guildID) != null;
	}

	public static ApplicationInfo getApplicationInfo() {
		if(getShards().isEmpty()) throw new IllegalStateException("Bot not running u blockhead yeet");
		ApplicationInfo info = getShards().get(0).getJDA().retrieveApplicationInfo().complete();
		info.setRequiredScopes("bot", "applications.commands");
		return info;
	}

	public static String getBotID() {
		if(getShards().isEmpty()) throw new IllegalStateException("Bot not running u blockhead yeet");
		return getShards().get(0).getJDA().getSelfUser().getId();
	}

	public static String getInviteUrl() {
		return "https://discord.com/oauth2/authorize?client_id={app_id}&permissions=8&scope=applications.commands%20bot"
				.replace("{app_id}", getApplicationInfo().getId());
	}

	public static String getIconUrl() {
		return getApplicationInfo().getIconUrl();
	}

	public static BotInfo getBotInfo() {
		return botInfo;
	}

	public static List<GraphiteVoteSource> getVoteSources() {
		return botInfo.getVoteSettings().getVoteSources();
	}

	public static FileManager getFileManager() {
		return fileManager;
	}

	public static GraphiteDataManager getDataManager() {
		return dataManager;
	}

	public static GraphiteLogger getLogger() {
		return logger;
	}

	public static AudioPlayerManager getAudioPlayerManager() {
		return audioPlayerManager;
	}

	public static String getFormattedTimeSinceRestart(GraphiteLocalizable localizable) {
		return LocalizedTimeUnit.formatTime(localizable, System.currentTimeMillis() - startedAt);
	}

	public static long getMillisSinceRestart() {
		return System.currentTimeMillis() - startedAt;
	}

	public static GraphiteQueue getQueue() {
		return queue;
	}

	public synchronized static void log(String message) {
		System.out.println("[Graphite] | " + Thread.currentThread().getName() + "] " + message);
	}

	public static GraphiteJDAListener getJDAListener() {
		return jdaListener;
	}

	public static GraphiteListener getCustomListener() {
		return customListener;
	}

	public static GraphiteScheduler getScheduler() {
		return scheduler;
	}

	public static GraphiteWebinterface getWebinterface() {
		return webinterface;
	}

	public static GraphiteWebsiteEndpoint getWebsiteEndpoint() {
		return websiteEndpoint;
	}

	public static GraphiteTwitch getTwitch() {
		return twitch;
	}

	public static GraphiteMySQL getMySQL() {
		return mysql;
	}

	public static GraphiteReddit getReddit() {
		return reddit;
	}

	public static GraphiteMinigames getMinigames() {
		return minigames;
	}

	public static GraphiteVoting getVoting() {
		return voting;
	}

	public static GraphiteSpotify getSpotify() {
		return spotify;
	}

	public static GraphiteEconomy getEconomy() {
		return economy;
	}

	public static GraphiteGenius getGenius() {
		return genius;
	}

	public static GraphiteAmongUs getAmongUs() {
		return amongUs;
	}

	public static GraphiteStatistics getStatistics() {
		return statistics;
	}

	public static List<GraphiteGuild> getGuilds() {
		return getShards().stream()
				.flatMap(s -> s.getJDA().getGuilds().stream())
				.map(Graphite::getGuild)
				.distinct()
				.collect(Collectors.toList());
	}

	public static int getGuildCount() {
		return getGuilds().size();
	}

	public static Guild getJDAGuild(String id) {
		if(!isValidSnowflake(id)) return null;
		return getShards().stream().map(s -> s.getJDA().getGuildById(id)).filter(g -> g != null).findFirst().orElse(null);
	}

	public static boolean isValidSnowflake(String flake) {
		try {
			Long.parseLong(flake);
			return true;
		}catch(NumberFormatException e) {
			return false;
		}
	}

	public static GraphiteGuild getGuild(String id) {
		return getGuild(getJDAGuild(id));
	}

	public static GraphiteGuild getGuildRaw(Guild guild) {
		return cachedGuilds.lookup(guild.getId());
	}

	private synchronized static GraphiteGuild getGuild(Guild guild, boolean load) {
		if(guild == null) return null;
		GraphiteGuild g = getGuildRaw(guild);
		if(g == null) {
			g = new GraphiteGuild(guild);
			cachedGuilds.add(g);
			if(load) g.load();
		}
		return g;
	}

	public synchronized static GraphiteGuild getGuild(Guild guild) {
		return getGuild(guild, true);
	}

	protected static void discardGuild(Guild guild) {
		GraphiteGuild g = getGuildRaw(guild);
		if(g != null) cachedGuilds.remove(g);
	}

	public static User getJDAUser(String id) {
		return getShards().stream()
			.map(s -> s.getJDA().retrieveUserById(id).onErrorMap(ErrorResponse.UNKNOWN_USER::test, e -> null).complete())
			.filter(Objects::nonNull)
			.findFirst().orElse(null);
	}

	public static GraphiteUser getUser(String id) {
		return getUser(getJDAUser(id));
	}

	@Deprecated
	public static User getJDAUser(String name, String hash) {
		return getShards()
				.stream().map(s -> s.getJDA().getUsersByName(name, true).stream()
						.filter(u -> u.getDiscriminator().equals(hash)).findFirst().orElse(null))
				.filter(u -> u != null).findFirst().orElse(null);
	}

	@Deprecated
	public static GraphiteUser getUser(String name, String hash) {
		return getUser(getJDAUser(name, hash));
	}

	public static List<User> getJDAUsersByName(String name) {
		return getShards().stream().map(s -> s.getJDA().getUsersByName(name, true))
				.flatMap(Collection::stream)
				.collect(Collectors.toList());
	}

	public static List<GraphiteUser> getUsersByName(String name) {
		return getUsers(getJDAUsersByName(name));
	}

	public static List<GraphiteUser> getUsers(List<User> users) {
		return users.stream()
				.map(u -> getUser(u))
				.collect(Collectors.toList());
	}

	public static GraphiteUser getUser(User user) {
		return new GraphiteUser(user); // TODO: check if works
	}

	public static GraphiteWebinterfaceUser getWebinterfaceUser(String id) {
		return webinterface.getAccountManager().getUser(id);
	}

	public static GraphiteWebinterfaceUser getWebinterfaceUser(GraphiteUser user) {
		return webinterface.getAccountManager().getUser(user);
	}

	public static GraphiteRole getRole(Role role) {
		return getGuild(role.getGuild()).getRole(role);
	}

	public static GraphiteTextChannel getTextChannel(TextChannel channel) {
		return getGuild(channel.getGuild()).getTextChannel(channel);
	}

	public static GraphitePrivateChannel getPrivateChannel(PrivateChannel channel) {
		return getUser(channel.getUser()).openPrivateChannel();
	}

	public static GraphiteNewsChannel getNewsChannel(NewsChannel channel) {
		return getGuild(channel.getGuild()).getNewsChannel(channel);
	}

	public static GraphiteGuildMessageChannel getGuildMessageChannel(GuildMessageChannel channel) {
		return getGuild(channel.getGuild()).getGuildMessageChannel(channel);
	}

	public static GraphiteGuildChannel getGuildChannel(GuildChannel channel) {
		return getGuild(channel.getGuild()).getGuildChannel(channel);
	}

	public static GraphiteMessageChannel<?> getMessageChannel(MessageChannel channel) {
		if(channel instanceof GuildMessageChannel) {
			return getGuildMessageChannel((GuildMessageChannel) channel);
		}else if(channel instanceof PrivateChannel) {
			return getPrivateChannel((PrivateChannel) channel);
		}
		throw new IllegalArgumentException("Invalid channel type: " + channel.getClass());
	}

	public static GraphiteMember getMember(Member member) {
		return getGuild(member.getGuild()).getMember(member);
	}

	public static GraphiteVoiceChannel getVoiceChannel(VoiceChannel channel) {
		return getGuild(channel.getGuild()).getVoiceChannel(channel);
	}

	public static GraphiteAudioChannel getAudioChannel(AudioChannel channel) {
		return getGuild(channel.getGuild()).getAudioChannel(channel);
	}

	public static GraphiteCategory getCategory(Category category) {
		return getGuild(category.getGuild()).getCategory(category);
	}

	public static RichCustomEmoji getJDAEmote(long id) {
		return getShards().stream().map(s -> s.getJDA().getEmojiById(id)).filter(g -> g != null).findFirst().orElse(null);
	}

	public static RichCustomEmoji getJDAEmote(String id) {
		if(!isValidSnowflake(id)) return null;
		return getShards().stream().map(s -> s.getJDA().getEmojiById(id)).filter(g -> g != null).findFirst().orElse(null);
	}

	public static <T> T completeRestAction(RestAction<T> action) {
		try {
			return action.complete();
		}catch(Exception e) {
			GraphiteDebug.log(DebugCategory.JDA, e);
			return null;
		}
	}

	public static List<Class<? extends Enum<? extends LocalizedString>>> getDefaultMessages() {
		return defaultMessages;
	}

	public static CustomConfig generateDefaultLocale() {
		CustomConfig cc = new YAMLFileCustomConfig((File) null);
		for(Class<? extends Enum<? extends LocalizedString>> c : defaultMessages) {
			try {
				Object[] o = (Object[]) c.getMethod("values").invoke(null); // NONBETA: use getEnumConstants
				for(Object s : o) {
					LocalizedString st = (LocalizedString) s;
					cc.set(st.getMessagePath(), st.getFallback());
				}
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
					| NoSuchMethodException | SecurityException e) {
				GraphiteDebug.log(DebugCategory.MISCELLANEOUS, e);
			}
		}
		return cc;
	}

	public static int getCustomizableMessageAmount() {
		int am = 0;
		for(Class<? extends Enum<? extends LocalizedString>> c : defaultMessages) {
			try {
				Object[] o = (Object[]) c.getMethod("values").invoke(null); // NONBETA: use getEnumConstants
				am += o.length;
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
					| NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
			}
		}
		return am;
	}

}

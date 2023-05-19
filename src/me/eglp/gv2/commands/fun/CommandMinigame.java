package me.eglp.gv2.commands.fun;

import java.awt.Color;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import me.eglp.gv2.guild.GraphiteModule;
import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.user.GraphiteUser;
import me.eglp.gv2.util.command.Command;
import me.eglp.gv2.util.command.CommandCategory;
import me.eglp.gv2.util.command.CommandInvokedEvent;
import me.eglp.gv2.util.command.ParentCommand;
import me.eglp.gv2.util.game.GraphiteMinigame;
import me.eglp.gv2.util.game.GraphiteMinigameStats;
import me.eglp.gv2.util.game.MinigameInstance;
import me.eglp.gv2.util.game.MultiPlayerMinigameInstance;
import me.eglp.gv2.util.lang.DefaultLocaleString;
import me.eglp.gv2.util.lang.DefaultMessage;
import me.eglp.gv2.util.selfcheck.SpecialSelfcheck;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class CommandMinigame extends ParentCommand {

	public CommandMinigame() {
		super(GraphiteModule.FUN, CommandCategory.FUN, "minigame");
		addAlias("mg");
		setDescription(DefaultLocaleString.COMMAND_MINIGAME_DESCRIPTION);
		setAllowPrivate(true);
		setAllowServer(true);

		addSubCommand(new Command(this, "play") {

			@Override
			public void action(CommandInvokedEvent event) {
				if(Graphite.getMinigames().getGame(event.getAuthor()) != null) {
					DefaultMessage.COMMAND_MINIGAME_PLAY_ALREADY_PLAYING.reply(event);
					return;
				}
				String mg = (String) event.getOption("game");
				GraphiteMinigame m = GraphiteMinigame.getByName(event.getSender(), mg);
				if(m == null) {
					DefaultMessage.COMMAND_MINIGAME_PLAY_INVALID_GAME.reply(event, "minigames",
							Arrays.stream(GraphiteMinigame.values())
							.map(g -> g.getFriendlyName().getFor(event.getSender()))
							.collect(Collectors.joining(", ")));
					return;
				}

				DefaultMessage.COMMAND_MINIGAME_PLAY_GAME_STARTED.reply(event,
						"minigame", m.getFriendlyName().getFor(event.getAuthor()),
						"multiplayer", (m.isMultiplayer() ? DefaultLocaleString.COMMAND_MINIGAME_PLAY_MULTIPLAYER.getFor(event.getAuthor()):""));

				m.startNewGame(event.getAuthor());
			}

			@Override
			public List<OptionData> getOptions() {
				OptionData d = new OptionData(OptionType.STRING, "game", "The game you want to play", true);
				Arrays.stream(GraphiteMinigame.values()).forEach(g -> {
					d.addChoice(g.getFriendlyName().getFallback(), g.getFriendlyName().getFallback());
				});
				return Arrays.asList(d);
			}
		})
		.setDescription(DefaultLocaleString.COMMAND_MINIGAME_PLAY_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_MINIGAME_PLAY_USAGE)
		.setAllowPrivate(true)
		.setAllowServer(false);

		addSubCommand(new Command(this, "leave") {

			@Override
			public void action(CommandInvokedEvent event) {
				MinigameInstance i = Graphite.getMinigames().getGame(event.getAuthor());
				if(i == null) {
					DefaultMessage.COMMAND_MINIGAME_NOT_PLAYING.reply(event);
					return;
				}
				Graphite.getMinigames().leaveGame(event.getAuthor());
				DefaultMessage.COMMAND_MINIGAME_LEAVE_SUCCESS.reply(event);
			}

			@Override
			public List<OptionData> getOptions() {
				return Collections.emptyList();
			}

		})
		.setDescription(DefaultLocaleString.COMMAND_MINIGAME_LEAVE_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_MINIGAME_LEAVE_USAGE)
		.setAllowPrivate(true)
		.setAllowServer(false)
		.setAllowInGame(true);

		addSubCommand(new Command(this, "invite") {

			@Override
			public void action(CommandInvokedEvent event) {
				if(event.getAuthor().isBot()) {
					DefaultMessage.COMMAND_MINIGAME_INVITE_BOTS.reply(event);
					return;
				}

				if(Graphite.getMinigames().getGame(event.getAuthor()) == null) {
					DefaultMessage.COMMAND_MINIGAME_NOT_PLAYING.reply(event);
					return;
				}

				MinigameInstance i = Graphite.getMinigames().getGame(event.getAuthor());
				if(!i.getGame().isMultiplayer()) {
					DefaultMessage.COMMAND_MINIGAME_INVITE_NOT_MULTIPLAYER.reply(event);
					return;
				}

				MultiPlayerMinigameInstance mm = (MultiPlayerMinigameInstance) i;
				GraphiteUser u = (GraphiteUser) event.getOption("user");
				if(u.equals(event.getAuthor())) {
					DefaultMessage.COMMAND_MINIGAME_INVITE_SELF.reply(event);
					return;
				}

				boolean invited = mm.sendInvite(event.getAuthor(), u);
				if(!invited) {
					DefaultMessage.COMMAND_MINIGAME_INVITE_FAILED.reply(event,
							"user", u.getName());
					return;
				}

				DefaultMessage.COMMAND_MINIGAME_INVITE_SUCCESS.reply(event,
						"user", u.getName(),
						"minigame", i.getGame().getFriendlyName().getFor(event.getAuthor()));
			}

			@Override
			public List<OptionData> getOptions() {
				return Arrays.asList(
						new OptionData(OptionType.USER, "user", "The user you want to play with", true)
					);
			}

		})
		.setDescription(DefaultLocaleString.COMMAND_MINIGAME_INVITE_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_MINIGAME_INVITE_USAGE);

		addSubCommand(new Command(this, "share") {

			@Override
			public void action(CommandInvokedEvent event) {
				if(Graphite.getMinigames().getGame(event.getAuthor()) == null) {
					DefaultMessage.COMMAND_MINIGAME_NOT_PLAYING.reply(event);
					return;
				}

				MinigameInstance i = Graphite.getMinigames().getGame(event.getAuthor());
				if(!i.getGame().isMultiplayer()) {
					DefaultMessage.COMMAND_MINIGAME_INVITE_NOT_MULTIPLAYER.reply(event);
					return;
				}

				if(i.getGame().isGlobal()) {
					DefaultMessage.COMMAND_MINIGAME_SHARE_GLOBAL_GAME.reply(event);
					return;
				}

				MultiPlayerMinigameInstance mm = (MultiPlayerMinigameInstance) i;

				if(!mm.isJoinable()) {
					DefaultMessage.COMMAND_MINIGAME_SHARE_CANT_SHARE.reply(event);
					return;
				}

				Graphite.getMinigames().shareMinigame(mm);
				DefaultMessage.COMMAND_MINIGAME_SHARE_SHARED.reply(event);
			}

			@Override
			public List<OptionData> getOptions() {
				return Collections.emptyList();
			}
		})
		.setDescription(DefaultLocaleString.COMMAND_MINIGAME_SHARE_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_MINIGAME_SHARE_USAGE)
		.setAllowInGame(true)
		.setAllowPrivate(true)
		.setAllowServer(false);

		addSubCommand(new Command(this, "join") {

			@Override
			public void action(CommandInvokedEvent event) {
				if(Graphite.getMinigames().getGame(event.getAuthor()) != null) {
					DefaultMessage.COMMAND_MINIGAME_PLAY_ALREADY_PLAYING.reply(event);
					return;
				}

				String mg = (String) event.getOption("game");
				GraphiteMinigame m = GraphiteMinigame.getByName(event.getSender(), mg);
				if(m == null) {
					DefaultMessage.COMMAND_MINIGAME_PLAY_INVALID_GAME.reply(event, "minigames",
							Arrays.stream(GraphiteMinigame.values())
							.filter(g -> g.isMultiplayer() && !g.isGlobal())
							.map(g -> g.getFriendlyName().getFor(event.getSender()))
							.collect(Collectors.joining(", ")));
					return;
				}

				if(!m.isMultiplayer()) {
					DefaultMessage.COMMAND_MINIGAME_JOIN_NOT_MULTIPLAYER.reply(event);
					return;
				}

				MultiPlayerMinigameInstance inst = Graphite.getMinigames().getSharedMinigame(m, event.getAuthor());
				if(inst == null) {
					DefaultMessage.COMMAND_MINIGAME_JOIN_NO_GAMES.reply(event);
					return;
				}

				if(!inst.isJoinable()) {
					DefaultMessage.COMMAND_MINIGAME_CANT_JOIN.reply(event);
					return;
				}

				inst.addUser(event.getAuthor());
				Graphite.getMinigames().setGame(event.getAuthor(), inst);

				DefaultMessage.COMMAND_MINIGAME_JOIN_SHARED_GAME_STARTED.reply(event,
						"minigame", m.getFriendlyName().getFor(event.getAuthor()));
			}

			@Override
			public List<OptionData> getOptions() {
				OptionData d = new OptionData(OptionType.STRING, "game", "The game you want to join", true);

				return Arrays.asList(d);
			}

		})
		.setDescription(DefaultLocaleString.COMMAND_MINIGAME_JOIN_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_MINIGAME_JOIN_USAGE)
		.setAllowPrivate(true)
		.setAllowServer(false);

		addSubCommand(new Command(this, "shared") {

			@Override
			public void action(CommandInvokedEvent event) {
				String game = (String) event.getOption("game");
				GraphiteMinigame m = GraphiteMinigame.getByName(event.getSender(), game);
				if(m == null) {
					DefaultMessage.COMMAND_MINIGAME_PLAY_INVALID_GAME.reply(event, "minigames",
							Arrays.stream(GraphiteMinigame.values())
							.filter(g -> g.isMultiplayer() && !g.isGlobal())
							.map(g -> g.getFriendlyName().getFor(event.getSender()))
							.collect(Collectors.joining(", ")));
					return;
				}

				List<MultiPlayerMinigameInstance> sharedGames = Graphite.getMinigames().getSharedMinigames(m);
				if(sharedGames.isEmpty()) {
					DefaultMessage.COMMAND_MINIGAME_JOIN_NO_GAMES.reply(event);
					return;
				}
				EmbedBuilder eb = new EmbedBuilder();
				eb.setColor(Color.DARK_GRAY);

				String s = m.getFriendlyName().getFor(event.getSender());

				eb.setDescription("Current shared " + s + " game(s)");

				int i = 0;
				for(MultiPlayerMinigameInstance sharedGame : sharedGames) {
					eb.addField(s + "#" + i, sharedGame.getPlayingUsers().stream().filter(Objects::nonNull).map(pl -> "- " + pl.getName()).collect(Collectors.joining("\n")), false);
					i++;
				}

				event.reply(eb.build());
			}

			@Override
			public List<OptionData> getOptions() {
				return Arrays.asList(
						new OptionData(OptionType.STRING, "game", "Search for a specified game", true)
					);
			}

		})
		.setAllowPrivate(true)
		.setAllowServer(false)
		.setDescription(DefaultLocaleString.COMMAND_MINIGAME_SHARED_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_MINIGAME_SHARED_USAGE);

		addSubCommand(new Command(this, "list") {

			@Override
			public void action(CommandInvokedEvent event) {
				DefaultMessage.COMMAND_MINIGAME_LIST_MESSAGE.reply(event, "minigames", Arrays.stream(GraphiteMinigame.values())
						.map(g -> g.getFriendlyName().getFor(event.getSender()))
						.collect(Collectors.joining(", ")));
			}

			@Override
			public List<OptionData> getOptions() {
				return Collections.emptyList();
			}

		})
		.setAllowPrivate(true)
		.setDescription(DefaultLocaleString.COMMAND_MINIGAME_LIST_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_MINIGAME_LIST_USAGE);

		addSubCommand(new Command(this, "stats") {

			@Override
			public void action(CommandInvokedEvent event) {
				GraphiteMinigameStats stats = Graphite.getMinigames().getStats();

				EmbedBuilder b = new EmbedBuilder();
				b.setDescription(DefaultLocaleString.COMMAND_MINIGAME_STATS_TITLE.getFor(event.getSender()));

				for(GraphiteMinigame mg : GraphiteMinigame.values()) {
					b.addField(mg.getFriendlyName().getFor(event.getSender()),
							DefaultLocaleString.COMMAND_MINIGAME_STATS_LINE.getFor(event.getSender(),
									"wins", "" + stats.getUserWins(mg, event.getAuthor()),
									"losses", "" + stats.getUserLosses(mg, event.getAuthor())), false);
				}

				event.reply(b.build());
			}

			@Override
			public List<OptionData> getOptions() {
				return Collections.emptyList();
			}

		})
		.setAllowPrivate(true)
		.setDescription(DefaultLocaleString.COMMAND_MINIGAME_STATS_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_MINIGAME_STATS_USAGE);
	}

	@SpecialSelfcheck(needsPermission = false)
	@Override
	public void action(CommandInvokedEvent event) {
		super.action(event);
	}

}

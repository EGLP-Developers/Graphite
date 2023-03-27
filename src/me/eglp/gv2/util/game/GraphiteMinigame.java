package me.eglp.gv2.util.game;

import java.util.Arrays;
import java.util.function.Function;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.base.GraphiteLocalizable;
import me.eglp.gv2.util.base.user.GraphiteUser;
import me.eglp.gv2.util.game.impl.battleships2.Battleships;
import me.eglp.gv2.util.game.impl.blackjack.Blackjack;
import me.eglp.gv2.util.game.impl.connectfour.ConnectFour;
import me.eglp.gv2.util.game.impl.minesweeper.Minesweeper;
import me.eglp.gv2.util.game.impl.poker.Poker;
import me.eglp.gv2.util.game.impl.rpg.RPG;
import me.eglp.gv2.util.game.impl.rps.RockPaperScissors;
import me.eglp.gv2.util.game.impl.tictactoe.TicTacToe;
import me.eglp.gv2.util.lang.DefaultLocaleString;
import me.eglp.gv2.util.lang.LocalizedString;
import me.eglp.gv2.util.lang.LocalizedStringImpl;

public enum GraphiteMinigame {

	MINESWEEPER(DefaultLocaleString.MINIGAME_MINESWEEPER_NAME, false, false, Minesweeper::new),
	CONNECT_FOUR(DefaultLocaleString.MINIGAME_CONNECT_FOUR_NAME, true, false, ConnectFour::new),
	TICTACTOE(DefaultLocaleString.MINIGAME_TICTACTOE_NAME, true, false, TicTacToe::new),
	BATTLESHIPS(DefaultLocaleString.MINIGAME_BATTLESHIPS_NAME, true, false, Battleships::new),
	GAME_RPG(new LocalizedStringImpl("RPG"), true, true, (u) -> {
		RPG.INSTANCE.addUser(u);
		return RPG.INSTANCE;
	}),
	BLACKJACK(DefaultLocaleString.MINIGAME_BLACKJACK_NAME, false, false, Blackjack::new),
	ROCK_PAPER_SCISSORS(DefaultLocaleString.MINIGAME_ROCK_PAPER_SCISSORS_NAME, true, false, RockPaperScissors::new),
	POKER(DefaultLocaleString.MINIGAME_POKER_NAME, true, false, Poker::new),
	;
	
	private LocalizedString friendlyName;
	
	private boolean
		isMultiplayer,
		isGlobal;
	
	private Function<GraphiteUser, MinigameInstance> instanceCreator;
	
	private GraphiteMinigame(LocalizedString friendlyName, boolean isMultiplayer, boolean isGlobal, Function<GraphiteUser, MinigameInstance> instanceCreator) {
		this.friendlyName = friendlyName;
		this.isMultiplayer = isMultiplayer;
		this.isGlobal = isGlobal;
		this.instanceCreator = instanceCreator;
	}
	
	public LocalizedString getFriendlyName() {
		return friendlyName;
	}
	
	public boolean isMultiplayer() {
		return isMultiplayer;
	}
	
	public boolean isGlobal() {
		return isGlobal;
	}
	
	public MinigameInstance startNewGame(GraphiteUser user) {
		MinigameInstance i = instanceCreator.apply(user);
		Graphite.getMinigames().setGame(user, i);
		return i;
	}
	
	public static GraphiteMinigame getByName(GraphiteLocalizable l, String name) {
		return Arrays.stream(values()).filter(g -> g.friendlyName.getFor(l).equalsIgnoreCase(name)).findFirst().orElse(null);
	}
	
}

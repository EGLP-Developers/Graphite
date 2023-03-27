package me.eglp.gv2.util.game.impl.connectfour;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.base.user.GraphiteUser;
import me.eglp.gv2.util.emote.JDAEmote;
import me.eglp.gv2.util.game.GraphiteMinigame;
import me.eglp.gv2.util.game.GraphiteMinigameMoney;
import me.eglp.gv2.util.game.MultiPlayerMinigameInstance;
import me.eglp.gv2.util.game.output.GameOutput;
import me.eglp.gv2.util.game.output.MessageOutput;
import me.eglp.gv2.util.game.output.renderer.IntMappingGetterRenderer;
import me.eglp.gv2.util.game.output.renderer.MessageGraphics;
import me.eglp.gv2.util.input.GraphiteInput;
import me.eglp.gv2.util.input.SelectInput;
import me.eglp.gv2.util.lang.DefaultMessage;

public class ConnectFour implements MultiPlayerMinigameInstance {
	
	public static final String DEFAULT_STAT_CATEGORY = "Connect Four 1v1";

	private GraphiteUser playerOne, playerTwo;
	private C4Board board;
	private SelectInput<Integer> moveInput;
	private MessageOutput message1, message2;
	private boolean running, stopped;
	
	public ConnectFour(GraphiteUser user) {
		this.playerOne = user;
		this.running = true;
		this.board = new C4Board();
	}
	
	@Override
	public List<GraphiteInput> getActiveInputs() {
		return Collections.singletonList(moveInput);
	}
	
	@Override
	public List<GameOutput> getActiveOutputs() {
		return Arrays.asList(message1, message2);
	}

	@Override
	public GraphiteMinigame getGame() {
		return GraphiteMinigame.CONNECT_FOUR;
	}
	
	@Override
	public boolean isJoinable() {
		return playerTwo == null && !stopped;
	}
	
	@Override
	public void addUser(GraphiteUser user) {
		if(playerTwo != null) throw new UnsupportedOperationException("Game is full");
		Graphite.getMinigames().unshareMinigame(this);
		playerTwo = user;
		message1 = new MessageOutput(playerOne.openPrivateChannel());
		message2 = new MessageOutput(playerTwo.openPrivateChannel());
		step(false);
	}
	
	private void step(boolean p2) {
		sendField(p2);
		moveInput = new SelectInput<Integer>(Arrays.asList(p2 ? playerTwo : playerOne), i -> {
			if(!board.drop(i, p2 ? 2 : 1)) {
				step(p2);
				return;
			}
			int w = board.getWinner();
			if(w != 0) {
				sendField(p2);
				if(w == -1) {
					DefaultMessage.MINIGAME_TIED.sendMessage(playerOne.openPrivateChannel());
					DefaultMessage.MINIGAME_TIED.sendMessage(playerTwo.openPrivateChannel());
					stop();
//					sendRematchInvite(playerOne, playerTwo); NONBETA: may cause problems
				}else {
					userWon(w == 1 ? playerOne : playerTwo, DEFAULT_STAT_CATEGORY, GraphiteMinigameMoney.CONNECTFOUR);
					userLost(w == 2 ? playerOne : playerTwo, DEFAULT_STAT_CATEGORY);
					DefaultMessage.MINIGAME_WON.sendMessage((w == 1 ? playerOne : playerTwo).openPrivateChannel(), "money", ""+GraphiteMinigameMoney.CONNECTFOUR.getMoney());
					DefaultMessage.MINIGAME_LOST.sendMessage((w == 1 ? playerTwo : playerOne).openPrivateChannel());
					stop();
				}
				return;
			}
			step(!p2);
		})
		.autoRemove(true)
		.removeMessage(false);
		
		for(int x = 0; x < board.getWidth(); x++) {
			moveInput.addOption(JDAEmote.getKeycapNumber(x + 1), x);
		}
		moveInput.applyReactions(message1.getMessage());
		moveInput.applyReactions(message2.getMessage());
		moveInput.apply(p2 ? message2.getMessage() : message1.getMessage());
	}
	
	private void sendField(boolean p2) {
		sendField(message1, false, p2);
		sendField(message2, true, p2);
	}
	
	private void sendField(MessageOutput m, boolean youArePlayerTwo, boolean p2) {
		MessageGraphics g = new MessageGraphics();
		g.setSymbol(youArePlayerTwo == p2 ? JDAEmote.WHITE_CHECK_MARK : JDAEmote.CLOCK1);
		g.point(0, 0);
		for(int x = 0; x < board.getWidth(); x++) {
			g.setSymbol(JDAEmote.getKeycapNumber(x + 1));
			g.point(x + 1, 0);
		}
		g.setSymbol(JDAEmote.ASTERISK);
		for(int y = 0; y < board.getHeight(); y++) g.point(0, y + 1);
		
		IntMappingGetterRenderer r = new IntMappingGetterRenderer(board.getWidth(), board.getHeight(), board::get);
		r.addMapping(0, JDAEmote.WHITE_CIRCLE);
		r.addMapping(1, JDAEmote.RED_CIRCLE);
		r.addMapping(2, JDAEmote.LARGE_BLUE_CIRCLE);
		MessageGraphics g2 = new MessageGraphics();
		r.render(g2);
		g.draw(1, 1, g2);
		
		m.update(g);
	}

	@Override
	public List<GraphiteUser> getPlayingUsers() {
		return Arrays.asList(playerOne, playerTwo);
	}

	@Override
	public void onUserLeave(GraphiteUser user) {
		if(!running) return;
		running = false;
		getPlayingUsers().stream().filter(p -> p != null && !p.equals(user)).forEach(p -> DefaultMessage.COMMAND_MINIGAME_LEAVE_MULTIPLAYER_AUTOLEAVE.sendMessage(p.openPrivateChannel(), "user", user.getName()));
		stop();
	}
	
	@Override
	public void stop() {
		MultiPlayerMinigameInstance.super.stop();
		stopped = true;
	}

}

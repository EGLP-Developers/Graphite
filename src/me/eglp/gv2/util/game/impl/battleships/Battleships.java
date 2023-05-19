package me.eglp.gv2.util.game.impl.battleships;

import java.util.Arrays;
import java.util.List;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.user.GraphiteUser;
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

public class Battleships implements MultiPlayerMinigameInstance {

	public static final String DEFAULT_STAT_CATEGORY = "Battleships 1v1";

	private GraphiteUser playerOne, playerTwo;
	private BSBoard board;
	private SelectInput<Integer> moveInput, pi1, pi2;
	private MessageOutput tracking1, primary1, tracking2, primary2, help1, help2;
	private Integer mx, my, px1, px2, py1, py2, rot1, rot2;
	private boolean running, help1Displayed, help2Displayed, stopped;

	public Battleships(GraphiteUser user) {
		this.playerOne = user;
		this.running = true;
		this.board = new BSBoard();
	}

	@Override
	public List<GraphiteInput> getActiveInputs() {
		return Arrays.asList(moveInput, pi1, pi2);
	}

	@Override
	public List<GameOutput> getActiveOutputs() {
		return Arrays.asList(tracking1, tracking2, primary1, primary2);
	}

	@Override
	public GraphiteMinigame getGame() {
		return GraphiteMinigame.BATTLESHIPS;
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
		primary1 = new MessageOutput(playerOne.openPrivateChannel());
		tracking1 = new MessageOutput(playerOne.openPrivateChannel());
		help1 = new MessageOutput(playerOne.openPrivateChannel());
		primary2 = new MessageOutput(playerTwo.openPrivateChannel());
		tracking2 = new MessageOutput(playerTwo.openPrivateChannel());
		help2 = new MessageOutput(playerTwo.openPrivateChannel());
		updateBoard(true, false);
		updateBoard(true, true);
	}

	private void step(boolean playerTwoMoves) {
		sendField(getIngameState(false, playerTwoMoves), getIngameState(true, playerTwoMoves), playerTwoMoves);
		moveInput = new SelectInput<Integer>(Arrays.asList(playerTwoMoves ? playerTwo : playerOne), it ->  {
			if(it == -3) {
				sendHelp(playerTwoMoves);
				step(playerTwoMoves);
				return;
			}

			if(it == -4) {
				mx = null;
				my = null;

				sendField(getIngameState(false, playerTwoMoves), getIngameState(true, playerTwoMoves), playerTwoMoves);
				step(playerTwoMoves);
				return;
			}

			if(mx == null) {
				mx = it;
				step(playerTwoMoves);
				return;
			}
			my = it;
			BSSubBoard otherBoard = playerTwoMoves ? board.getPlayer1() : board.getPlayer2();
			if(!otherBoard.fireAt(mx, my)) {
				mx = my = null;
				step(playerTwoMoves);
				return;
			}
			int w = board.getWinner();
			if(w != 0) {
				sendField(null, null, playerTwoMoves);
				userWon(w == 1 ? playerOne : playerTwo, DEFAULT_STAT_CATEGORY, GraphiteMinigameMoney.BATTLESHIPS);
				userLost(w == 2 ? playerOne : playerTwo, DEFAULT_STAT_CATEGORY);
				DefaultMessage.MINIGAME_WON.sendMessage((w == 1 ? playerOne : playerTwo).openPrivateChannel(), "money", ""+GraphiteMinigameMoney.BATTLESHIPS.getMoney());
				DefaultMessage.MINIGAME_LOST.sendMessage((w == 1 ? playerTwo : playerOne).openPrivateChannel());
				stop();
				return;
			}
			mx = my = null;
			step(!playerTwoMoves);
		})
		.autoRemove(true)
		.removeMessage(false);

		for(int x = 0; x < board.getWidth(); x++) {
			moveInput.addOption(JDAEmote.getKeycapNumber(x + 1), x);
		}
		moveInput.addOption(JDAEmote.WASTEBASKET, -4);
		moveInput.addOption(JDAEmote.QUESTION, -3);
		moveInput.applyReactions(primary1.getMessage());
		moveInput.applyReactions(primary2.getMessage());
		moveInput.apply(playerTwoMoves ? primary2.getMessage() : primary1.getMessage());
	}

	private void updateBoard(boolean sendField, boolean youArePlayerTwo) {
		if(sendField) sendField(getPregameState(false), getPregameState(true), null);
		SelectInput<Integer> pi = new SelectInput<Integer>(Arrays.asList(playerOne, playerTwo), it -> {
			if(it == -3) {
				sendHelp(youArePlayerTwo);
				updateBoard(false, youArePlayerTwo);
				return;
			}

			if(it == -4) {
				if(youArePlayerTwo) {
					px2 = null;
					py2 = null;
				}else {
					px1 = null;
					py1 = null;
				}

				sendField(youArePlayerTwo ? primary2 : primary1, getPregameState(youArePlayerTwo), youArePlayerTwo, null);
				updateBoard(false, youArePlayerTwo);
				return;
			}

			if((youArePlayerTwo ? px2 : px1) == null) {
				if(it < 0) { // Not a coordinate, ignore
					updateBoard(false, youArePlayerTwo);
					return;
				}
				if(youArePlayerTwo) px2 = it; else px1 = it;
				sendField(youArePlayerTwo ? primary2 : primary1, getPregameState(youArePlayerTwo), youArePlayerTwo, null);
				updateBoard(false, youArePlayerTwo);
				return;
			}

			if((youArePlayerTwo ? py2 : py1) == null) {
				if(it < 0) { // Not a coordinate, ignore
					updateBoard(false, youArePlayerTwo);
					return;
				}
				if(youArePlayerTwo) py2 = it; else py1 = it;
				sendField(youArePlayerTwo ? primary2 : primary1, getPregameState(youArePlayerTwo), youArePlayerTwo, null);
				updateBoard(false, youArePlayerTwo);
				return;
			}

			if(it >= 0) { // Not a rotation, ignore
				updateBoard(false, youArePlayerTwo);
				return;
			}

			if(youArePlayerTwo) rot2 = it == -1 ? 0 : 1; else rot1 = it == -1 ? 0 : 1;
			BSSubBoard yourBoard = youArePlayerTwo ? board.getPlayer2() : board.getPlayer1();
			if(!yourBoard.placeShip(yourBoard.getNextShipType(), youArePlayerTwo ? px2 : px1, youArePlayerTwo ? py2 : py1, youArePlayerTwo ? rot2 : rot1)) {
				if(youArePlayerTwo) px2 = py2 = rot2 = null; else px1 = py1 = rot1 = null;
				updateBoard(false, youArePlayerTwo);
				return;
			}
			if(youArePlayerTwo) px2 = py2 = rot2 = null; else px1 = py1 = rot1 = null;
			if(!yourBoard.hasFinishedPlacingShips()) {
				(youArePlayerTwo ? pi2 : pi1).remove();
				sendField(getPregameState(false), getPregameState(true), null);
				updateBoard(true, youArePlayerTwo);
			}else if((youArePlayerTwo ? board.getPlayer1() : board.getPlayer2()).hasFinishedPlacingShips()){
				sendField(getPregameState(false), getPregameState(true), null);
				step(false);
			}else {
				sendField(getPregameState(false), getPregameState(true), null);
			}
		})
		.autoRemove(true)
		.removeMessage(false);

		for(int x = 0; x < board.getWidth(); x++) {
			pi.addOption(JDAEmote.getKeycapNumber(x + 1), x);
		}
		pi.addOption(JDAEmote.ARROW_RIGHT, -1);
		pi.addOption(JDAEmote.ARROW_DOWN, -2);
		pi.addOption(JDAEmote.WASTEBASKET, -4);
		pi.addOption(JDAEmote.QUESTION, -3);
		if(youArePlayerTwo) pi2 = pi; else pi1 = pi;
		pi.apply(youArePlayerTwo ? primary2.getMessage() : primary1.getMessage());
	}

	private String getPregameState(boolean youArePlayerTwo) {
		Integer x = youArePlayerTwo ? px2 : px1, y = youArePlayerTwo ? py2 : py1;
		String it = String.format("(%s/%s)", x == null ? "?" : (x + 1), y == null ? "?" : (y + 1));
		BSSubBoard subBoard = youArePlayerTwo ? board.getPlayer2() : board.getPlayer1();
		if(!subBoard.hasFinishedPlacingShips()) {
			ShipType tp = subBoard.getNextShipType();
			it += String.format(" %s (Size: %s)", tp.getFriendlyName(), tp.getSize());
		}
		return (youArePlayerTwo ? board.getPlayer2() : board.getPlayer1()).hasFinishedPlacingShips() ? ((youArePlayerTwo ? board.getPlayer1() : board.getPlayer2()).hasFinishedPlacingShips() ? "Ready" : "Waiting for " + (youArePlayerTwo ? "Player 1" : "Player 2")) : "Place your ships " + it;
	}

	private String getIngameState(boolean youArePlayerTwo, boolean playerTwoMoves) {
		if(youArePlayerTwo == playerTwoMoves) {
			return String.format("(%s/%s) It's your turn. Shoot at the opponent's board and try to hit a ship", mx == null ? "?" : (mx + 1), my == null ? "?" : (my + 1));
		}else {
			return "It's your opponent's turn";
		}
	}

	private void sendHelp(boolean p2) {
		GraphiteUser p = (p2 ? playerTwo : playerOne);
		if(p2) {
			help2Displayed = !help2Displayed;
		}else {
			help1Displayed = !help1Displayed;
		}
		MessageOutput o = (p2 ? help2 : help1);
		if((p2 ? help2Displayed : help1Displayed)) {
			o.update(DefaultMessage.MINIGAME_BATTLESHIPS_HELP.createEmbed(p));
		}else {
			o.remove();
		}
	}

	private void sendField(String msgForPlayer1, String msgForPlayer2, Boolean playerTwoMoves) {
		sendTrackingField(tracking1, false, playerTwoMoves);
		sendField(primary1, msgForPlayer1, false, playerTwoMoves);
		sendTrackingField(tracking2, true, playerTwoMoves);
		sendField(primary2, msgForPlayer2, true, playerTwoMoves);
	}

	private void sendTrackingField(MessageOutput m, boolean youArePlayerTwo, Boolean playerTwoMoves) {
		MessageGraphics g = new MessageGraphics();
		g.setSymbol(JDAEmote.RED_CIRCLE);
		g.point(0, 0);
		for(int x = 0; x < board.getWidth(); x++) {
			g.setSymbol(JDAEmote.getKeycapNumber(x + 1));
			g.point(x + 1, 0);
		}
		for(int y = 0; y < board.getWidth(); y++) {
			g.setSymbol(JDAEmote.getKeycapNumber(y + 1));
			g.point(0, y + 1);
		}
		IntMappingGetterRenderer r = new IntMappingGetterRenderer(board.getWidth(), board.getHeight(), (x, y) -> {
			int v = (youArePlayerTwo ? board.getPlayer1() : board.getPlayer2()).get(x, y);
			if(playerTwoMoves != null && youArePlayerTwo == playerTwoMoves && mx != null && mx == x
					&& v != BSSubBoard.HIT_SHOT
					&& v != BSSubBoard.MISSED_SHOT
					&& v != BSSubBoard.SUNKEN_SHIP) {
				v = -1;
			}
			return v;
		});
		r.addMapping(BSSubBoard.HIT_SHOT, JDAEmote.RED_CIRCLE);
		r.addMapping(BSSubBoard.MISSED_SHOT, JDAEmote.WHITE_CIRCLE);
		r.addMapping(BSSubBoard.OCEAN, JDAEmote.OCEAN);
		r.addMapping(BSSubBoard.SHIP, JDAEmote.OCEAN);
		r.addMapping(BSSubBoard.SUNKEN_SHIP, JDAEmote.BOOM);
		r.addMapping(-1, JDAEmote.GREEN_SQUARE);
		MessageGraphics g2 = new MessageGraphics();
		r.render(g2);
		g.draw(1, 1, g2);
		m.update(g);
	}

	private void sendField(MessageOutput m, String msg, boolean youArePlayerTwo, Boolean playerTwoMoves) {
		MessageGraphics g = new MessageGraphics();
		if(msg != null && !msg.isEmpty()) {
			g.setSymbol("");
			g.fill(0, -1, board.getWidth() + 1, 1);
			g.setSymbol(msg);
			g.point(0, -1);
		}

		g.setSymbol(playerTwoMoves == null ? JDAEmote.RED_CIRCLE : (youArePlayerTwo == playerTwoMoves ? JDAEmote.WHITE_CHECK_MARK : JDAEmote.CLOCK1));
		g.point(0, 0);

		for(int x = 0; x < board.getWidth(); x++) {
			g.setSymbol(JDAEmote.getKeycapNumber(x + 1));
			g.point(x + 1, 0);
		}

		for(int y = 0; y < board.getWidth(); y++) {
			g.setSymbol(JDAEmote.getKeycapNumber(y + 1));
			g.point(0, y + 1);
		}

		IntMappingGetterRenderer r = new IntMappingGetterRenderer(board.getWidth(), board.getHeight(), (x, y) -> {
			int v = (youArePlayerTwo ? board.getPlayer2() : board.getPlayer1()).get(x, y);
			Integer px = youArePlayerTwo ? px2 : px1;
			Integer py = youArePlayerTwo ? py2 : py1;
			if(px != null && x == px && (py == null || y == py)
					&& v != BSSubBoard.SHIP) {
				v = -1;
			}
			return v;
		});

		r.addMapping(BSSubBoard.HIT_SHOT, JDAEmote.RED_CIRCLE);
		r.addMapping(BSSubBoard.MISSED_SHOT, JDAEmote.WHITE_CIRCLE);
		r.addMapping(BSSubBoard.OCEAN, JDAEmote.OCEAN);
		r.addMapping(BSSubBoard.SHIP, JDAEmote.BLACK_CIRCLE);
		r.addMapping(BSSubBoard.SUNKEN_SHIP, JDAEmote.BOOM);
		r.addMapping(-1, JDAEmote.GREEN_SQUARE);
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

package me.eglp.gv2.util.game.impl.battleships2;

import java.util.Arrays;
import java.util.List;

import me.eglp.gv2.user.GraphiteUser;
import me.eglp.gv2.util.game.AbstractMultiplayerMinigameInstance;
import me.eglp.gv2.util.game.GraphiteMinigame;
import me.eglp.gv2.util.game.output.GameOutput;
import me.eglp.gv2.util.input.GraphiteInput;
import me.eglp.gv2.util.lang.DefaultMessage;

public class Battleships extends AbstractMultiplayerMinigameInstance {

	public static final String DEFAULT_STAT_CATEGORY = "Battleships 1v1";

	private BSPlayer
		playerOne,
		playerTwo;

	private boolean running;

	private BSMoveState moveState;

	public Battleships(GraphiteUser user) {
		this.playerOne = new BSPlayer(this, user);
		this.running = true;
		this.moveState = null;
	}

	public void setMoveState(BSMoveState moveState) {
		this.moveState = moveState;
	}

	public BSMoveState getMoveState() {
		return moveState;
	}

	public boolean isTurnForPlayer(BSPlayer player) {
		return (moveState == BSMoveState.PLAYER_ONE) == (player == playerOne);
	}

	public BSPlayer getOpponent(BSPlayer player) {
		return player == playerOne ? playerTwo : playerOne;
	}

	@Override
	public List<GraphiteInput> getActiveInputs() {
		return Arrays.asList(playerOne.getInput(), playerTwo == null ? null : playerTwo.getInput());
	}

	@Override
	public List<GameOutput> getActiveOutputs() {
		return Arrays.asList(playerOne.getOutput(), playerTwo == null ? null : playerTwo.getOutput());
	}

	@Override
	public GraphiteMinigame getGame() {
		return GraphiteMinigame.BATTLESHIPS;
	}

	public void startGame() {
		moveState = BSMoveState.PLACE_SHIPS;
		playerOne.sendMessage();
		playerTwo.sendMessage();
	}

	@Override
	public void addUser(GraphiteUser user) {
		if(playerTwo != null) throw new UnsupportedOperationException("Game is full");
		this.playerTwo = new BSPlayer(this, user);
		startGame();
	}

	@Override
	public List<GraphiteUser> getPlayingUsers() {
		return Arrays.asList(playerOne.getUser(), playerTwo == null ? null : playerTwo.getUser());
	}

	@Override
	public void onUserLeave(GraphiteUser user) {
		endGame(DefaultMessage.COMMAND_MINIGAME_LEAVE_MULTIPLAYER_AUTOLEAVE, "user", user.getName());
	}

	@Override
	public boolean isJoinable() {
		return playerTwo == null && running;
	}

}

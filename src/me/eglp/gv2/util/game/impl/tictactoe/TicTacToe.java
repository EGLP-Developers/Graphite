package me.eglp.gv2.util.game.impl.tictactoe;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import me.eglp.gv2.user.GraphiteUser;
import me.eglp.gv2.util.game.AbstractMultiplayerMinigameInstance;
import me.eglp.gv2.util.game.GraphiteMinigame;
import me.eglp.gv2.util.game.GraphiteMinigameMoney;
import me.eglp.gv2.util.game.output.GameOutput;
import me.eglp.gv2.util.input.ButtonInputEvent;
import me.eglp.gv2.util.input.GraphiteInput;
import me.eglp.gv2.util.lang.DefaultMessage;

public class TicTacToe extends AbstractMultiplayerMinigameInstance {

	public static final String DEFAULT_STAT_CATEGORY = "TicTacToe 1v1";

	private static final Random RANDOM = new Random();

	private TicTacToePlayer
		playerOne,
		playerTwo;

	private TTTBoard board;

	private boolean running;

	private TTTMoveState moveState;

	public TicTacToe(GraphiteUser user) {
		this.playerOne = new TicTacToePlayer(this, user);
		this.running = true;
		this.moveState = null;
		this.board = new TTTBoard(3);
	}

	public void startGame() {
		this.moveState = RANDOM.nextBoolean() ? TTTMoveState.PLAYER_ONE : TTTMoveState.PLAYER_TWO;
		playerOne.updateMessage();
		playerTwo.updateMessage();
	}

	public TTTBoard getBoard() {
		return board;
	}

	public TTTMoveState getMoveState() {
		return moveState;
	}

	public boolean isTurnForPlayer(TicTacToePlayer player) {
		return (moveState == TTTMoveState.PLAYER_ONE) == (player == playerOne);
	}

	public TicTacToePlayer getOpponent(TicTacToePlayer player) {
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
		return GraphiteMinigame.TICTACTOE;
	}

	@Override
	public void addUser(GraphiteUser user) {
		if(playerTwo != null) throw new UnsupportedOperationException("Game is full");
		this.playerTwo = new TicTacToePlayer(this, user);
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

	public void onPlayerInput(TicTacToePlayer player, ButtonInputEvent<Integer> event) {
		boolean isPlayerOne = player == playerOne;
		if(isPlayerOne != (moveState == TTTMoveState.PLAYER_ONE)) {
			event.getJDAEvent().deferReply(true).setContent("It's currently the other player's turn").queue();
			return;
		}

		if(board.get(event.getItem()) != TTTBoard.EMPTY) {
			event.getJDAEvent().deferReply(true).setContent("That field is already full").queue();
			return;
		}

		moveState = isPlayerOne ? TTTMoveState.PLAYER_TWO : TTTMoveState.PLAYER_ONE;
		board.set(event.getItem(), isPlayerOne ? TTTBoard.X : TTTBoard.O);

		int winner = board.checkWinner();
		if(winner != TTTBoard.EMPTY) {
			if(winner == -1) {
				DefaultMessage.MINIGAME_TIED.sendMessage(playerOne.getUser().openPrivateChannel());
				DefaultMessage.MINIGAME_TIED.sendMessage(playerTwo.getUser().openPrivateChannel());
				stop();
			}else {
				userWon((winner == TTTBoard.X ? playerOne : playerTwo).getUser(), DEFAULT_STAT_CATEGORY, GraphiteMinigameMoney.TICTACTOE);
				userLost((winner == TTTBoard.O ? playerOne : playerTwo).getUser(), DEFAULT_STAT_CATEGORY);
				DefaultMessage.MINIGAME_WON.sendMessage((winner == TTTBoard.X ? playerOne : playerTwo).getUser().openPrivateChannel(), "money", ""+GraphiteMinigameMoney.TICTACTOE.getMoney());
				DefaultMessage.MINIGAME_LOST.sendMessage((winner == TTTBoard.X ? playerTwo : playerOne).getUser().openPrivateChannel());
				stop();
			}
		}else {
			playerOne.updateMessage();
			playerTwo.updateMessage();
		}
	}

}

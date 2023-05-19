package me.eglp.gv2.util.game.impl.rps;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.user.GraphiteUser;
import me.eglp.gv2.util.emote.JDAEmote;
import me.eglp.gv2.util.game.GraphiteMinigame;
import me.eglp.gv2.util.game.GraphiteMinigameMoney;
import me.eglp.gv2.util.game.MultiPlayerMinigameInstance;
import me.eglp.gv2.util.game.output.GameOutput;
import me.eglp.gv2.util.game.output.MessageOutput;
import me.eglp.gv2.util.input.GraphiteInput;
import me.eglp.gv2.util.input.SelectInput;
import me.eglp.gv2.util.lang.DefaultLocaleString;
import me.eglp.gv2.util.lang.DefaultMessage;

public class RockPaperScissors implements MultiPlayerMinigameInstance {

	private static final int
		ROCK = 1,
		PAPER = 2,
		SCISSORS = 3;

	private GraphiteUser playerOne, playerTwo;
	private boolean running, stopped;
	private SelectInput<Integer> moveInput1, moveInput2;
	private MessageOutput out1, out2;
	private int score1, score2;
	private int otherSelected;

	public RockPaperScissors(GraphiteUser user) {
		this.playerOne = user;
		this.running = true;
	}

	@Override
	public List<GraphiteInput> getActiveInputs() {
		return Arrays.asList(moveInput1, moveInput2);
	}

	@Override
	public List<GameOutput> getActiveOutputs() {
		return Arrays.asList(out1, out2);
	}

	@Override
	public GraphiteMinigame getGame() {
		return GraphiteMinigame.ROCK_PAPER_SCISSORS;
	}

	@Override
	public void addUser(GraphiteUser user) {
		if(playerTwo != null) throw new UnsupportedOperationException("Game is full");
		Graphite.getMinigames().unshareMinigame(this);
		this.playerTwo = user;

		setup(true);
		setup(false);
	}

	private void setup(boolean p1) {
		GraphiteUser user = p1 ? playerOne : playerTwo;

		MessageOutput out = new MessageOutput(user.openPrivateChannel());

		out.update(DefaultLocaleString.MINIGAME_ROCK_PAPER_SCISSORS_SELECT.getFor(user, "score_self", ""+(p1 ? score1 : score2), "score_other", ""+(p1 ? score2 : score1)));

		SelectInput<Integer> move = new SelectInput<Integer>(Collections.singletonList(user), it -> {
			if(otherSelected != 0) {
				int state = winStateAgainst(it, otherSelected);

				if(state == -1) {
					DefaultMessage.MINIGAME_TIED.sendMessage(playerOne.openPrivateChannel());
					DefaultMessage.MINIGAME_TIED.sendMessage(playerTwo.openPrivateChannel());
				}else {
					boolean playerOneWins = (p1 && state == 1) || (!p1 && state == 0);
					userWon(playerOneWins ? playerOne : playerTwo, "Rock Paper Scissors 1v1", GraphiteMinigameMoney.ROCK_PAPER_SCISSORS);
					userLost(playerOneWins ? playerTwo : playerOne, "Rock Paper Scissors 1v1");

					if(playerOneWins) {
						score1++;
					}else {
						score2++;
					}

					DefaultMessage.MINIGAME_WON.sendMessage((playerOneWins ? playerOne : playerTwo).openPrivateChannel(), "money", ""+GraphiteMinigameMoney.ROCK_PAPER_SCISSORS.getMoney());
					DefaultMessage.MINIGAME_LOST.sendMessage((playerOneWins ? playerTwo : playerOne).openPrivateChannel());
				}

//				stop(true);
//				sendRematchInvite(playerOne, playerTwo);
				otherSelected = 0;
				setup(true);
				setup(false);
			}else {
				otherSelected = it;
			}
		})
		.autoRemove(true)
		.removeMessage(false);

		move.addOption(JDAEmote.BRICKS, ROCK);
		move.addOption(JDAEmote.ROLL_OF_PAPER, PAPER);
		move.addOption(JDAEmote.SCISSORS, SCISSORS);
		move.apply(out.getMessage());

		if(p1) {
			out1 = out;
			moveInput1 = move;
		}else {
			out2 = out;
			moveInput2 = move;
		}
	}

	private int winStateAgainst(int a, int b) {
		if(a == b) return -1;
		switch(b) {
			case ROCK:
				return a == PAPER ? 1 : 0;
			case PAPER:
				return a == SCISSORS ? 1 : 0;
			case SCISSORS:
				return a == ROCK ? 1 : 0;
		}
		return -1; // Shouldn't ever happen
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
	public boolean isJoinable() {
		return playerTwo == null && !stopped;
	}

	@Override
	public void stop() {
		MultiPlayerMinigameInstance.super.stop();
		stopped = true;
	}

}

package me.eglp.gv2.util.game.impl.poker;

import java.util.ArrayList;
import java.util.List;

import me.eglp.gv2.user.GraphiteUser;
import me.eglp.gv2.util.game.impl.blackjack.PlayingCard;
import me.eglp.gv2.util.game.output.MessageOutput;
import me.eglp.gv2.util.input.GraphiteInput;

public class PokerPlayer {

	private Poker poker;
	private GraphiteUser user;

	private GraphiteInput
		moveInput,
		additionalMoveInput;

	private MessageOutput
		output,
		statusOutput,
		additionalOutput;

	private List<PlayingCard> hand;

	private int
		ownChips,
		totalBet;

	private Integer
		currentBet;

	private boolean
		isOut,
		spectatorCardsVisible;

	public PokerPlayer(Poker poker, GraphiteUser user) {
		this.poker = poker;
		this.user = user;
		this.output = new MessageOutput(user.openPrivateChannel());
		this.statusOutput = new MessageOutput(user.openPrivateChannel());
		this.hand = new ArrayList<>();
	}

	public Poker getPoker() {
		return poker;
	}

	public GraphiteUser getUser() {
		return user;
	}

	public void setMoveInput(GraphiteInput moveInput) {
		this.moveInput = moveInput;
	}

	public GraphiteInput getMoveInput() {
		return moveInput;
	}

	public void setAdditionalMoveInput(GraphiteInput additionalMoveInput) {
		this.additionalMoveInput = additionalMoveInput;
	}

	public GraphiteInput getAdditionalMoveInput() {
		return additionalMoveInput;
	}

	public MessageOutput getOutput() {
		return output;
	}

	public MessageOutput getStatusOutput() {
		return statusOutput;
	}

	public void setAdditionalOutput(MessageOutput additionalOutput) {
		this.additionalOutput = additionalOutput;
	}

	public MessageOutput getAdditionalOutput() {
		return additionalOutput;
	}

	public List<PlayingCard> getHand() {
		return hand;
	}

	public void setOwnChips(int ownChips) {
		this.ownChips = ownChips;
	}

	public int getOwnChips() {
		return ownChips;
	}

	public void setCurrentBet(Integer currentBet) {
		this.currentBet = currentBet;
	}

	public Integer getCurrentBet() {
		return currentBet;
	}

	public void setTotalBet(int totalBet) {
		this.totalBet = totalBet;
	}

	public int getTotalBet() {
		return totalBet;
	}

	public void setOut(boolean isOut) {
		this.isOut = isOut;
	}

	public boolean isOut() {
		return isOut;
	}

	public boolean isAllIn() {
		return (currentBet != null && currentBet == ownChips) || ownChips == 0;
	}

	public void setSpectatorCardsVisible(boolean spectatorCardsVisible) {
		this.spectatorCardsVisible = spectatorCardsVisible;
	}

	public boolean isSpectatorCardsVisible() {
		return spectatorCardsVisible;
	}

	@Override
	public String toString() {
		return "{P: " + user.getName()  + "}";
	}

}

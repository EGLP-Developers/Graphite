package me.eglp.gv2.util.game.impl.blackjack;

import java.util.Optional;

import me.eglp.gv2.util.emote.JDAEmote;

public enum CardRank {
	
	ACE,
	TWO(2),
	THREE(3),
	FOUR(4),
	FIVE(5),
	SIX(6),
	SEVEN(7),
	EIGHT(8),
	NINE(9),
	TEN(10),
	JACK,
	QUEEN,
	KING;
	
	private Optional<Integer> faceValue;
	
	private CardRank(int faceValue) {
		this.faceValue = Optional.of(faceValue);
	}
	
	private CardRank() {
		this.faceValue = Optional.empty();
	}
	
	public Optional<Integer> getFaceValue() {
		return faceValue;
	}
	
	public JDAEmote getEmote(boolean red) {
		return JDAEmote.valueOf(String.format("CARD_%s_%s", red ? "RED" : "BLACK", name()));
	}

}

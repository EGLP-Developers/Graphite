package me.eglp.gv2.util.game.impl.blackjack;

import me.eglp.gv2.util.emote.JDAEmote;

public enum CardSuit {
	
	HEART,
	TILE,
	CLOVER,
	SPADE;
	
	public JDAEmote getEmote() {
		return JDAEmote.valueOf(String.format("CARD_%s", name()));
	}
	
	public boolean isRed() {
		return this == HEART || this == TILE;
	}

}

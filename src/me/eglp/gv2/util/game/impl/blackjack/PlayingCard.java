package me.eglp.gv2.util.game.impl.blackjack;

public class PlayingCard {

	private CardSuit suit;
	private CardRank rank;
	
	public PlayingCard(CardSuit suit, CardRank rank) {
		this.suit = suit;
		this.rank = rank;
	}
	
	public CardSuit getSuit() {
		return suit;
	}
	
	public CardRank getRank() {
		return rank;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof PlayingCard)) return false;
		PlayingCard o = (PlayingCard) obj;
		return o.suit == suit && o.rank == rank;
	}
	
	@Override
	public String toString() {
		return "{C:" + rank + " OF " + suit + "}";
	}
	
}

package me.eglp.gv2.util.game.impl.poker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import me.eglp.gv2.util.game.impl.blackjack.CardRank;
import me.eglp.gv2.util.game.impl.blackjack.CardSuit;
import me.eglp.gv2.util.game.impl.blackjack.PlayingCard;

public enum PokerHand {
	
	ROYAL_FLUSH(5, cards -> {
		for(CardSuit suit : CardSuit.values()) {
			List<CardRank> ranks = Arrays.asList(CardRank.TEN, CardRank.JACK, CardRank.QUEEN, CardRank.KING, CardRank.ACE);
			
			List<PlayingCard> m = cards.stream()
					.filter(c -> ranks.contains(c.getRank()) && c.getSuit() == suit)
					.collect(Collectors.toList());
			if(m.size() == ranks.size()) return m;
		}
		
		return null;
	}),
	STRAIGHT_FLUSH(5, cards -> {
		for(CardSuit s : CardSuit.values()) {
			List<PlayingCard> currentStreak = new ArrayList<>();
			List<CardRank> ranks = new ArrayList<>(Arrays.asList(CardRank.values()));
			ranks.add(CardRank.ACE);
			for(CardRank r : ranks) {
				PlayingCard c;
				if((c = cards.stream().filter(cr -> cr.getRank() == r && cr.getSuit() == s).findFirst().orElse(null)) != null) {
					currentStreak.add(c);
					
					if(currentStreak.size() >= 5) return currentStreak;
				}else {
					currentStreak.clear();
				}
			}
		}
		return null;
	}),
	FOUR_OF_A_KIND(4, cards -> {
		for(CardRank r : CardRank.values()) {
			List<PlayingCard> m = cards.stream()
					.filter(c -> c.getRank() == r)
					.collect(Collectors.toList());
			if(m.size() >= 4) return m;
		}
		return null;
	}),
	FULL_HOUSE(5, cards -> {
		Map<CardRank, Integer> counts = new HashMap<>();
		for(CardRank r : CardRank.values()) {
			int num = (int) cards.stream()
					.filter(c -> c.getRank() == r)
					.count();
			counts.put(r, num);
		}
		
		CardRank triple = counts.entrySet().stream()
				.filter(e -> e.getValue() >= 3)
				.map(e -> e.getKey())
				.findFirst().orElse(null);
		
		if(triple == null) return null;
		
		
		CardRank pair;
		if((pair = counts.entrySet().stream()
				.filter(e -> e.getValue() >= 2 && e.getKey() != triple)
				.map(e -> e.getKey())
				.findFirst().orElse(null)) != null) {
			List<PlayingCard> m = cards.stream()
					.filter(c -> c.getRank() == triple)
					.limit(3)
					.collect(Collectors.toList());
			
			m.addAll(cards.stream()
					.filter(c -> c.getRank() == pair)
					.limit(2)
					.collect(Collectors.toList()));
			return m;
		}
		
		return null;
	}),
	FLUSH(5, cards -> {
		for(CardSuit s : CardSuit.values()) {
			List<PlayingCard> m = cards.stream()
					.filter(c -> c.getSuit() == s)
					.collect(Collectors.toList());
			if(m.size() >= 5) return m;
		}
		
		return null;
	}),
	STRAIGHT(5, cards -> {
		List<PlayingCard> currentStreak = new ArrayList<>();
		List<CardRank> ranks = new ArrayList<>(Arrays.asList(CardRank.values()));
		ranks.add(CardRank.ACE);
		for(CardRank r : ranks) {
			PlayingCard c;
			if((c = cards.stream().filter(cr -> cr.getRank() == r).findFirst().orElse(null)) != null) {
				currentStreak.add(c);
				
				if(currentStreak.size() >= 5) return currentStreak;
			}else {
				currentStreak.clear();
			}
		}
		return null;
	}),
	THREE_OF_A_KIND(3, cards -> {
		for(CardRank r : CardRank.values()) {
			List<PlayingCard> m = cards.stream()
					.filter(c -> c.getRank() == r)
					.collect(Collectors.toList());
			if(m.size() >= 3) return m;
		}
		return null;
	}),
	TWO_PAIR(4, cards -> {
		Map<CardRank, Integer> counts = new HashMap<>();
		for(CardRank r : CardRank.values()) {
			int num = (int) cards.stream()
					.filter(c -> c.getRank() == r)
					.count();
			counts.put(r, num);
		}
		
		if(counts.values().stream()
				.filter(v -> v >= 2)
				.count() >= 2) {
			List<PlayingCard> m = new ArrayList<>();
			for(CardRank r : CardRank.values()) {
				if(counts.get(r) >= 2) {
					cards.stream()
						.filter(c -> c.getRank() == r)
						.limit(2)
						.forEach(m::add);
				}
			}
			return m;
		}else {
			return null;
		}
	}),
	ONE_PAIR(2, cards -> {
		for(CardRank r : CardRank.values()) {
			List<PlayingCard> m = cards.stream()
					.filter(c -> c.getRank() == r)
					.collect(Collectors.toList());
			if(m.size() >= 2) return m;
		}
		return null;
	});
	
	private int requiredCards;
	private Function<List<PlayingCard>, List<PlayingCard>> check;
	
	private PokerHand(int requiredCards, Function<List<PlayingCard>, List<PlayingCard>> check) {
		this.requiredCards = requiredCards;
		this.check = check;
	}
	
	public int getRequiredCards() {
		return requiredCards;
	}
	
	public List<PlayingCard> check(List<PlayingCard> cards) {
		return check.apply(cards);
	}

}

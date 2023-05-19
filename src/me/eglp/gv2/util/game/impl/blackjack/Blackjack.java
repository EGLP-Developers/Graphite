package me.eglp.gv2.util.game.impl.blackjack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.user.GraphiteUser;
import me.eglp.gv2.util.emote.JDAEmote;
import me.eglp.gv2.util.game.GraphiteMinigame;
import me.eglp.gv2.util.game.SinglePlayerMinigameInstance;
import me.eglp.gv2.util.game.output.GameOutput;
import me.eglp.gv2.util.game.output.MessageOutput;
import me.eglp.gv2.util.game.output.renderer.MessageGraphics;
import me.eglp.gv2.util.input.GraphiteInput;
import me.eglp.gv2.util.input.MessageInput;
import me.eglp.gv2.util.input.SelectInput;
import me.eglp.gv2.util.lang.DefaultLocaleString;
import me.eglp.gv2.util.lang.DefaultMessage;

public class Blackjack implements SinglePlayerMinigameInstance {

	// TODO: double down (coming soon-ish(TM))

	private static final int
		BLACKJACK = -1,
		TRIPLE_SEVEN = -2,
		STATE_WIN = 1,
		STATE_LOSS = 0,
		STATE_TIE = -1,
		MAX_BET = 500,
		MIN_BET = 1;

	private GraphiteUser user;
	private MessageInput betInput;
	private MessageOutput betOutput;
	private SelectInput<Integer> moveInput;
	private MessageOutput msg;
	private SelectInput<Integer> splitInput;
	private MessageOutput splitMsg;
	private int bet;

	private List<PlayingCard> cards;
	private List<PlayingCard> playingHand;
	private List<PlayingCard> firstHand;
	private List<PlayingCard> secondHand;
	private List<PlayingCard> dealerHand;
	private boolean gameOver;

	public Blackjack(GraphiteUser user) {
		this.user = user;
		this.msg = new MessageOutput(user.openPrivateChannel());
		this.cards = new ArrayList<>();
		this.firstHand = new ArrayList<>();
		this.dealerHand = new ArrayList<>();

		this.playingHand = firstHand;

		betOutput = new MessageOutput(user.openPrivateChannel());
		betOutput.update(DefaultLocaleString.MINIGAME_BLACKJACK_BET.getFor(user));
		betInput = new MessageInput(Arrays.asList(user), false, msg -> {
			try {
				int amount = Integer.parseInt(msg);

				if(amount < MIN_BET || amount > MAX_BET) {
					DefaultMessage.MINIGAME_BLACKJACK_BET_LIMIT.sendMessage(user.openPrivateChannel(), "min_bet", ""+MIN_BET, "max_bet", ""+MAX_BET);
					stop();
					return;
				}

				bet = amount;
			}catch(NumberFormatException e) {
				betOutput.update(DefaultLocaleString.MINIGAME_BLACKJACK_INVALID_BET.getFor(user));
				return;
			}

			betInput.remove();
			betOutput.remove();

			betInput = null;
			betOutput = null;

			for(CardSuit type : CardSuit.values()) {
				for(CardRank n : CardRank.values()) cards.add(new PlayingCard(type, n));
			}

			Collections.shuffle(cards);

			dealerHand.add(cards.remove(0));
			dealerHand.add(cards.remove(0));

			firstHand.add(cards.remove(0));
			firstHand.add(cards.remove(0));

			step();

			if(firstHand.get(0).getRank() == firstHand.get(1).getRank()) {
				splitMsg = new MessageOutput(user.openPrivateChannel());
				splitMsg.update(DefaultLocaleString.MINIGAME_BLACKJACK_SPLIT.getFor(user));
				splitInput = new SelectInput<Integer>(Collections.singletonList(user), it -> {
					splitMsg.remove();

					splitInput = null;
					splitMsg = null;

					if(it != 0) return;

					secondHand = new ArrayList<>();
					secondHand.add(firstHand.remove(0));

					firstHand.add(cards.remove(0));
					secondHand.add(cards.remove(0));

					sendState(false);
				})
				.autoRemove(true)
				.removeMessage(false);

				splitInput.addOption(JDAEmote.LEFT_RIGHT_ARROW, 0);
				splitInput.addOption(JDAEmote.X, 1);
				splitInput.apply(splitMsg.getMessage());
			}
		});

		betInput.apply(user.openPrivateChannel());
	}

	private void step() {
		sendState(false);
		SelectInput<Integer> s = new SelectInput<Integer>(Collections.singletonList(user), it ->  {
			if(splitInput != null) splitInput.remove();
			if(splitMsg != null) splitMsg.remove();
			switch(it) {
				case 0: // hit
				{
					playingHand.add(cards.remove(0));

					if(countHand(playingHand) == BLACKJACK || countHand(playingHand) == TRIPLE_SEVEN) {
						playDealer();
						stop(false);
						return;
					}

					if(countHand(playingHand) > 21) {
						if(secondHand != null && playingHand != secondHand) {
							playingHand = secondHand;
							step();
							return;
						}

						playDealer();
						stop(false);
						return;
					}

					break;
				}
				case 1: // stand
				{
					if(secondHand != null && playingHand != secondHand) {
						playingHand = secondHand;
						step();
						return;
					}

					playDealer();
					stop(false);
					return;
				}
				case 2: // NONBETA (double down, surrender?, bust?)
				{

				}
			}
			step();
		})
		.autoRemove(true)
		.removeMessage(false);

		s.addOption(JDAEmote.OK, 0);
		s.addOption(JDAEmote.X, 1);
		if(!gameOver) s.apply(msg.getMessage());
		moveInput = s;
	}

	private void playDealer() {
		gameOver = true;
		while(countHand(dealerHand) <= 16) {
			dealerHand.add(cards.remove(0));
		}

		sendState(true);

		int firstHandState = winState(firstHand);

		switch(firstHandState) {
			case STATE_WIN:
				int win = (countHand(firstHand) == BLACKJACK || countHand(firstHand) == TRIPLE_SEVEN) ? 2 * bet : bet;
				userWon(getPlayingUser(), "Blackjack", win);
				DefaultMessage.MINIGAME_BLACKJACK_FIRST_HAND_WON.sendMessage(user.openPrivateChannel(), "money", ""+win);
				break;
			case STATE_LOSS:
				userLost(getPlayingUser(), "Blackjack");
				DefaultMessage.MINIGAME_BLACKJACK_FIRST_HAND_LOST.sendMessage(user.openPrivateChannel(), "money", ""+bet);
				break;
			case STATE_TIE:
				DefaultMessage.MINIGAME_BLACKJACK_FIRST_HAND_TIED.sendMessage(user.openPrivateChannel(), "money", ""+bet);
				break;
		}

		if(secondHand != null) {
			int secondHandState = winState(secondHand);

			switch(secondHandState) {
				case STATE_WIN:
					int win = (countHand(firstHand) == BLACKJACK || countHand(firstHand) == TRIPLE_SEVEN) ? 2 * bet : bet;
					userWon(getPlayingUser(), "Blackjack", win);
					DefaultMessage.MINIGAME_BLACKJACK_SECOND_HAND_WON.sendMessage(user.openPrivateChannel(), "money", ""+win);
					break;
				case STATE_LOSS:
					userLost(getPlayingUser(), "Blackjack");
					DefaultMessage.MINIGAME_BLACKJACK_SECOND_HAND_LOST.sendMessage(user.openPrivateChannel(), "money", ""+bet);
					break;
				case STATE_TIE:
					DefaultMessage.MINIGAME_BLACKJACK_SECOND_HAND_TIED.sendMessage(user.openPrivateChannel(), "money", ""+bet);
					break;
			}
		}

		sendReplayMessage();
	}

	private int winState(List<PlayingCard> playingHand) {
		int player = countHand(playingHand);

		if(player > 21) return STATE_LOSS;

		int dealer = countHand(dealerHand);

		if(player == dealer) {
			return STATE_TIE;
		}else if(dealer > 21 || player > dealer || player == BLACKJACK || player == TRIPLE_SEVEN)  {
			return STATE_WIN;
		}else {
			return STATE_LOSS;
		}
	}

	private int countHand(List<PlayingCard> handCards) {
		int total = 0, numAces = 0;

		for(PlayingCard c : handCards) {
			switch(c.getRank()) {
				case ACE:
				{
					numAces++;
					break;
				}
				case JACK:
				case KING:
				case QUEEN:
				{
					total += 10;
					break;
				}
				default:
				{
					total += c.getRank().getFaceValue().get(); // 2 - 10
					break;
				}
			}
		}

		while(numAces-- > 0) {
			if(total + numAces + 11 <= 21) {
				total += 11;
				continue;
			}
			total++;
		}

		if(total == 21 && handCards.size() == 2) return BLACKJACK;
		if(total == 21 && handCards.size() == 3
				&& handCards.get(0).getRank() == CardRank.SEVEN
				&& handCards.get(1).getRank() == CardRank.SEVEN
				&& handCards.get(2).getRank() == CardRank.SEVEN) return TRIPLE_SEVEN;

		return total;
	}

	private void sendState(boolean dealerRevealed) {
		MessageGraphics g = new MessageGraphics();

		g.setSymbol("Dealer's hand:" + (dealerRevealed && gameOver ? " " + friendlyFormat(countHand(dealerHand)) : ""));
		g.point(0, 0);

		for(int i = 0; i < dealerHand.size(); i++) {
			PlayingCard c = dealerHand.get(i);

			boolean hidden = i > 0 && !dealerRevealed;

			g.setSymbol(hidden ? JDAEmote.CARD_BACK_TOP : c.getRank().getEmote(c.getSuit().isRed()));
			g.point(i, 1);
			g.setSymbol(hidden ? JDAEmote.CARD_BACK_BOTTOM : c.getSuit().getEmote());
			g.point(i, 2);
		}

		g.setSymbol("Your hand(s):" + (gameOver ? " " + friendlyFormat(countHand(firstHand)) : "") + (gameOver && secondHand != null ? " and " + friendlyFormat(countHand(secondHand)) : ""));
		g.point(0, 4);

		int i = 0;
		for(PlayingCard c : firstHand) {
			g.setSymbol(c.getRank().getEmote(c.getSuit().isRed()));
			g.point(i, 5);
			g.setSymbol(c.getSuit().getEmote());
			g.point(i, 6);

			if(!gameOver && secondHand != null) {
				if(playingHand == firstHand) {
					g.setSymbol(JDAEmote.WHITE_LARGE_SQUARE);
					g.point(i, 7);
				}else {
					g.setSymbol(JDAEmote.BLACK_LARGE_SQUARE);
					g.point(i, 7);
				}
			}

			i++;
		}

		if(secondHand != null) {
			g.setSymbol(JDAEmote.BLACK_LARGE_SQUARE);
			g.point(i, 5);
			g.point(i, 6);
			if(!gameOver) g.point(i, 7);

			i++;

			for(PlayingCard c : secondHand) {
				g.setSymbol(c.getRank().getEmote(c.getSuit().isRed()));
				g.point(i, 5);
				g.setSymbol(c.getSuit().getEmote());
				g.point(i, 6);

				if(!gameOver) {
					if(playingHand == secondHand) {
						g.setSymbol(JDAEmote.WHITE_LARGE_SQUARE);
						g.point(i,  7);
					}else {
						g.setSymbol(JDAEmote.BLACK_LARGE_SQUARE);
						g.point(i, 7);
					}
				}

				i++;
			}
		}

		msg.update(g.render(false));
	}

	private String friendlyFormat(int handScore) {
		switch(handScore) {
			case BLACKJACK:
				return "Blackjack!";
			case TRIPLE_SEVEN:
				return "Triple Seven";
			default:
				return String.valueOf(handScore);
		}
	}

	@Override
	public List<GraphiteInput> getActiveInputs() {
		return Arrays.asList(moveInput, splitInput, betInput);
	}

	@Override
	public List<GameOutput> getActiveOutputs() {
		return Arrays.asList(msg, betOutput);
	}

	@Override
	public GraphiteMinigame getGame() {
		return GraphiteMinigame.BLACKJACK;
	}

	@Override
	public GraphiteUser getPlayingUser() {
		return user;
	}

	@Override
	public void onUserLeave(GraphiteUser user) {
		stop(!gameOver);
	}

}

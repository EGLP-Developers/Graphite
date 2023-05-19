package me.eglp.gv2.util.game.impl.poker;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.user.GraphiteUser;
import me.eglp.gv2.util.emote.JDAEmote;
import me.eglp.gv2.util.game.GraphiteMinigame;
import me.eglp.gv2.util.game.GraphiteMinigameMoney;
import me.eglp.gv2.util.game.MultiPlayerMinigameInstance;
import me.eglp.gv2.util.game.impl.blackjack.CardRank;
import me.eglp.gv2.util.game.impl.blackjack.CardSuit;
import me.eglp.gv2.util.game.impl.blackjack.PlayingCard;
import me.eglp.gv2.util.game.output.GameOutput;
import me.eglp.gv2.util.game.output.MessageOutput;
import me.eglp.gv2.util.game.output.renderer.MessageGraphics;
import me.eglp.gv2.util.input.GraphiteInput;
import me.eglp.gv2.util.input.MessageInput;
import me.eglp.gv2.util.input.SelectInput;
import me.eglp.gv2.util.lang.DefaultMessage;
import net.dv8tion.jda.api.EmbedBuilder;

public class Poker implements MultiPlayerMinigameInstance {

	private static final int[][] CARD_COORDS = {
		{3, 7},
		{1, 4},
		{3, 1},
		{10, 1},
		{12, 4},
		{10, 7}
	};

	private static final int[][] PLAYER_HIGHLIGHT_COORDS = {
		{3, 9, 4, 9},
		{0, 4, 0, 5},
		{3, 0, 4, 0},
		{10, 0, 11, 0},
		{14, 4, 14, 5},
		{10, 9, 10, 9}
	};

	private static final int[][] DEALER_TOKEN_COORDS = {
		{5, 7},
		{1, 6},
		{5, 2},
		{9, 2},
		{13, 6},
		{9, 7}
	};

	private static final int[][] PLAYER_NUMBER_COORDS = {
		{2, 7},
		{1, 3},
		{2, 2},
		{12, 2},
		{13, 3},
		{12, 7}
	};

	private static final int
		STAGE_WAITING = -1,
		STAGE_PREFLOP = 0,
		STAGE_FLOP = 1,
		STAGE_TURN_CARD = 2,
		STAGE_RIVER_CARD = 3,
		STAGE_SHOWDOWN = 4,
		STAGE_STOPPED = 5;

	private static final Map<JDAEmote, Integer> CHIPS = new HashMap<>();

	static {
		CHIPS.put(JDAEmote.POKER_CHIP_1, 1);
		CHIPS.put(JDAEmote.POKER_CHIP_5, 5);
		CHIPS.put(JDAEmote.POKER_CHIP_10, 10);
		CHIPS.put(JDAEmote.POKER_CHIP_20, 20);
		CHIPS.put(JDAEmote.POKER_CHIP_25, 25);
		CHIPS.put(JDAEmote.POKER_CHIP_100, 100);
		CHIPS.put(JDAEmote.POKER_CHIP_500, 500);
	}

	private GraphiteUser host;

	private List<PokerPlayer>
		players,
		spectators;

	private PokerPlayer
		hostPlayer,
		dealerPlayer,
		currentPlayer;

	private int blindState = 0;

	private int
		smallBlind = 5,
		bigBlind = 10;

	private int stage;

	private List<PlayingCard>
		table,
		cardStack;

	public Poker(GraphiteUser user) {
		this.host = user;
		this.hostPlayer = new PokerPlayer(this, host);
		this.players = new ArrayList<>();
		this.players.add(hostPlayer);
		this.spectators = new ArrayList<>();
		this.stage = STAGE_WAITING;

		initGame();

		hostPlayer.getOutput().update("Press :white_check_mark: to start the game.\nCurrent players:\n- " + user.getName());
		SelectInput<Integer> s = new SelectInput<Integer>(Collections.singletonList(host), it -> {
			if(players.size() < 2) {
				hostPlayer.getOutput().update("If you want to play alone, try a singleplayer game"); // TODO: overrides player msg
				return;
			}

			stage = STAGE_PREFLOP;
			hostPlayer.getOutput().remove();
			hostPlayer.getOutput().update("Enter the amount of starting chips everyone should get (100 - 10000):");
			Graphite.getMinigames().unshareMinigame(this);
			hostPlayer.getMoveInput().remove();
			hostPlayer.setMoveInput(null);

			MessageInput in = new MessageInput(Collections.singletonList(hostPlayer.getUser()), false, txt -> {
				int startingChips;
				try {
					startingChips = Integer.parseInt(txt);
				}catch(NumberFormatException e) {
					hostPlayer.getOutput().update("You need to enter a valid number between `100` and `10000`");
					return;
				}

				if(startingChips < 100 || startingChips > 10000) {
					hostPlayer.getOutput().update("You need to enter a valid number between `100` and `10000`");
					return;
				}

				hostPlayer.getMoveInput().remove();
				hostPlayer.getOutput().remove();

				for(PokerPlayer pl : players) {
					pl.getHand().add(cardStack.remove(0)); // Give everyone 2 cards
					pl.getHand().add(cardStack.remove(0));
					pl.setOwnChips(startingChips); // Give everyone starting chips
				}

				dealerPlayer = players.get(new Random().nextInt(players.size()));

				step();
			});
			in.apply(hostPlayer.getUser().openPrivateChannel());
			hostPlayer.setMoveInput(in);
		})
		.autoRemove(false)
		.removeMessage(false);

		s.addOption(JDAEmote.WHITE_CHECK_MARK, 0);
		s.apply(hostPlayer.getOutput().getMessage());
		hostPlayer.setMoveInput(s);
	}

	private void initGame() {
		this.table = new ArrayList<>();
		this.cardStack = new ArrayList<>();

		for(CardSuit s : CardSuit.values()) {
			for(CardRank r : CardRank.values()) cardStack.add(new PlayingCard(s, r));
		}

		Collections.shuffle(cardStack);
	}

	public MessageGraphics renderTable(PokerPlayer forPlayer) {
		MessageGraphics gr = new MessageGraphics();
		gr.setSymbol(JDAEmote.BLACK_LARGE_SQUARE);
		gr.fill(0, 0, 15, 10);

		for(int i = 0; i < players.size(); i++) {
			int[] coords = CARD_COORDS[i];
			PokerPlayer pl = players.get(i);
			if(pl == forPlayer || stage == STAGE_SHOWDOWN || (spectators.contains(forPlayer) && forPlayer.isSpectatorCardsVisible())) {
				renderCard(gr, coords[0], coords[1], pl.getHand().get(0));
				renderCard(gr, coords[0] + 1, coords[1], pl.getHand().get(1));
			}else {
				renderHiddenCard(gr, coords[0], coords[1]);
				renderHiddenCard(gr, coords[0] + 1, coords[1]);
			}

			if(pl == currentPlayer) {
				gr.setSymbol(JDAEmote.GREEN_SQUARE);
				int[] pos = PLAYER_HIGHLIGHT_COORDS[i];
				gr.point(pos[0], pos[1]);
				gr.point(pos[2], pos[3]);
			}

			if(pl.isOut()) {
				gr.setSymbol(JDAEmote.POKER_FOLD);
				int[] pos = PLAYER_HIGHLIGHT_COORDS[i];
				gr.point(pos[0], pos[1]);
				gr.point(pos[2], pos[3]);
			}

			if(pl == dealerPlayer) {
				gr.setSymbol(JDAEmote.POKER_DEALER);
				int[] pos = DEALER_TOKEN_COORDS[i];
				gr.point(pos[0], pos[1]);
			}

			gr.setSymbol(JDAEmote.getBlackKeycapNumber(i + 1));
			int[] pos = PLAYER_NUMBER_COORDS[i];
			gr.point(pos[0], pos[1]);
		}

		for(int i = 0; i < table.size(); i++) {
			PlayingCard c = table.get(i);
			renderCard(gr, 5 + i, 4, c);
		}

		EmbedBuilder eb = new EmbedBuilder();
		eb.setColor(Color.DARK_GRAY);

		eb.setTitle("Your chips: " + forPlayer.getOwnChips());
		eb.setDescription("Current bets:");

		for(int i = 0; i < players.size(); i++) {
			PokerPlayer pl = players.get(i);

			String fieldTitle = (i + 1) + ") " + pl.getUser().getName() + " | " + pl.getOwnChips();

			if(pl.isOut()) {
				eb.addField(fieldTitle, "**Folded**" , false);
				continue;
			}

			if(pl.isAllIn()) {
				eb.addField(fieldTitle, "All in" + (pl.getCurrentBet() != null ? " | " + asChipString(pl.getCurrentBet()) + " (= " + pl.getCurrentBet() + ")" : ""), false);
				continue;
			}

			if(pl.getCurrentBet() == null) {
				eb.addField(fieldTitle, "No bet set", false);
				continue;
			}

			eb.addField(fieldTitle, (pl.getCurrentBet() == 0 ? "Checked" : asChipString(pl.getCurrentBet()) + " (" + pl.getCurrentBet() + ")"), false);
		}

		eb.addField("Total current pot", "**" + getTotalPot() + "**", false);

		forPlayer.getStatusOutput().update(eb.build());

		return gr;
	}

	private void renderCard(MessageGraphics g, int x, int y, PlayingCard card) {
		g.setSymbol(card.getRank().getEmote(card.getSuit().isRed()));
		g.point(x, y);
		g.setSymbol(card.getSuit().getEmote());
		g.point(x, y + 1);
	}

	private void renderHiddenCard(MessageGraphics g, int x, int y) {
		g.setSymbol(JDAEmote.CARD_BACK_TOP);
		g.point(x, y);
		g.setSymbol(JDAEmote.CARD_BACK_BOTTOM);
		g.point(x, y + 1);
	}

	private void bet() {
		SelectInput<Integer> moveInput = new SelectInput<Integer>(Collections.singletonList(currentPlayer.getUser()), it -> {
			switch(it) {
				case 0: // Fold
					if(blindState < 2) return;
					if(currentPlayer.getAdditionalMoveInput() != null) {
						currentPlayer.getAdditionalMoveInput().remove();
						currentPlayer.getAdditionalOutput().remove();
						currentPlayer.setAdditionalMoveInput(null);
						currentPlayer.setAdditionalOutput(null);
					}

					currentPlayer.setOut(true);
					currentPlayer.getMoveInput().remove();

					step();
					break;
				case 1: // Check/Hold
					if(currentPlayer.getAdditionalMoveInput() != null) {
						currentPlayer.getAdditionalMoveInput().remove();
						currentPlayer.getAdditionalOutput().remove();
						currentPlayer.setAdditionalMoveInput(null);
						currentPlayer.setAdditionalOutput(null);
					}

					if(blindState < 2) {
						currentPlayer.setCurrentBet(Math.min(currentPlayer.getOwnChips(), blindState == 0 ? smallBlind : bigBlind));
						currentPlayer.getMoveInput().remove();
						blindState++;
						step();
						return;
					}

					currentPlayer.setCurrentBet(Math.min(currentPlayer.getOwnChips(), getCurrentMaxBet()));
					currentPlayer.getMoveInput().remove();

					step();
					break;
				case 2: // Bet
					if(currentPlayer.getAdditionalMoveInput() != null) {
						currentPlayer.getAdditionalMoveInput().remove();
						currentPlayer.getAdditionalOutput().remove();
						currentPlayer.setAdditionalMoveInput(null);
						currentPlayer.setAdditionalOutput(null);
						return;
					}

					MessageOutput out = new MessageOutput(currentPlayer.getUser().openPrivateChannel());
					out.update("Place bet:" + (currentPlayer.getCurrentBet() != null && currentPlayer.getCurrentBet() > 0 ? " " + currentPlayer.getCurrentBet() : ""));
					currentPlayer.setAdditionalOutput(out);
					SelectInput<Integer> betInput = new SelectInput<Integer>(Collections.singletonList(currentPlayer.getUser()), b -> {
						if(b == -1) { // Ok
							int playerBet = currentPlayer.getCurrentBet() == null ? 0 : currentPlayer.getCurrentBet();

							if(blindState == 0 && playerBet != smallBlind && (currentPlayer.getOwnChips() >= smallBlind || !currentPlayer.isAllIn())) {
								out.update("Place bet: You need to place the small blind (" + smallBlind + " chips)");
								return;
							}

							if(blindState == 1 && playerBet < bigBlind && (currentPlayer.getOwnChips() >= bigBlind || !currentPlayer.isAllIn())) {
								out.update("Place bet: You need to at least place the big blind (" + bigBlind + " chips)");
								return;
							}

							if((currentPlayer.getCurrentBet() == 0 || currentPlayer.getCurrentBet() < getCurrentMaxBet()) && !currentPlayer.isAllIn()) {
								out.update("Place bet: Too little. Must be more than the current max bet of " + getCurrentMaxBet());
								return;
							}

							if(currentPlayer.getCurrentBet() > currentPlayer.getOwnChips()) {
								out.update("Place bet: You can't afford that bet");
								return;
							}

							currentPlayer.getMoveInput().remove();
							currentPlayer.getAdditionalMoveInput().remove();
							currentPlayer.setAdditionalMoveInput(null);
							out.remove();
							currentPlayer.setAdditionalOutput(null);
							if(blindState < 2) blindState++;
							step();
							return;
						}else if(b == -2) { // Reset
							currentPlayer.setCurrentBet(0);
							out.update("Place bet: " + currentPlayer.getCurrentBet());
							return;
						}else if(b == -3) { // All in
							currentPlayer.setCurrentBet(currentPlayer.getOwnChips());
							out.update("Place bet: All in! (= " + currentPlayer.getCurrentBet() + ")");
							return;
						}

						if(currentPlayer.getCurrentBet() == null) {
							currentPlayer.setCurrentBet(b);
						}else {
							currentPlayer.setCurrentBet(currentPlayer.getCurrentBet() + b);
						}
						out.update("Place bet: " + currentPlayer.getCurrentBet());
					})
					.autoRemove(false)
					.removeMessage(false);

					betInput.addOption(JDAEmote.POKER_CHIP_1, 1);
					if(currentPlayer.getOwnChips() >= 5) betInput.addOption(JDAEmote.POKER_CHIP_5, 5);
					if(currentPlayer.getOwnChips() >= 10) betInput.addOption(JDAEmote.POKER_CHIP_10, 10);
					if(currentPlayer.getOwnChips() >= 20) betInput.addOption(JDAEmote.POKER_CHIP_20, 20);
					if(currentPlayer.getOwnChips() >= 25) betInput.addOption(JDAEmote.POKER_CHIP_25, 25);
					if(currentPlayer.getOwnChips() >= 100) betInput.addOption(JDAEmote.POKER_CHIP_100, 100);
					if(currentPlayer.getOwnChips() >= 500) betInput.addOption(JDAEmote.POKER_CHIP_500, 500);
					betInput.addOption(JDAEmote.POKER_ALL_IN, -3);
					betInput.addOption(JDAEmote.OK, -1);
					betInput.addOption(JDAEmote.X, -2);
					betInput.apply(out.getMessage());
					currentPlayer.setAdditionalMoveInput(betInput);
					break;
			}
		})
		.autoRemove(false)
		.removeMessage(false);

		moveInput.addOption(JDAEmote.POKER_FOLD, 0);
		moveInput.addOption(JDAEmote.POKER_CHECK, 1);
		moveInput.addOption(JDAEmote.POKER_BET, 2);
		moveInput.apply(currentPlayer.getOutput().getMessage());
		currentPlayer.setMoveInput(moveInput);
	}

	private boolean isBettingDone() {
		if(players.stream().filter(p -> p.isOut() || (p.isAllIn() && p.getCurrentBet() == null /* player was all in already */)).count() == players.size() - 1) return true;

		int maxBet = getCurrentMaxBet();
		for(PokerPlayer pl : players) {
			if(pl.isOut() || pl.isAllIn()) continue;
			if(pl.getCurrentBet() == null) return false;
			if(pl.getCurrentBet() != maxBet) return false;
		}
		return true;
	}

	private int getCurrentMaxBet() {
		int maxBet = 0;
		for(PokerPlayer pl : players) {
			if(pl.isOut() || pl.getCurrentBet() == null) continue;
			if(pl.getCurrentBet() > maxBet) maxBet = pl.getCurrentBet();
		}
		return maxBet;
	}

	private void applyBets() {
		for(PokerPlayer pl : players) {
			if(pl.getCurrentBet() == null) continue; // Players that are out (+ haven't placed a bet before going out) can't bet
			pl.setTotalBet(pl.getTotalBet() + pl.getCurrentBet());
			pl.setOwnChips(pl.getOwnChips() - pl.getCurrentBet());
			pl.setCurrentBet(null);
		}

		currentPlayer = dealerPlayer;
	}

	private void updateCurrentPlayer() {
		if(currentPlayer == null) {
			currentPlayer = players.size() == 2 ? dealerPlayer : players.get((players.indexOf(dealerPlayer) + 1) % players.size());
			return;
		}

		for(int i = 1; i < players.size(); i++) {
			PokerPlayer pl = players.get((players.indexOf(currentPlayer) + i) % players.size());
			if(pl.isOut()) continue;
			if(pl.isAllIn()) continue;
			currentPlayer = pl;
			break;
		}
	}

	private int getTotalPot() {
		return players.stream()
				.mapToInt(PokerPlayer::getTotalBet)
				.sum();
	}

	private void step() {
		// First everyone bets
		switch(stage) {
			case STAGE_PREFLOP:
				if(isBettingDone()) {
					applyBets();
					for(int i = 0; i < 3; i++) table.add(cardStack.remove(0)); // Reveal 3 cards
					stage = STAGE_FLOP;
					step();
				}else {
					updateCurrentPlayer();
					players.forEach(pl -> pl.getOutput().update(renderTable(pl)));
					spectators.forEach(pl -> pl.getOutput().update(renderTable(pl)));
					bet(); // Let the next player place their bet until everyone is either out, all in or on the same bet
				}
				break;
			case STAGE_FLOP:
				if(isBettingDone()) {
					applyBets();
					table.add(cardStack.remove(0)); // Reveal 1 card
					stage = STAGE_TURN_CARD;
					step();
				}else {
					updateCurrentPlayer();
					players.forEach(pl -> pl.getOutput().update(renderTable(pl)));
					spectators.forEach(pl -> pl.getOutput().update(renderTable(pl)));
					bet(); // Let the next player place their bet until everyone is either out, all in or on the same bet
				}
				break;
			case STAGE_TURN_CARD:
				if(isBettingDone()) {
					applyBets();
					table.add(cardStack.remove(0)); // Reveal 1 card
					stage = STAGE_RIVER_CARD;
					step();
				}else {
					updateCurrentPlayer();
					players.forEach(pl -> pl.getOutput().update(renderTable(pl)));
					spectators.forEach(pl -> pl.getOutput().update(renderTable(pl)));
					bet(); // Let the next player place their bet until everyone is either out, all in or on the same bet
				}
				break;
			case STAGE_RIVER_CARD:
				if(isBettingDone()) {
					applyBets();
					stage = STAGE_SHOWDOWN;
					players.forEach(pl -> pl.getOutput().update(renderTable(pl)));
					spectators.forEach(pl -> pl.getOutput().update(renderTable(pl)));

					Map<PokerPlayer, Integer> winnings = new HashMap<>();

					int totalPot = getTotalPot();
					List<PokerPlayer> remainingPlayers = players.stream()
							.filter(p -> !p.isOut())
							.collect(Collectors.toList());

					while(totalPot > 0) {
						Map<PokerPlayer, List<PlayingCard>> playerCards = remainingPlayers.stream()
								.collect(Collectors.toMap(p -> p, p -> {
									List<PlayingCard> totalCards = new ArrayList<>(p.getHand());
									totalCards.addAll(table);
									return totalCards;
								}));

						Map<PokerPlayer, PokerHand> hands = new HashMap<>();
						for(PokerPlayer p : remainingPlayers) {
							hands.put(p, getHand(playerCards.get(p), 5, true));
						}

						AtomicReference<PokerHand> maxHand = new AtomicReference<>(hands.values().stream().filter(Objects::nonNull).max(Comparator.reverseOrder()).orElse(null)); // What's the highest hand anyone has?
						AtomicInteger usedCards = new AtomicInteger(); // Keep track of the amount of cards that have been used already to impose the 5 card limit
						List<PokerPlayer> potentialWinners = new ArrayList<>(remainingPlayers);

						while(usedCards.get() < 5 && maxHand.get() != null) { // While there's still a hand higher than high card and we haven't used up all 5 cards yet
							usedCards.addAndGet(maxHand.get().getRequiredCards());
							potentialWinners.removeIf(p -> hands.get(p) != maxHand.get()); // Remove anyone who doesn't have that hand
							if(potentialWinners.size() == 1) break; // Don't bother checking if there's only one person left
							hands.clear();
							for(PokerPlayer p : potentialWinners) {
								hands.put(p, getHand(playerCards.get(p), 5, true));
							}
							maxHand.set(hands.values().stream().filter(Objects::nonNull).max(Comparator.reverseOrder()).orElse(null));
						}

						if(potentialWinners.size() > 1 && usedCards.get() < 5) { // We still have a chance at high card
							for(int i = 0; i < 5 - usedCards.get(); i++) { // There might be up to 5 - usedCards rounds of high card comparing
								CardRank highCard = potentialWinners.stream()
										.flatMap(p -> playerCards.get(p).stream())
										.map(c -> c.getRank())
										.max(Comparator.comparingInt(c -> c == CardRank.ACE ? 13 : c.ordinal())).get();

								potentialWinners.removeIf(p -> playerCards.get(p).stream().noneMatch(c -> c.getRank() == highCard)); // Anyone who doesn't have high card has lost
								potentialWinners.forEach(p -> {
									PlayingCard playerHighCard = playerCards.get(p).stream()
											.filter(c -> c.getRank() == highCard)
											.findFirst().orElse(null);
									playerCards.get(p).remove(playerHighCard);
								});
								if(potentialWinners.size() == 1) break; // We've reached a final winner
							}
						}

						OptionalInt minBet = potentialWinners.stream()
								.filter(p -> !p.isOut() && p.isAllIn())
								.mapToInt(PokerPlayer::getTotalBet)
								.min(); // The minimum pot between the users that have won, to account for all-in users (-> side pots)

						int actualBet = minBet.isPresent() ? minBet.getAsInt() : totalPot; // e.g. if all players fold and the other players didn't bet anything

						int totalSidePot = players.stream()
								.mapToInt(p -> p.isOut() ? p.getTotalBet() : Math.min(actualBet, p.getTotalBet()))
								.sum();

						totalPot -= totalSidePot; // Remove the side pot from the rest of the pot

						// Split the pot between the winners
						int winningsPerPlayer = Math.floorDiv(totalSidePot, potentialWinners.size());
						potentialWinners.forEach(p -> winnings.put(p, winnings.getOrDefault(p, 0) + winningsPerPlayer));
						totalSidePot -= winningsPerPlayer * potentialWinners.size();

						if(totalSidePot != 0) { // The pot can't be split evenly
							Collections.shuffle(potentialWinners); // Shuffle the players so nobody has an advantage
							for(int i = 0; i < totalSidePot; i++) {
								PokerPlayer luckyPlayer = potentialWinners.get(i);
								luckyPlayer.setOwnChips(winnings.put(luckyPlayer, winnings.getOrDefault(luckyPlayer, 0) + 1));
							}
						}

						players.forEach(p -> p.setTotalBet(Math.max(0, p.getTotalBet() - actualBet)));
						remainingPlayers.removeIf(p -> p.getTotalBet() <= 0);
					}

					winnings.entrySet().forEach(e -> e.getKey().setOwnChips(e.getKey().getOwnChips() + e.getValue()));

					EmbedBuilder eb = new EmbedBuilder();
					eb.setColor(Color.DARK_GRAY);

					eb.setTitle("Winnings");
					for(int i = 0; i < players.size(); i++) {
						PokerPlayer pl = players.get(i);

						if(!winnings.containsKey(pl)) {
							eb.addField((i + 1) + ") " + pl.getUser().getName(), "No winning", false);
							continue;
						}

						int w = winnings.get(pl);
						eb.addField((i + 1) + ") " + pl.getUser().getName(), asChipString(w) + " (" + w + ")", false);
					}

					for(PokerPlayer p : players) p.getStatusOutput().update(eb.build());
					for(PokerPlayer p : spectators) p.getStatusOutput().update(eb.build());

					if(players.stream().filter(p -> p.getOwnChips() == 0).count() == players.size() - 1) { // Only one player left
						PokerPlayer winner = players.stream()
								.filter(p -> p.getOwnChips() > 0)
								.findFirst().get();
						DefaultMessage.MINIGAME_WON.sendMessage(winner.getUser().openPrivateChannel(), "money", ""+GraphiteMinigameMoney.BATTLESHIPS.getMoney());
						userWon(winner.getUser(), players.size() + " player Poker", GraphiteMinigameMoney.POKER);
						players.forEach(p -> {
							if(p == winner) return;
							DefaultMessage.MINIGAME_LOST.sendMessage(p.getUser().openPrivateChannel());
							userLost(p.getUser(), players.size() + " player Poker");
						});
						stage = STAGE_STOPPED;
						stop();
						return;
					}

					for(int i = 1; i < players.size(); i++) {
						PokerPlayer pl = players.get((players.indexOf(dealerPlayer) + i) % players.size());
						if(pl.getOwnChips() == 0) continue;
						dealerPlayer = pl;
						break;
					}

					List<PokerPlayer> newSpectators = new ArrayList<>();

					for(PokerPlayer pl : new ArrayList<>(players)) {
						pl.setTotalBet(0);
						pl.setCurrentBet(null);
						pl.setOut(false);
						pl.getHand().clear();

						if(pl.getOwnChips() == 0) {
							players.remove(pl);
							newSpectators.add(pl);
							continue;
						}
					}

					spectators.addAll(newSpectators);

					if(spectators.contains(hostPlayer)) hostPlayer = players.get(0); // If host player loses, make the new first player the host

					blindState = 0;
					currentPlayer = null;

					MessageOutput out = new MessageOutput(hostPlayer.getUser().openPrivateChannel());
					out.update("Play next round?");

					SelectInput<Integer> replayIn = new SelectInput<Integer>(Collections.singletonList(hostPlayer.getUser()), it -> {
						initGame();

						for(PokerPlayer pl : new ArrayList<>(players)) {
							pl.getHand().add(cardStack.remove(0)); // Give everyone 2 cards
							pl.getHand().add(cardStack.remove(0));
						}

						hostPlayer.getMoveInput().remove();
						hostPlayer.setMoveInput(null);

						stage = STAGE_PREFLOP;
						step();

						newSpectators.forEach(sp -> {
							if(!spectators.contains(sp)) return;
							MessageOutput spOut = new MessageOutput(sp.getUser().openPrivateChannel());
							spOut.update("You've lost all your chips and are now spectating the game. Press the :eyes: button below to see the cards of all players");
							sp.setAdditionalOutput(spOut);

							SelectInput<Integer> spIn = new SelectInput<Integer>(Collections.singletonList(sp.getUser()), it2 -> {
								sp.setSpectatorCardsVisible(!sp.isSpectatorCardsVisible());
								sp.getOutput().update(renderTable(sp));
							})
							.autoRemove(false)
							.removeMessage(false);

							spIn.addOption(JDAEmote.EYES, 0);
							spIn.apply(spOut.getMessage());
							sp.setMoveInput(spIn);
						});
					})
					.autoRemove(true)
					.removeMessage(true);

					replayIn.addOption(JDAEmote.WHITE_CHECK_MARK, 0);
					replayIn.apply(out.getMessage());
					hostPlayer.setMoveInput(replayIn);
					hostPlayer.setAdditionalOutput(out);
				}else {
					updateCurrentPlayer();
					players.forEach(pl -> pl.getOutput().update(renderTable(pl)));
					spectators.forEach(pl -> pl.getOutput().update(renderTable(pl)));
					bet(); // Let the next player place their bet until everyone is either out, all in or on the same bet
				}
				break;
		}

		// Then different stuff happens, depending on the round
	}

	private PokerHand getHand(List<PlayingCard> cards, int maxCards, boolean removeCards) {
		for(PokerHand hand : PokerHand.values()) {
			if(hand.getRequiredCards() > maxCards || hand.getRequiredCards() > cards.size()) continue; // Don't check hands that require more cards than we have (available)
			List<PlayingCard> m = hand.check(cards);
			if(m != null) {
				if(removeCards) cards.removeAll(m);
				return hand;
			}
		}

		return null;
	}

	private String asChipString(int chips) {
		StringBuilder b = new StringBuilder();
		while(chips > 0) {
			final int ch = chips;
			Map.Entry<JDAEmote, Integer> e = CHIPS.entrySet().stream()
					.filter(en -> en.getValue() <= ch)
					.max(Comparator.comparingInt(en -> en.getValue())).orElse(null);
			int am = Math.floorDiv(chips, e.getValue());
			chips -= am * e.getValue();
			b.append(" ").append(am).append("x ").append(e.getKey().getUnicode());
		}
		return b.toString().trim();
	}

	private List<PokerPlayer> getAllPlayers() {
		List<PokerPlayer> allPlayers = new ArrayList<>(players);
		allPlayers.addAll(spectators);
		return allPlayers;
	}

	@Override
	public List<GraphiteInput> getActiveInputs() {
		return getAllPlayers().stream()
				.flatMap(p -> Arrays.asList(p.getMoveInput(), p.getAdditionalMoveInput()).stream())
				.collect(Collectors.toList());
	}

	@Override
	public List<GameOutput> getActiveOutputs() {
		return getAllPlayers().stream()
				.flatMap(p -> Arrays.asList(p.getOutput(), p.getAdditionalOutput(), p.getStatusOutput()).stream())
				.collect(Collectors.toList());
	}

	@Override
	public GraphiteMinigame getGame() {
		return GraphiteMinigame.POKER;
	}

	@Override
	public void addUser(GraphiteUser user) {
		if(players.size() >= 6) throw new UnsupportedOperationException("Game is full");
		players.add(new PokerPlayer(this, user));

		hostPlayer.getOutput().update("Press :white_check_mark: to start the game.\nCurrent players:\n" + players.stream()
			.map(p -> "- " + p.getUser().getName())
			.collect(Collectors.joining("\n")));
	}

	@Override
	public List<GraphiteUser> getPlayingUsers() {
		return getAllPlayers().stream()
				.map(PokerPlayer::getUser)
				.collect(Collectors.toList());
	}

	@Override
	public void onUserLeave(GraphiteUser user) {
		if(spectators.stream().anyMatch(sp -> sp.getUser().equals(user))) {
			spectators.removeIf(sp -> sp.getUser().equals(user));
			return;
		}

		if(stage != STAGE_WAITING) {
			if(stage == STAGE_STOPPED) return;
			stage = STAGE_STOPPED;
			getPlayingUsers().stream().filter(p -> p != null && !p.equals(user)).forEach(p -> DefaultMessage.COMMAND_MINIGAME_LEAVE_MULTIPLAYER_AUTOLEAVE.sendMessage(p.openPrivateChannel(), "user", user.getName()));
			stop();
			return;
		}

		if(players.size() == 1 || hostPlayer.getUser().equals(user)) {
			stop();
		}else {
			players.removeIf(pl -> pl.getUser().equals(user));
			hostPlayer.getOutput().update("Press :white_check_mark: to start the game.\nCurrent players:\n" + players.stream()
				.map(p -> "- " + p.getUser().getName())
				.collect(Collectors.joining("\n")));
		}
	}

	@Override
	public boolean isJoinable() {
		return stage == STAGE_WAITING && players.size() < 6;
	}



}

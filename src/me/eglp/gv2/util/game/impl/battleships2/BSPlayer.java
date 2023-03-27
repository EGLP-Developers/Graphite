package me.eglp.gv2.util.game.impl.battleships2;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import me.eglp.gv2.util.GraphiteUtil;
import me.eglp.gv2.util.base.user.GraphiteUser;
import me.eglp.gv2.util.emote.JDAEmote;
import me.eglp.gv2.util.game.output.MessageOutput;
import me.eglp.gv2.util.game.output.renderer.IntMappingGetterRenderer;
import me.eglp.gv2.util.game.output.renderer.MessageGraphics;
import me.eglp.gv2.util.input.ButtonInput;
import me.eglp.gv2.util.lang.DefaultMessage;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;

public class BSPlayer {
	
	private static final Random RANDOM = new Random();

	private Battleships game;
	private GraphiteUser user;
	private BSSubBoard board;
	private ButtonInput<Integer> input;
	private MessageOutput output;
	
	private Integer x, y;
	
	public BSPlayer(Battleships game, GraphiteUser user) {
		this.game = game;
		this.user = user;
		this.board = new BSSubBoard(10, 10);
		this.output = new MessageOutput(user.openPrivateChannel());
	}
	
	public GraphiteUser getUser() {
		return user;
	}
	
	public BSSubBoard getBoard() {
		return board;
	}
	
	public void setInput(ButtonInput<Integer> input) {
		this.input = input;
	}
	
	public ButtonInput<Integer> getInput() {
		return input;
	}
	
	public MessageOutput getOutput() {
		return output;
	}
	
	private IntMappingGetterRenderer createRenderer(BSSubBoard board, boolean tracking) {
		IntMappingGetterRenderer r = new IntMappingGetterRenderer(board.getWidth(), board.getHeight(), (x, y) -> {
			int v = board.get(x, y);
			
			if(!tracking && v == BSSubBoard.SHIP) return v;
			
			if((tracking != (game.getMoveState() == BSMoveState.PLACE_SHIPS)) && (this.x == x && (this.y == null || this.y == y))) {
				return -1;
			}
			
			if(tracking) {
				if(v != BSSubBoard.HIT_SHOT
						&& v != BSSubBoard.MISSED_SHOT
						&& v != BSSubBoard.SUNKEN_SHIP) return BSSubBoard.OCEAN;
				return v;
			}
			
			return v;
		});
		r.addMapping(BSSubBoard.HIT_SHOT, JDAEmote.RED_CIRCLE);
		r.addMapping(BSSubBoard.MISSED_SHOT, JDAEmote.WHITE_CIRCLE);
		r.addMapping(BSSubBoard.OCEAN, JDAEmote.OCEAN);
		r.addMapping(BSSubBoard.SHIP, JDAEmote.BLACK_CIRCLE);
		r.addMapping(BSSubBoard.SUNKEN_SHIP, JDAEmote.BOOM);
		r.addMapping(-1, JDAEmote.GREEN_SQUARE);
		return r;
	}
	
	private MessageEditBuilder createField() {
		MessageEditBuilder builder = new MessageEditBuilder();
		EmbedBuilder b = new EmbedBuilder();
		
		b.setTitle("Battleships");
		b.appendDescription("You're playing against " + game.getOpponent(this).getUser().getName());
		
		IntMappingGetterRenderer r = createRenderer(game.getOpponent(this).getBoard(), true);
		MessageGraphics g2 = new MessageGraphics();
		
		g2.setSymbol(JDAEmote.ASTERISK);
		g2.point(0, 0);
		for(int x = 0; x < 10; x++) {
			g2.setSymbol(JDAEmote.getKeycapNumber(x + 1));
			g2.point(x + 1, 0);
		}
		
		for(int y = 0; y < 10; y++) {
			g2.setSymbol(JDAEmote.getKeycapNumber(y + 1));
			g2.point(0, y + 1);
		}
		
		r.render(g2, 1, 1);
		b.addField("Opponent's Board", g2.render(false), false);
		
		IntMappingGetterRenderer r2 = createRenderer(board, false);
		MessageGraphics g3 = new MessageGraphics();
		
		g3.setSymbol(JDAEmote.ASTERISK);
		g3.point(0, 0);
		
		for(int x = 0; x < 10; x++) {
			g3.setSymbol(JDAEmote.getKeycapNumber(x + 1));
			g3.point(x + 1, 0);
		}
		
		for(int y = 0; y < 10; y++) {
			g3.setSymbol(JDAEmote.getKeycapNumber(y + 1));
			g3.point(0, y + 1);
		}
		
		r2.render(g3, 1, 1);
		
		String s = g3.render(false);
		if(game.getMoveState() == BSMoveState.PLACE_SHIPS && !board.hasFinishedPlacingShips()) {
			s = String.format("Placing at (%s/%s)", x == null ? "?" : x + 1, y == null ? "?" : y + 1) + "\n" + s;
		}
		b.addField("Your Board", s, false);
		builder.setEmbeds(b.build());
		return builder;
	}
	
	private ButtonInput<Integer> createInput() {
		if(input != null) input.remove();
		input = new ButtonInput<>(event -> {
			BSMoveState ms = game.getMoveState();
			if(ms == BSMoveState.PLACE_SHIPS) {
				if(event.getItem() == -1) {
					x = null;
					y = null;
				}
				
				if(x == null) {
					x = event.getItem();
				}else if(y == null) {
					y = event.getItem();
				}else {
					Direction d = Direction.values()[event.getItem()];
					if(!board.placeShip(board.getNextShipType(), x, y, d)) event.getJDAEvent().deferReply(true).setContent("You can't place a ship there").queue();
					x = null;
					y = null;
					
					if(board.hasFinishedPlacingShips() && game.getOpponent(this).getBoard().hasFinishedPlacingShips()) {
						game.setMoveState(RANDOM.nextBoolean() ? BSMoveState.PLAYER_ONE : BSMoveState.PLAYER_TWO);
						game.getOpponent(this).updateMessage();
					}
					
					MessageEditBuilder newPrimary = createField();
					createInput().apply(newPrimary);
					if(!event.getJDAEvent().isAcknowledged()) {
						event.getJDAEvent().editMessage(newPrimary.build()).queue();
					}else {
						output.update(newPrimary);
					}
					return;
				}
				
				MessageEditBuilder newMessage = createField();
				createInput().apply(newMessage);
				event.getJDAEvent().editMessage(newMessage.build()).queue();
			}else if(game.isTurnForPlayer(this)) {
				if(event.getItem() == -1) {
					x = null;
					y = null;
				}
				
				if(x == null) {
					x = event.getItem();
				}else {
					y = event.getItem();
					if(!game.getOpponent(this).getBoard().fireAt(x, y)) {
						event.getJDAEvent().deferReply(true).setContent("You've already shot that point");
						x = null;
						y = null;
						return;
					}
					
					game.setMoveState(game.getMoveState() == BSMoveState.PLAYER_ONE ? BSMoveState.PLAYER_TWO : BSMoveState.PLAYER_ONE);
					game.getOpponent(this).updateMessage();
				}
				
				MessageEditBuilder newMessage = createField();
				createInput().apply(newMessage);
				event.getJDAEvent().editMessage(newMessage.build()).queue();
			}
		});
		
		input.autoRemove(false);
		input.removeMessage(false);
		input.expireAfter(1, TimeUnit.HOURS);
		input.setOnExpire(in -> game.endGame(DefaultMessage.COMMAND_MINIGAME_STOPPED));
		
		BSMoveState ms = game.getMoveState();
		if(ms == BSMoveState.PLACE_SHIPS) {
			if(board.hasFinishedPlacingShips()) {
				input.addOptionRaw(Button.of(ButtonStyle.SUCCESS, GraphiteUtil.randomShortID(), "Waiting for other player", JDAEmote.WHITE_CHECK_MARK.getEmoji()).asDisabled(), -1);
			}else if(x == null || y == null) {
				for(int y = 0; y < 2; y++) {
					for(int x = 0; x < 5; x++) {
						int coord = y * 5 + x;
						JDAEmote e = JDAEmote.getKeycapNumber(coord + 1);
						input.addOption(ButtonStyle.PRIMARY, e, coord);
					}
					input.newRow();
				}
				
				input.addOption(ButtonStyle.DANGER, JDAEmote.WASTEBASKET, -1);
			}else {
				input.addOptionRaw(Button.of(ButtonStyle.SECONDARY, GraphiteUtil.randomShortID(), JDAEmote.TRANSPARENT.getEmoji()).asDisabled(), -2);
				input.addOption(ButtonStyle.PRIMARY, JDAEmote.ARROW_UP, Direction.UP.ordinal());
				input.addOptionRaw(Button.of(ButtonStyle.SECONDARY, GraphiteUtil.randomShortID(), JDAEmote.TRANSPARENT.getEmoji()).asDisabled(), -2);
				input.newRow();
				input.addOption(ButtonStyle.PRIMARY, JDAEmote.ARROW_LEFT, Direction.LEFT.ordinal());
				input.addOptionRaw(Button.of(ButtonStyle.SECONDARY, GraphiteUtil.randomShortID(), JDAEmote.TRANSPARENT.getEmoji()).asDisabled(), -2);
				input.addOption(ButtonStyle.PRIMARY, JDAEmote.ARROW_RIGHT, Direction.RIGHT.ordinal());
				input.newRow();
				input.addOptionRaw(Button.of(ButtonStyle.SECONDARY, GraphiteUtil.randomShortID(), JDAEmote.TRANSPARENT.getEmoji()).asDisabled(), -2);
				input.addOption(ButtonStyle.PRIMARY, JDAEmote.ARROW_DOWN, Direction.DOWN.ordinal());
				input.addOptionRaw(Button.of(ButtonStyle.SECONDARY, GraphiteUtil.randomShortID(), JDAEmote.TRANSPARENT.getEmoji()).asDisabled(), -2);
				input.newRow();
				input.addOption(ButtonStyle.DANGER, JDAEmote.WASTEBASKET, -1);
			}
		}else if(game.isTurnForPlayer(this)) {
			for(int y = 0; y < 2; y++) {
				for(int x = 0; x < 5; x++) {
					int coord = y * 5 + x;
					JDAEmote e = JDAEmote.getKeycapNumber(coord + 1);
					input.addOption(ButtonStyle.PRIMARY, e, coord);
				}
				input.newRow();
			}
			
			input.addOption(ButtonStyle.DANGER, JDAEmote.WASTEBASKET, -1);
		}else {
			input.addOptionRaw(Button.of(ButtonStyle.SUCCESS, GraphiteUtil.randomShortID(), "It's the other player's turn", JDAEmote.CLOCK1.getEmoji()).asDisabled(), -1);
		}
		
		return input;
	}
	
	public void sendMessage() {
		if(input != null) input.remove();
		
		MessageEditBuilder builder = createField();
		createInput().apply(builder);
		output.update(builder);
	}
	
	public void updateMessage() {
		MessageEditBuilder newPrimary = createField();
		createInput().apply(newPrimary);
		output.update(newPrimary);
	}
	
}

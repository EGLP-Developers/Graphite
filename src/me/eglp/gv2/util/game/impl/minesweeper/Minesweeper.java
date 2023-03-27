package me.eglp.gv2.util.game.impl.minesweeper;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import me.eglp.gv2.util.base.user.GraphiteUser;
import me.eglp.gv2.util.emote.JDAEmote;
import me.eglp.gv2.util.game.GraphiteMinigame;
import me.eglp.gv2.util.game.GraphiteMinigameMoney;
import me.eglp.gv2.util.game.SinglePlayerMinigameInstance;
import me.eglp.gv2.util.game.output.GameOutput;
import me.eglp.gv2.util.game.output.MessageOutput;
import me.eglp.gv2.util.game.output.renderer.IntMappingGetterRenderer;
import me.eglp.gv2.util.game.output.renderer.MessageGraphics;
import me.eglp.gv2.util.input.GraphiteInput;
import me.eglp.gv2.util.input.SelectInput;
import me.eglp.gv2.util.lang.DefaultMessage;
import net.dv8tion.jda.api.entities.Message;

public class Minesweeper implements SinglePlayerMinigameInstance {
	
	private GraphiteUser user;
	private MineField field;
	private SelectInput<Integer> coordInput, flagInput;
	private boolean flagMode;
	private Integer mx, my;
	private MessageOutput msg;
	
	public Minesweeper(GraphiteUser user) {
		this.user = user;
		this.field = new MineField(13, 25);
		this.msg = new MessageOutput(user.openPrivateChannel());
		step();
	}
	
	private void step() {
		sendField(false, flagMode);
		SelectInput<Integer> s = new SelectInput<Integer>(Collections.singletonList(user), it ->  {
			if(mx == null) {
				mx = it;
				step();
				return;
			}
			my = it;
			if(!flagMode) {
				field.reveal(mx, my);
				if(field.hasLost()) {
					flagInput.remove();
					int sz = field.getSize();
					userLost(user, sz + "x" + sz + "-" + field.getNumMines());
					sendField(true, false);
					DefaultMessage.MINIGAME_LOST.sendMessage(user.openPrivateChannel());
					stop();
					return;
				}else if(field.hasWon()){
					flagInput.remove();
					int sz = field.getSize();
					userWon(user, sz + "x" + sz + "-" + field.getNumMines(), GraphiteMinigameMoney.MINESWEEPER);
					sendField(true, false);
					DefaultMessage.MINIGAME_WON.sendMessage(user.openPrivateChannel(), "money", ""+GraphiteMinigameMoney.MINESWEEPER.getMoney());
					stop();
					return;
				}
			}else {
				field.flag(mx, my);
			}
			mx = my = null;
			step();
		})
		.removeUserInput(true)
		.autoRemove(true)
		.removeMessage(false);
		
		for(int i = 0; i < field.getSize(); i++) {
			s.addOption(JDAEmote.getKeycapNumber(i + 1), i);
		}
		s.apply(msg.getMessage());
		flg(msg.getMessage());
		coordInput = s;
	}
	
	private void flg(Message m) {
		if(flagInput != null) return;
		flagInput = new SelectInput<Integer>(Collections.singletonList(user), it -> {
			if(it == -1) {
				stop();
				return;
			}else if(it == -2) {
				mx = my = null;
				return;
			}else if(it == -3) {
				DefaultMessage.MINIGAME_MINESWEEPER_HELP.sendMessage(user.openPrivateChannel());
				return;
			}
			flagMode = !flagMode;
			sendField(false, flagMode);
		})
		.autoRemove(false)
		.removeMessage(false);
		
		flagInput.addOption(JDAEmote.REPEAT, -2);
		flagInput.addOption(JDAEmote.TRIANGULAR_FLAG_ON_POST, 1);
		flagInput.addOption(JDAEmote.X, -1);
		flagInput.addOption(JDAEmote.QUESTION, -3);
		flagInput.apply(m);
	}
	
	private void sendField(boolean reveal, boolean flagMode) {
		MessageGraphics g = new MessageGraphics();
		g.setSymbol(flagMode ? JDAEmote.TRIANGULAR_FLAG_ON_POST : JDAEmote.ASTERISK);
		g.point(0, 0);
		for(int x = 0; x < field.getSize(); x++) {
			g.setSymbol(JDAEmote.getKeycapNumber(x + 1));
			g.point(x + 1, 0);
		}
		for(int y = 0; y < field.getSize(); y++) {
			g.setSymbol(JDAEmote.getKeycapNumber(y + 1));
			g.point(0, y + 1);
		}
		
		int[][] nf = field.genNumField(reveal);
		IntMappingGetterRenderer r = new IntMappingGetterRenderer(field.getSize(), field.getSize(), (x, y) -> nf[x][y]);
		r.addMapping(MineField.FLAG, JDAEmote.TRIANGULAR_FLAG_ON_POST);
		r.addMapping(MineField.MINE, JDAEmote.BOMB);
		r.addMapping(MineField.MINE_EXPLODED, JDAEmote.BOOM);
		r.addMapping(MineField.UNREVEALED, JDAEmote.BLACK_LARGE_SQUARE);
		r.addMapping(0, JDAEmote.WHITE_LARGE_SQUARE);
		for(int i = 1; i <= 8; i++) r.addMapping(i, JDAEmote.getKeycapNumber(i));
		MessageGraphics g2 = new MessageGraphics();
		r.render(g2);
		g.draw(1, 1, g2);
		
		msg.update(g);
	}
	
	@Override
	public List<GraphiteInput> getActiveInputs() {
		return Arrays.asList(coordInput, flagInput);
	}
	
	@Override
	public List<GameOutput> getActiveOutputs() {
		return Collections.singletonList(msg);
	}
	
	@Override
	public GraphiteMinigame getGame() {
		return GraphiteMinigame.MINESWEEPER;
	}

	@Override
	public GraphiteUser getPlayingUser() {
		return user;
	}
	
}

package me.eglp.gv2.util.game.impl.tictactoe;

import java.util.concurrent.TimeUnit;

import me.eglp.gv2.user.GraphiteUser;
import me.eglp.gv2.util.GraphiteUtil;
import me.eglp.gv2.util.emote.JDAEmote;
import me.eglp.gv2.util.game.output.MessageOutput;
import me.eglp.gv2.util.input.ButtonInput;
import me.eglp.gv2.util.lang.DefaultMessage;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;

public class TicTacToePlayer {

	private TicTacToe game;
	private GraphiteUser user;
	private ButtonInput<Integer> input;
	private MessageOutput output;

	public TicTacToePlayer(TicTacToe game, GraphiteUser user) {
		this.game = game;
		this.user = user;
		this.output = new MessageOutput(user.openPrivateChannel());
	}

	public GraphiteUser getUser() {
		return user;
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

	public void updateMessage() {
		if(input != null) input.remove();

		MessageEditBuilder builder = new MessageEditBuilder();
		builder.setContent("You're playing against " + game.getOpponent(this).getUser().getName());
		input = new ButtonInput<>(event -> {
			game.onPlayerInput(this, event);
		});

		input.autoRemove(false);
		input.removeMessage(false);
		input.expireAfter(10, TimeUnit.SECONDS);
		input.setOnExpire(in -> game.endGame(DefaultMessage.COMMAND_MINIGAME_STOPPED));

		for(int y = 0; y < 3; y++) {
			for(int x = 0; x < 3; x++) {
				JDAEmote e = JDAEmote.BOOM;
				switch(game.getBoard().get(x, y)) {
					case TTTBoard.EMPTY:
						e = JDAEmote.TRANSPARENT;
						break;
					case TTTBoard.X:
						e = JDAEmote.X;
						break;
					case TTTBoard.O:
						e = JDAEmote.O;
						break;
				}

				Button button = Button.of(game.isTurnForPlayer(this) ? ButtonStyle.PRIMARY : ButtonStyle.SECONDARY, GraphiteUtil.randomShortID(), e.getEmoji());
				input.addOptionRaw(button, y * 3 + x);
			}
			if(y != 2) input.newRow();
		}

		input.apply(builder);
		output.update(builder);
	}

}

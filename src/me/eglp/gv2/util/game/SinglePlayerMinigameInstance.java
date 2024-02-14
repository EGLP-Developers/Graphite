package me.eglp.gv2.util.game;

import java.util.Collections;
import java.util.List;

import me.eglp.gv2.user.GraphitePrivateChannel;
import me.eglp.gv2.user.GraphiteUser;
import me.eglp.gv2.util.emote.JDAEmote;
import me.eglp.gv2.util.input.ButtonInput;
import me.eglp.gv2.util.lang.DefaultLocaleString;
import me.eglp.gv2.util.lang.DefaultMessage;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

public interface SinglePlayerMinigameInstance extends MinigameInstance {

	public GraphiteUser getPlayingUser();

	@Override
	public default void addUser(GraphiteUser user) throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Game is a single player game");
	}

	@Override
	public default List<GraphiteUser> getPlayingUsers() {
		return Collections.singletonList(getPlayingUser());
	}

	@Override
	public default void onUserLeave(GraphiteUser user) {
		stop();
	}

	public default void sendReplayMessage() {
		GraphiteUser user = getPlayingUser();
		GraphitePrivateChannel p = user.openPrivateChannel();
		if(p == null) return;
		MessageCreateBuilder lol = DefaultMessage.MINIGAME_REPLAY.createMessageBuilder(user);

		ButtonInput<Boolean> b = new ButtonInput<Boolean>(Collections.singletonList(user), bl -> {
			if(bl.getItem()) getGame().startNewGame(user);
			bl.markDone();
		})
		.autoRemove(true)
		.removeMessage(true);

		b.addOption(ButtonStyle.PRIMARY, JDAEmote.OK_HAND, DefaultLocaleString.OTHER_YES.getFor(user), true);
		b.addOption(ButtonStyle.SECONDARY, JDAEmote.X, DefaultLocaleString.OTHER_NO.getFor(user), false);
		b.apply(lol);
	}

}

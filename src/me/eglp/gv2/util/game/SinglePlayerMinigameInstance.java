package me.eglp.gv2.util.game;

import java.util.Collections;
import java.util.List;

import me.eglp.gv2.util.base.user.GraphitePrivateChannel;
import me.eglp.gv2.util.base.user.GraphiteUser;
import me.eglp.gv2.util.emote.JDAEmote;
import me.eglp.gv2.util.input.SelectInput;
import me.eglp.gv2.util.lang.DefaultMessage;
import net.dv8tion.jda.api.entities.Message;

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
		Message lol = DefaultMessage.MINIGAME_REPLAY.sendMessageComplete(p);
		
		SelectInput<Boolean> b = new SelectInput<Boolean>(Collections.singletonList(user), bl -> {
			if(bl) getGame().startNewGame(user);
		})
		.autoRemove(true)
		.removeMessage(true);
		
		b.addOption(JDAEmote.OK_HAND, true);
		b.addOption(JDAEmote.X, false);
		b.apply(lol);
	}
	
}

package me.eglp.gv2.util.game;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.user.GraphitePrivateChannel;
import me.eglp.gv2.user.GraphiteUser;
import me.eglp.gv2.util.emote.JDAEmote;
import me.eglp.gv2.util.input.ButtonInput;
import me.eglp.gv2.util.lang.DefaultMessage;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;

public interface MultiPlayerMinigameInstance extends MinigameInstance {

	public boolean isJoinable();

	public default boolean sendInvite(GraphiteUser inviter, GraphiteUser user) {
		return sendInvite(inviter, user, false);
	}

	public default boolean sendInvite(GraphiteUser inviter, GraphiteUser user, boolean isRematch) {
		GraphitePrivateChannel p = user.openPrivateChannel();
		if(p == null) return false;

		ButtonInput<Boolean> input = new ButtonInput<>(event -> {
			if(event.getItem()) {
				if(isJoinable()) {
					addUser(user);
					Graphite.getMinigames().setGame(user, this);
				}else {
					DefaultMessage.COMMAND_MINIGAME_CANT_JOIN.sendMessage(p);
				}
			}else if(isRematch){
				GraphitePrivateChannel ch = inviter.openPrivateChannel();
				if(ch == null) return;
				DefaultMessage.MINIGAME_REMATCH_INVITE_DECLINED.sendMessage(ch, "user", user.getName());
			}
		});
		input.autoRemove(true);
		input.removeMessage(true);

		input.addOption(ButtonStyle.PRIMARY, JDAEmote.OK_HAND, true);
		input.addOption(ButtonStyle.SECONDARY, JDAEmote.X, false);
		input.send(p, DefaultMessage.COMMAND_MINIGAME_INVITE_MESSAGE,
				"inviter", inviter.getName(),
				"minigame", getGame().getFriendlyName().getFor(user));

		return true;
	}

	public default void sendRematchInvite(GraphiteUser inviter, GraphiteUser invited) {
		MinigameInstance ins = getGame().startNewGame(inviter);
		MultiPlayerMinigameInstance mm = (MultiPlayerMinigameInstance) ins;
		mm.sendInvite(inviter, invited, true);
		GraphitePrivateChannel ch = inviter.openPrivateChannel();
		if(ch == null) return;
		DefaultMessage.COMMAND_MINIGAME_REMATCH_INVITED.sendMessage(ch,
				"user", invited.getName(),
				"minigame", getGame().getFriendlyName().getFor(inviter));
	}

	@Override
	public default void stop(boolean removeOutputs) {
		Graphite.getMinigames().unshareMinigame(this);
		MinigameInstance.super.stop(removeOutputs);
	}

}

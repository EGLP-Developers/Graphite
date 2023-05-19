package me.eglp.gv2.util.input;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.user.GraphiteUser;
import me.eglp.gv2.util.base.GraphiteMessageChannel;
import me.eglp.gv2.util.event.AnnotationEventHandler;
import me.eglp.gv2.util.event.EventHandler;
import me.eglp.gv2.util.event.custom.impl.GraphiteMessageReceivedEvent;

public class MessageInput implements GraphiteInput, AnnotationEventHandler {

	private String channelID;
	private List<GraphiteUser> allowedUsers;
	private boolean autoRemove;
	private Consumer<String> callback;

	public MessageInput(List<GraphiteUser> allowedUsers, boolean autoRemove, Consumer<String> callback) {
		this.allowedUsers = allowedUsers;
		this.autoRemove = autoRemove;
		this.callback = callback;
	}

	public MessageInput(GraphiteUser allowedUser, boolean autoRemove, Consumer<String> callback) {
		this(Collections.singletonList(allowedUser), autoRemove, callback);
	}

	@EventHandler
	public void onMessage(GraphiteMessageReceivedEvent event) {
		if(event.getCommandTriggered() != null && event.getCommandTriggered().allowsInGame()) return;

		if(event.getJDAEvent().getChannel().getId().equals(channelID)) {
			if(!allowedUsers.stream().anyMatch(u -> u.getID().equals(event.getJDAEvent().getAuthor().getId()))) return;
			callback.accept(event.getJDAEvent().getMessage().getContentRaw());
			if(autoRemove) remove();
		}
	}

	public void apply(GraphiteMessageChannel<?> ch) {
		this.channelID = ch.getJDAChannel().getId();
		Graphite.getCustomListener().registerTemporaryHandler(this, 1, TimeUnit.HOURS);
	}

	@Override
	public void remove() {
		Graphite.getCustomListener().unregisterHandler(this);
	}

}

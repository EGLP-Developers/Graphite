package me.eglp.gv2.util.input;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import me.eglp.gv2.main.DebugCategory;
import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.main.GraphiteDebug;
import me.eglp.gv2.multiplex.GraphiteMultiplex;
import me.eglp.gv2.multiplex.bot.MultiplexBot;
import me.eglp.gv2.util.base.user.GraphiteUser;
import me.eglp.gv2.util.emote.JDAEmote;
import me.eglp.gv2.util.event.AnnotationEventHandler;
import me.eglp.gv2.util.event.EventHandler;
import me.mrletsplay.mrcore.misc.TriConsumer;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;

@Deprecated
public class SelectInput<T> implements GraphiteInput, AnnotationEventHandler {

	private MultiplexBot byBot;
	private String messageID;
	private Map<JDAEmote, T> mappings;
	private List<GraphiteUser> allowedUsers;
	
	private boolean
		autoRemove,
		removeMessage,
		removeUserInput;
	
	private TriConsumer<SelectInput<T>, GraphiteUser, T> callback;
	
	public SelectInput(List<GraphiteUser> allowedUsers, TriConsumer<SelectInput<T>, GraphiteUser, T> callback) {
		this.allowedUsers = allowedUsers;
		this.callback = callback;
		this.autoRemove = true;
		this.removeMessage = true;
		this.removeUserInput = false;
		this.mappings = new LinkedHashMap<>();
	}
	
	public SelectInput(List<GraphiteUser> allowedUsers, BiConsumer<SelectInput<T>, T> callback) {
		this(allowedUsers, (it, user, t) -> callback.accept(it, t));
	}
	
	public SelectInput(List<GraphiteUser> allowedUsers, Consumer<T> callback) {
		this(allowedUsers, (it, t) -> callback.accept(t));
	}
	
	public SelectInput(GraphiteUser allowedUser, TriConsumer<SelectInput<T>, GraphiteUser, T> callback) {
		this(Collections.singletonList(allowedUser), callback);
	}
	
	public SelectInput(GraphiteUser allowedUser, BiConsumer<SelectInput<T>, T> callback) {
		this(Collections.singletonList(allowedUser), callback);
	}
	
	public SelectInput(GraphiteUser allowedUser, Consumer<T> callback) {
		this(Collections.singletonList(allowedUser), callback);
	}
	
	/**
	 * Sets whether the input should be automatically removed (the listener unregistered) when an input is submitted<br>
	 * <code>true</code> by default
	 * @param autoRemove Whether to remove on input
	 * @return This SelectInput for chaining purposes
	 */
	public SelectInput<T> autoRemove(boolean autoRemove) {
		this.autoRemove = autoRemove;
		return this;
	}

	/**
	 * Sets whether the message this input is attached to should be automatically removed when an input is submitted<br>
	 * <code>true</code> by default
	 * @param removeMessage Whether to remove on input
	 * @return This SelectInput for chaining purposes
	 */
	public SelectInput<T> removeMessage(boolean removeMessage) {
		this.removeMessage = removeMessage;
		return this;
	}
	
	/**
	 * Sets whether the input reactions should be treated as buttons instead of toggle switches<br>
	 * (= the reaction of a user will be removed when they input something)<br>
	 * <code>false</code> by default
	 * @param removeUserInput Whether to remove user reactions after they've been submitted
	 * @return This SelectInput fir chaining purposes
	 */
	public SelectInput<T> removeUserInput(boolean removeUserInput) {
		this.removeUserInput = removeUserInput;
		return this;
	}
	
	@EventHandler
	public void onReaction(GenericMessageReactionEvent event) {
		if(!GraphiteMultiplex.getCurrentBot().equals(byBot)) return;
		if(event.getMessageId().equals(messageID)) {
			if(event.getUser().isBot()) return; // Ignore bots
			if(removeUserInput && event.isFromGuild()) {
				if(event instanceof MessageReactionRemoveEvent) return;
				event.getReaction().removeReaction(event.getUser()).queue(null, e -> {
					GraphiteDebug.log(DebugCategory.MISCELLANEOUS, "Failed to remove reaction", e);
				});
			}
			if(allowedUsers != null && !allowedUsers.stream().anyMatch(u -> u.getID().equals(event.getUser().getId()))) return;
			Emoji em = event.getEmoji();
			JDAEmote e = JDAEmote.getByEmoji(em);
			if(e == null || !mappings.containsKey(e)) return;
			T v = mappings.get(e);
			callback.accept(this, Graphite.getUser(event.getUser()), v);
			if(autoRemove) remove();
			if(removeMessage) event.getChannel().deleteMessageById(event.getMessageId()).queue();
		}
	}
	
	public void addOption(JDAEmote emote, T option) {
		mappings.put(emote, option);
	}
	
	public void applyReactions(Message m) {
		m = m.getChannel().retrieveMessageById(m.getIdLong()).complete();
		if(m == null) return;
		for(JDAEmote em : mappings.keySet()) {
			if(m.getReactions().stream().anyMatch(r -> em.equalsEmoji(r.getEmoji()))) continue;
			m.addReaction(em.getEmoji()).queue(null, e -> {
				GraphiteDebug.log(DebugCategory.MISCELLANEOUS, "Failed to add reaction", e);
			});
		}
	}
	
	public void apply(Message m) {
		applyReactions(m);
		this.messageID = m.getId();
		this.byBot = GraphiteMultiplex.getCurrentBot();
		Graphite.getJDAListener().registerTemporaryHandler(this, 1, TimeUnit.HOURS);
	}
	
	@Override
	public void remove() {
		Graphite.getJDAListener().unregisterHandler(this);
	}

}

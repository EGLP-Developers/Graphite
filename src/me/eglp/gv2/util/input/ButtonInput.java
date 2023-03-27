package me.eglp.gv2.util.input;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.base.GraphiteMessageChannel;
import me.eglp.gv2.util.base.user.GraphiteUser;
import me.eglp.gv2.util.command.CommandInvokedEvent;
import me.eglp.gv2.util.emote.JDAEmote;
import me.eglp.gv2.util.event.AnnotationEventHandler;
import me.eglp.gv2.util.event.EventHandler;
import me.eglp.gv2.util.lang.DefaultLocaleString;
import me.eglp.gv2.util.lang.DefaultMessage;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;

public class ButtonInput<T> implements GraphiteInput, AnnotationEventHandler {

	private List<Button> buttons;
	private Map<String, T> mappings;
	private List<GraphiteUser> allowedUsers;
	
	private boolean
		autoRemove,
		removeMessage,
		editOnExpire;
	
	private Consumer<ButtonInputEvent<T>> callback;
	
	private long expiryTime;
	private TimeUnit expiryTimeUnit;
	
	private Consumer<ButtonInput<T>> onExpire;
	
	private long expiresAt;
	
	private Message message;
	private InteractionHook interactionHook;
	
	public ButtonInput(List<GraphiteUser> allowedUsers, Consumer<ButtonInputEvent<T>> callback) {
		this.allowedUsers = allowedUsers;
		this.callback = callback;
		this.autoRemove = true;
		this.removeMessage = true;
		this.editOnExpire = true;
		this.buttons = new ArrayList<>();
		this.mappings = new HashMap<>();
		this.expiryTime = 5;
		this.expiryTimeUnit = TimeUnit.MINUTES;
	}
	
	public ButtonInput(GraphiteUser allowedUser, Consumer<ButtonInputEvent<T>> callback) {
		this(Collections.singletonList(allowedUser), callback);
	}
	
	public ButtonInput(Consumer<ButtonInputEvent<T>> callback) {
		this((List<GraphiteUser>) null, callback);
	}
	
	/**
	 * Sets whether the input should be automatically removed (the listener unregistered) when an input is submitted<br>
	 * <code>true</code> by default
	 * @param autoRemove Whether to remove on input
	 * @return This input for chaining purposes
	 */
	public ButtonInput<T> autoRemove(boolean autoRemove) {
		this.autoRemove = autoRemove;
		return this;
	}

	/**
	 * Sets whether the message this input is attached to should be automatically removed when an input is submitted<br>
	 * <code>true</code> by default
	 * @param removeMessage Whether to remove on input
	 * @return This input for chaining purposes
	 */
	public ButtonInput<T> removeMessage(boolean removeMessage) {
		this.removeMessage = removeMessage;
		return this;
	}

	/**
	 * Sets the timeout for when the listener should be removed<br>
	 * <code>1 TimeUnit.HOURS</code> by default
	 * @param timeout The timeout after which the listener should be removed
	 * @param unit The unit of the timeout
	 * @return This input for chaining purposes
	 */
	public ButtonInput<T> expireAfter(long timeout, TimeUnit unit) {
		this.expiryTime = timeout;
		this.expiryTimeUnit = unit;
		return this;
	}

	/**
	 * Sets whether or not the message should be edited to disable the buttons after the input expires.<br>
	 * <code>true</code> by default
	 * @param editOnExpire Whether to edit the message
	 * @return This input for chaining purposes
	 */
	public ButtonInput<T> editOnExpire(boolean editOnExpire) {
		this.editOnExpire = editOnExpire;
		return this;
	}
	
	/**
	 * Sets a callback that will be executed when the underlying event handler expires (the timeout can be changed using {@link #expireAfter(long, TimeUnit)})
	 * @param onExpire The callback
	 * @see #expireAfter(long, TimeUnit)
	 */
	public void setOnExpire(Consumer<ButtonInput<T>> onExpire) {
		this.onExpire = onExpire;
	}

	@EventHandler
	public void onInteract(ButtonInteractionEvent event) {
		if(System.currentTimeMillis() > expiresAt) return;
		if(!mappings.containsKey(event.getComponentId())) return;
		
		if(allowedUsers != null && !allowedUsers.stream().anyMatch(u -> u.getID().equals(event.getUser().getId()))) {
			event.deferReply(true).setContent("You can't interact with this").queue();
			return;
		}
		
		callback.accept(new ButtonInputEvent<>(this, event, Graphite.getUser(event.getUser()), mappings.get(event.getComponentId())));
		
		if(autoRemove || removeMessage) remove();
		if(!event.isAcknowledged()) {
			InteractionHook h = event.deferEdit().complete();
			if(autoRemove && (!removeMessage || event.getMessage() == null)) h.editOriginalComponents(ActionRow.of(Button.of(ButtonStyle.SUCCESS, "bi_done", "Done", JDAEmote.OK_HAND.getEmoji()).asDisabled())).queue();
			if(removeMessage && event.getMessage() != null && !event.getMessage().isEphemeral()) h.deleteOriginal().queue();
		}
	}
	
	public void addOption(ButtonStyle style, Emoji emoji, T option) {
		addOptionRaw(Button.of(style, newID(), emoji), option);
	}
	
	public void addOption(ButtonStyle style, JDAEmote emote, T option) {
		addOptionRaw(Button.of(style, newID(), emote.getEmoji()), option);
	}
	
	public void addOption(ButtonStyle style, String label, T option) {
		addOptionRaw(Button.of(style, newID(), label), option);
	}
	
	public void addOption(ButtonStyle style, Emoji emoji, String label, T option) {
		addOptionRaw(Button.of(style, newID(), label, emoji), option);
	}
	
	public void addOption(ButtonStyle style, JDAEmote emote, String label, T option) {
		addOptionRaw(Button.of(style, newID(), label, emote.getEmoji()), option);
	}
	
	public void addLink(JDAEmote emote, String url) {
		buttons.add(Button.of(ButtonStyle.LINK, url, emote.getEmoji()));
	}
	
	public void addLink(String label, String url) {
		buttons.add(Button.of(ButtonStyle.LINK, url, label));
	}
	
	public void addLink(JDAEmote emote, String label, String url) {
		buttons.add(Button.of(ButtonStyle.LINK, url, label, emote.getEmoji()));
	}
	
	public void newRow() {
		buttons.add(null);
	}
	
	private String newID() {
		return "bi_" + Long.toHexString(System.nanoTime());
	}
	
	public void addOptionRaw(Button button, T option) {
		buttons.add(button);
		mappings.put(button.getId(), option);
	}
	
	public void registerHandler() {
		expiresAt = System.currentTimeMillis() + expiryTimeUnit.toMillis(expiryTime);
		Graphite.getJDAListener().registerTemporaryHandler(this, editOnExpire ? () -> {
			if(message != null) message.editMessageComponents(createActionRows(true)).queue(m -> {}, t -> {});
			if(interactionHook != null) interactionHook.editOriginalComponents(createActionRows(true)).queue(m -> {}, t -> {});
			if(onExpire != null) onExpire.accept(this);
		} : null, expiryTime, expiryTimeUnit);
	}
	
	public void send(GraphiteMessageChannel<?> channel, DefaultLocaleString message, String... params) {
		send(channel, new MessageCreateBuilder().setContent(message.getFor(channel.getOwner(), params)));
	}
	
	public void send(GraphiteMessageChannel<?> channel, DefaultMessage message, String... params) {
		send(channel, new MessageCreateBuilder().setEmbeds(message.createEmbed(channel.getOwner(), params)));
	}
	
	public void send(GraphiteMessageChannel<?> channel, MessageCreateBuilder builder) {
		send(channel, builder, null);
	}
	
	public void send(GraphiteMessageChannel<?> channel, MessageCreateBuilder builder, Consumer<Message> onSuccess) {
		channel.getJDAChannel().sendMessage(builder.setComponents(createActionRows()).build()).queue(m -> {
			this.message = m;
			if(onSuccess != null) onSuccess.accept(m);
		});
		registerHandler();
	}
	
	public Message sendComplete(GraphiteMessageChannel<?> channel, DefaultLocaleString message, String... params) {
		return sendComplete(channel, new MessageCreateBuilder().setContent(message.getFor(channel.getOwner(), params)));
	}
	
	public Message sendComplete(GraphiteMessageChannel<?> channel, DefaultMessage message, String... params) {
		return sendComplete(channel, new MessageCreateBuilder().setEmbeds(message.createEmbed(channel.getOwner(), params)));
	}
	
	public Message sendComplete(GraphiteMessageChannel<?> channel, MessageCreateBuilder builder) {
		this.message = channel.sendMessageComplete(builder.setComponents(createActionRows()).build());
		registerHandler();
		return this.message;
	}
	
	public void reply(CommandInvokedEvent event, DefaultLocaleString message, String... params) {
		reply(event, new MessageCreateBuilder().setContent(message.getFor(event.getSender(), params)));
	}
	
	public void reply(CommandInvokedEvent event, DefaultMessage message, String... params) {
		reply(event, new MessageCreateBuilder().setEmbeds(message.createEmbed(event.getSender(), params)));
	}
	
	public void reply(CommandInvokedEvent event, MessageCreateBuilder builder) {
		reply(event, builder, null);
	}
	
	public void reply(CommandInvokedEvent event, MessageCreateBuilder builder, Consumer<Object> onSuccess) {
		event.reply(builder.setComponents(createActionRows()).build(), o -> {
			if(onSuccess != null) onSuccess.accept(o);
			if(o instanceof Message) {
				this.message = (Message) o;
			}else if(o instanceof InteractionHook) {
				this.interactionHook = (InteractionHook) o;
			}
		});
		registerHandler();
	}
	
	public void replyEphemeral(CommandInvokedEvent event, DefaultLocaleString message, String... params) {
		replyEphemeral(event, new MessageCreateBuilder().setContent(message.getFor(event.getSender(), params)));
	}
	
	public void replyEphemeral(CommandInvokedEvent event, DefaultMessage message, String... params) {
		replyEphemeral(event, new MessageCreateBuilder().setEmbeds(message.createEmbed(event.getSender(), params)));
	}
	
	public void replyEphemeral(CommandInvokedEvent event, MessageCreateBuilder builder) {
		replyEphemeral(event, builder, null);
	}
	
	public void replyEphemeral(CommandInvokedEvent event, MessageCreateBuilder builder, Consumer<Object> onSuccess) {
		if(removeMessage) throw new IllegalStateException("Ephemeral messages cannot be deleted");
		event.replyEphemeral(builder.setComponents(createActionRows()).build(), o -> {
			if(o instanceof Message) {
				this.message = (Message) o;
			}else if(o instanceof InteractionHook) {
				this.interactionHook = (InteractionHook) o;
			}
		});
		registerHandler();
	}
	
	public List<ActionRow> createActionRows(boolean isDisabled) {
		List<ActionRow> rows = new ArrayList<>();
		List<Button> bts = new ArrayList<>(buttons);
		List<Button> temp = new ArrayList<>();
		while(!bts.isEmpty()) {
			Button b = bts.remove(0);
			if(isDisabled) b = b.asDisabled();
			if(b == null) {
				rows.add(ActionRow.of(temp));
				temp = new ArrayList<>();
				continue;
			}
			temp.add(b);
		}
		rows.add(ActionRow.of(temp));
		return rows;
	}
	
	public void apply(MessageCreateBuilder builder) {
		builder.setComponents(createActionRows());
		registerHandler();
	}
	
	public void apply(MessageEditBuilder builder) {
		builder.setComponents(createActionRows());
		registerHandler();
	}
	
	public List<ActionRow> createActionRows() {
		return createActionRows(false);
	}
	
	@Override
	public void remove() {
		Graphite.getJDAListener().unregisterHandler(this);
	}

}

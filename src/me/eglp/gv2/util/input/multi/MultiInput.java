package me.eglp.gv2.util.input.multi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.user.GraphiteUser;
import me.eglp.gv2.util.base.GraphiteMessageChannel;
import me.eglp.gv2.util.command.CommandInvokedEvent;
import me.eglp.gv2.util.emote.JDAEmote;
import me.eglp.gv2.util.event.AnnotationEventHandler;
import me.eglp.gv2.util.event.EventHandler;
import me.eglp.gv2.util.input.GraphiteInput;
import me.eglp.gv2.util.lang.DefaultLocaleString;
import me.eglp.gv2.util.lang.DefaultMessage;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;

public class MultiInput implements GraphiteInput, AnnotationEventHandler {

	private static final Consumer<ButtonPressedEvent> SUBMIT_CALLBACK = event -> {
		Consumer<MultiInputSubmitEvent> cb = event.getInput().onSubmit;
		if(cb != null) cb.accept(new MultiInputSubmitEvent(event.getInput(), event));
		if(!event.getJDAEvent().isAcknowledged()) event.markDone();
		event.getInput().remove();
	};

	private List<GraphiteUser> allowedUsers;

	private List<ItemComponent> components;
	private Map<String, String> selectMenuIDs;
	private Map<String, List<String>> selectMenuValues;
	private Map<String, Consumer<ButtonPressedEvent>> buttonCallbacks;
	private Consumer<MultiInputSubmitEvent> onSubmit;

	private boolean
		editOnExpire;

	private long expiryTime;
	private TimeUnit expiryTimeUnit;

	private Consumer<MultiInput> onExpire;

	private long expiresAt;

	private Message message;
	private InteractionHook interactionHook;

	public MultiInput(List<GraphiteUser> allowedUsers) {
		this.allowedUsers = allowedUsers;
		this.editOnExpire = true;
		this.components = new ArrayList<>();
		this.selectMenuIDs = new HashMap<>();
		this.selectMenuValues = new HashMap<>();
		this.buttonCallbacks = new HashMap<>();
		this.expiryTime = 5;
		this.expiryTimeUnit = TimeUnit.MINUTES;
	}

	public MultiInput(GraphiteUser allowedUser) {
		this(Collections.singletonList(allowedUser));
	}

	/**
	 * Sets the timeout for when the listener should be removed<br>
	 * <code>1 TimeUnit.HOURS</code> by default
	 * @param timeout The timeout after which the listener should be removed
	 * @param unit The unit of the timeout
	 * @return This input for chaining purposes
	 */
	public MultiInput expireAfter(long timeout, TimeUnit unit) {
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
	public MultiInput editOnExpire(boolean editOnExpire) {
		this.editOnExpire = editOnExpire;
		return this;
	}

	/**
	 * Sets a callback that will be executed when the underlying event handler expires (the timeout can be changed using {@link #expireAfter(long, TimeUnit)})
	 * @param onExpire The callback
	 * @see #expireAfter(long, TimeUnit)
	 */
	public void setOnExpire(Consumer<MultiInput> onExpire) {
		this.onExpire = onExpire;
	}

	/**
	 * Sets a callback that will be executed when the input is submitted using a submit button
	 * @param onSubmit The callback
	 * @see {@link #addSubmit(ButtonStyle, Emoji)}
	 * @see {@link #addSubmit(ButtonStyle, String)}
	 * @see {@link #addSubmit(ButtonStyle, Emoji, String)}
	 */
	public void setOnSubmit(Consumer<MultiInputSubmitEvent> onSubmit) {
		this.onSubmit = onSubmit;
	}

	@EventHandler
	public void onButton(ButtonInteractionEvent event) {
		if(System.currentTimeMillis() > expiresAt) return;
		if(!buttonCallbacks.containsKey(event.getComponentId())) return;

		if(allowedUsers != null && !allowedUsers.stream().anyMatch(u -> u.getID().equals(event.getUser().getId()))) {
			event.deferReply(true).setContent("You can't interact with this").queue();
			return;
		}

		Consumer<ButtonPressedEvent> callback = buttonCallbacks.get(event.getComponentId());
		callback.accept(new ButtonPressedEvent(this, event, Graphite.getUser(event.getUser())));

		if(!event.isAcknowledged()) event.deferEdit().queue();
	}

	@EventHandler
	public void onSelect(StringSelectInteractionEvent event) {
		if(System.currentTimeMillis() > expiresAt) return;
		if(allowedUsers != null && !allowedUsers.stream().anyMatch(u -> u.getID().equals(event.getUser().getId()))) {
			event.deferReply(true).setContent("You can't interact with this").queue();
			return;
		}

		if(!selectMenuValues.containsKey(event.getComponentId())) return;

		selectMenuValues.put(event.getComponentId(), event.getValues());

		if(!event.isAcknowledged()) event.deferEdit().queue();
	}

	public void addButton(ButtonStyle style, Emoji emoji, Consumer<ButtonPressedEvent> callback) {
		addButtonRaw(Button.of(style, newID(), emoji), callback);
	}

	public void addButton(ButtonStyle style, String label, Consumer<ButtonPressedEvent> callback) {
		addButtonRaw(Button.of(style, newID(), label), callback);
	}

	public void addButton(ButtonStyle style, Emoji emoji, String label, Consumer<ButtonPressedEvent> callback) {
		addButtonRaw(Button.of(style, newID(), label, emoji), callback);
	}

	public void addLink(JDAEmote emote, String url) {
		components.add(Button.of(ButtonStyle.LINK, url, emote.getEmoji()));
	}

	public void addLink(String label, String url) {
		components.add(Button.of(ButtonStyle.LINK, url, label));
	}

	public void addLink(JDAEmote emote, String label, String url) {
		components.add(Button.of(ButtonStyle.LINK, url, label, emote.getEmoji()));
	}

	public void addSubmit(ButtonStyle style, Emoji emoji) {
		addButtonRaw(Button.of(style, newID(), emoji), SUBMIT_CALLBACK);
	}

	public void addSubmit(ButtonStyle style, String label) {
		addButtonRaw(Button.of(style, newID(), label), SUBMIT_CALLBACK);
	}

	public void addSubmit(ButtonStyle style, Emoji emoji, String label) {
		addButtonRaw(Button.of(style, newID(), label, emoji), SUBMIT_CALLBACK);
	}

	public void newRow() {
		components.add(null);
	}

	public void addButtonRaw(Button button, Consumer<ButtonPressedEvent> callback) {
		components.add(button);
		buttonCallbacks.put(button.getId(), callback);
	}

	public void addSelectMenu(String id, List<SelectOption> options) {
		addSelectMenu(id, options.toArray(SelectOption[]::new));
	}

	public void addSelectMenu(String id, SelectOption... options) {
		StringSelectMenu.Builder b = StringSelectMenu.create(newID());
		b.addOptions(options);
		components.add(b.build());
		selectMenuIDs.put(id, b.getId());
		selectMenuValues.put(b.getId(), Collections.emptyList());
	}

	private String newID() {
		return "mi_" + Long.toHexString(System.nanoTime());
	}

	public void registerHandler() {
		expiresAt = System.currentTimeMillis() + expiryTimeUnit.toMillis(expiryTime);
		Graphite.getJDAListener().registerTemporaryHandler(this, editOnExpire ? () -> {
			if(message != null) message.editMessageComponents(createActionRows(true)).queue(m -> {}, t -> {});
			if(interactionHook != null) interactionHook.editOriginalComponents(createActionRows(true)).queue(m -> {}, t -> {});
			if(onExpire != null) onExpire.accept(this);
		} : null, expiryTime, expiryTimeUnit);
	}

	public List<String> getSelectMenuValues(String id) {
		return selectMenuValues.get(selectMenuIDs.get(id));
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
		List<ItemComponent> comps = new ArrayList<>(components);
		List<ItemComponent> temp = new ArrayList<>();
		while(!comps.isEmpty()) {
			ItemComponent b = comps.remove(0);
			if(isDisabled) {
				if(b instanceof SelectMenu) b = ((SelectMenu) b).asDisabled();
				if(b instanceof Button) b = ((Button) b).asDisabled();
			}
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

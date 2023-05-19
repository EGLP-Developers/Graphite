package me.eglp.gv2.util.input;

import me.eglp.gv2.user.GraphiteUser;
import me.eglp.gv2.util.emote.JDAEmote;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;

public class ButtonInputEvent<T> {

	private ButtonInput<T> input;
	private ButtonInteractionEvent jdaEvent;
	private GraphiteUser user;
	private T item;

	public ButtonInputEvent(ButtonInput<T> input, ButtonInteractionEvent jdaEvent, GraphiteUser user, T item) {
		this.input = input;
		this.jdaEvent = jdaEvent;
		this.user = user;
		this.item = item;
	}

	public ButtonInput<T> getInput() {
		return input;
	}

	/**
	 * Returns the original JDA event.<br>
	 * If this event is acknowledged in any way, <code>autoRemove</code> and <code>removeMessage</code> will not work anymore!
	 * @return The original JDA event
	 */
	public ButtonInteractionEvent getJDAEvent() {
		return jdaEvent;
	}

	public GraphiteUser getUser() {
		return user;
	}

	public T getItem() {
		return item;
	}

	/**
	 * Replaces the buttons with a "Done" button.<br>
	 * This acknowledges the event. See {@link #getJDAEvent()} for more info.
	 */
	public void markDone() {
		jdaEvent.editComponents(ActionRow.of(Button.of(ButtonStyle.SUCCESS, "bi_done", "Done", JDAEmote.OK_HAND.getEmoji()).asDisabled())).queue();
	}

	/**
	 * Replaces the buttons with a "Cancelled" button.<br>
	 * This acknowledges the event. See {@link #getJDAEvent()} for more info.
	 */
	public void markCancelled() {
		jdaEvent.editComponents(ActionRow.of(Button.of(ButtonStyle.SECONDARY, "bi_cancelled", "Cancelled", JDAEmote.X.getEmoji()).asDisabled())).queue();
	}

	/**
	 * Acknowledges the event without doing anything.<br>
	 * This acknowledges the event. See {@link #getJDAEvent()} for more info.
	 */
	public void acknowledge() {
		jdaEvent.deferEdit().queue();
	}

}

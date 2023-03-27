package me.eglp.gv2.util.input.multi;

import me.eglp.gv2.util.base.user.GraphiteUser;
import me.eglp.gv2.util.emote.JDAEmote;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;

public class ButtonPressedEvent {

	private MultiInput input;
	private ButtonInteractionEvent jdaEvent;
	private GraphiteUser user;

	public ButtonPressedEvent(MultiInput input, ButtonInteractionEvent jdaEvent, GraphiteUser user) {
		this.input = input;
		this.jdaEvent = jdaEvent;
		this.user = user;
	}

	public MultiInput getInput() {
		return input;
	}

	/**
	 * Returns the original JDA event.<br>
	 * @return The original JDA event
	 */
	public ButtonInteractionEvent getJDAEvent() {
		return jdaEvent;
	}

	public GraphiteUser getUser() {
		return user;
	}
	
	/**
	 * Replaces the buttons with a "Done" button.
	 */
	public void markDone() {
		jdaEvent.editComponents(ActionRow.of(Button.of(ButtonStyle.SUCCESS, "mi_done", "Done", JDAEmote.OK_HAND.getEmoji()).asDisabled())).queue();
	}

	/**
	 * Replaces the buttons with a "Cancelled" button.
	 */
	public void markCancelled() {
		jdaEvent.editComponents(ActionRow.of(Button.of(ButtonStyle.SECONDARY, "mi_cancelled", "Cancelled", JDAEmote.X.getEmoji()).asDisabled())).queue();
	}

	/**
	 * Acknowledges the event without doing anything.
	 */
	public void acknowledge() {
		jdaEvent.deferEdit().queue();
	}
	
}

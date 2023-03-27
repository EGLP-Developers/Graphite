package me.eglp.gv2.util.command.slash;

import java.util.List;

import me.eglp.gv2.util.command.Command;
import me.eglp.gv2.util.event.AnnotationEventHandler;
import me.eglp.gv2.util.event.EventHandler;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;

public class CommandCompleteListener implements AnnotationEventHandler {

	@EventHandler
	public void onComplete(CommandAutoCompleteInteractionEvent event) {
		Command command = SlashCommandListener.getSlashCommand(event.getFullCommandName());
		List<Choice> completions = command.complete(event);
		completions.removeIf(c -> !c.getName().toLowerCase().contains(event.getFocusedOption().getValue().toLowerCase()));
		event.replyChoices(completions).queue();
	}

}

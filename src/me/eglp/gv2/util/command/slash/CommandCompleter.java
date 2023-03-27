package me.eglp.gv2.util.command.slash;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;

public interface CommandCompleter {
	
	public List<Choice> complete(CommandAutoCompleteInteractionEvent event);
	
	public static CommandCompleter ofString(Function<CommandAutoCompleteInteractionEvent, List<String>> function) {
		return event -> function.apply(event).stream().map(s -> new Choice(s, s)).collect(Collectors.toList());
	}
	
	public static CommandCompleter ofLong(Function<CommandAutoCompleteInteractionEvent, List<Long>> function) {
		return event -> function.apply(event).stream().map(l -> new Choice(String.valueOf(l), l)).collect(Collectors.toList());
	}
	
	public static CommandCompleter ofDouble(Function<CommandAutoCompleteInteractionEvent, List<Double>> function) {
		return event -> function.apply(event).stream().map(d -> new Choice(String.valueOf(d), d)).collect(Collectors.toList());
	}

}

package me.eglp.gv2.util.command;

import java.awt.Color;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.guild.GraphiteModule;
import me.eglp.gv2.util.base.GraphiteMessageChannel;
import me.eglp.gv2.util.command.slash.CommandCompleter;
import me.eglp.gv2.util.lang.DefaultLocaleString;
import me.eglp.gv2.util.lang.LocalizedString;
import me.eglp.gv2.util.lang.LocalizedStringImpl;
import me.eglp.gv2.util.versioning.Beta;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public abstract class Command {

	protected Command parent;
	private CommandCategory category;
	private String name, permission;
	private LocalizedString description, usage;
	protected List<Command> subCommands;
	private List<String> aliases;
	private GraphiteModule module;
	private Optional<Boolean> allowServer, allowPrivate, allowInGame;
	private boolean bypassQueue;
	private Set<Permission> requiredPermissions;

	// Slash-specific
	protected Map<String, CommandCompleter> completers;

	private Command(String name) {
		this.name = name;
		this.subCommands = new ArrayList<>();
		this.aliases = new ArrayList<>();
		this.allowServer = Optional.empty();
		this.allowPrivate = Optional.empty();
		this.allowInGame = Optional.empty();
		this.bypassQueue = false;
		this.completers = new HashMap<>();
		this.requiredPermissions = new HashSet<>();
	}

	public Command(GraphiteModule module, CommandCategory category, String name) {
		this(name);
		this.module = module;
		this.category = category;
	}

	public Command(Command parent, String name) {
		this(name);
		this.parent = parent;
		this.module = parent.module;
		this.category = parent.category;
	}

	public void registerCompleter(String optionName, CommandCompleter completer) {
		completers.put(optionName, completer);
	}

	public List<Choice> complete(CommandAutoCompleteInteractionEvent event) {
		var completer = completers.get(event.getFocusedOption().getName());
		if(completer == null) return Collections.emptyList();
		return completer.complete(event);
	}

	public Command setPermission(String permission) {
		this.permission = permission;
		return this;
	}

	public Command setDescription(LocalizedString description) {
		this.description = description;
		return this;
	}

	public Command setUsage(LocalizedString usage) {
		this.usage = usage;
		return this;
	}

	public Command setDescription(String description) {
		this.description = new LocalizedStringImpl(description);
		return this;
	}

	public Command setUsage(String usage) {
		this.usage = new LocalizedStringImpl(usage);
		return this;
	}

	public void setBypassQueue(boolean bypassQueue) {
		this.bypassQueue = bypassQueue;
	}

	public boolean isBypassQueue() {
		return bypassQueue;
	}

	public <T extends Command> T addSubCommand(T subCommand) {
		this.subCommands.add(subCommand);
		subCommand.parent = this;
		return subCommand;
	}

	public Command addAlias(String alias) {
		this.aliases.add(alias);
		return this;
	}

	public Command getParent() {
		return parent;
	}

	public String getName() {
		return name;
	}

	public String getFullName() {
		return (parent != null ? parent.getFullName() + " " : "") + name;
	}

	public String getPermission() {
		return permission;
	}

	public LocalizedString getDescription() {
		return description;
	}

	public LocalizedString getUsage() {
		return usage;
	}

	public List<Command> getSubCommands() {
		return subCommands;
	}

	public List<String> getAliases() {
		return aliases;
	}

	public GraphiteModule getModule() {
		return module;
	}

	public CommandCategory getCategory() {
		return category;
	}

	public Command setAllowServer(boolean allowServer) {
		this.allowServer = Optional.of(allowServer);
		return this;
	}

	public Command setAllowPrivate(boolean allowPrivate) {
		this.allowPrivate = Optional.of(allowPrivate);
		return this;
	}

	public Command setAllowInGame(boolean allowInGame) {
		this.allowInGame = Optional.of(allowInGame);
		return this;
	}

	public boolean allowsServer() {
		return allowServer.isPresent() ? allowServer.get() : (getAnnotation(SpecialExecute.class) == null || getAnnotation(SpecialExecute.class).allowServer());
	}

	public boolean allowsPrivate() {
		return allowPrivate.isPresent() ? allowPrivate.get() : (getAnnotation(SpecialExecute.class) != null && getAnnotation(SpecialExecute.class).allowPrivate());
	}

	public boolean allowsInGame() {
		return allowInGame.isPresent() ? allowInGame.get() : (getAnnotation(SpecialExecute.class) != null && getAnnotation(SpecialExecute.class).allowInGame());
	}

	public Command requirePermissions(Permission... permission) {
		requiredPermissions.addAll(Arrays.asList(permission));
		return this;
	}

	public Set<Permission> getRequiredPermissions() {
		Set<Permission> r = new HashSet<>();
		if(parent != null) r.addAll(parent.getRequiredPermissions());
		r.addAll(requiredPermissions);
		return r;
	}

	public boolean isBeta() {
		return getAnnotation(Beta.class) != null;
	}

	public abstract void action(CommandInvokedEvent event);

	public abstract List<OptionData> getOptions();

	public void sendCommandHelp(GraphiteMessageChannel<?> channel, boolean showFull) {
		channel.sendMessage(getCommandHelpFor(channel.getOwner(), showFull));
	}

	public void sendCommandHelp(GraphiteMessageChannel<?> channel) {
		sendCommandHelp(channel, false);
	}

	public <T extends CommandSender> MessageEmbed getCommandHelpFor(T it, boolean showFull) {
		String prefix = it.getPrefix();

		EmbedBuilder b = new EmbedBuilder();
		b.setColor(Color.YELLOW);
		b.addField(DefaultLocaleString.COMMAND_HELP_MESSAGE_COMMAND.getFor(it), prefix + getFullName(), false);
		if(getSubCommands().isEmpty()) {
			if(getUsage() != null) {
				b.addField(DefaultLocaleString.COMMAND_HELP_MESSAGE_USAGE.getFor(it), "`" + getUsage().getFor(it, "prefix", prefix) + "`", false);
			}else {
				b.addField("Usage Missing", DefaultLocaleString.COMMAND_HELP_MESSAGE_MISSING_USAGE.getFor(it), false);
			}
		}else {
			String sCs = getSubCommands().stream()
					.filter(c -> showFull || (it != null ? (it instanceof GraphiteGuild ? c.allowsServer() : c.allowsPrivate()) : true))
					.map(s -> s.getName())
					.collect(Collectors.joining(" | ", "<",">"));

			b.addField(DefaultLocaleString.COMMAND_HELP_MESSAGE_USAGE.getFor(it), "`" + DefaultLocaleString.COMMAND_HELP_MESSAGE_SUBCOMMANDS.getFor(it, "prefix", prefix, "command_name", getFullName(), "subcommands", sCs) + "`", false);
		}

		if(getPermission() != null) {
			b.addField(DefaultLocaleString.COMMAND_HELP_MESSAGE_PERMISSION.getFor(it), "`" + getPermission() + "`", false);
		}

		if(getDescription() != null) {
			b.addField(DefaultLocaleString.COMMAND_HELP_MESSAGE_DESCRIPTION.getFor(it), getDescription().getFor(it), false);
		}

		b.setFooter(DefaultLocaleString.COMMAND_HELP_MESSAGE_FOOTER.getFor(it, "prefix", prefix), null);
		return b.build();
	}

	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		try {
			return getClass().getMethod("action", CommandInvokedEvent.class).getAnnotation(annotationClass);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new RuntimeException();
		}
	}

}

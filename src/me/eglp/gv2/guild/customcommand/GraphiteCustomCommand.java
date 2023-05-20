package me.eglp.gv2.guild.customcommand;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.main.DebugCategory;
import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.main.GraphiteDebug;
import me.eglp.gv2.util.backup.IDMappings;
import me.eglp.gv2.util.base.GraphiteLocalizable;
import me.eglp.gv2.util.base.GraphiteMessageChannel;
import me.eglp.gv2.util.command.CommandInvokedEvent;
import me.eglp.gv2.util.command.CommandSender;
import me.eglp.gv2.util.command.text.CommandHandler;
import me.eglp.gv2.util.lang.DefaultLocaleString;
import me.eglp.gv2.util.webinterface.js.JavaScriptClass;
import me.eglp.gv2.util.webinterface.js.JavaScriptConstructor;
import me.eglp.gv2.util.webinterface.js.JavaScriptFunction;
import me.eglp.gv2.util.webinterface.js.JavaScriptParameter;
import me.eglp.gv2.util.webinterface.js.JavaScriptValue;
import me.eglp.gv2.util.webinterface.js.WebinterfaceObject;
import me.mrletsplay.mrcore.json.converter.JSONComplexListType;
import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONConvertible;
import me.mrletsplay.mrcore.json.converter.JSONValue;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.restaction.CommandCreateAction;

@JavaScriptClass(name = "CustomCommand")
public class GraphiteCustomCommand implements WebinterfaceObject, JSONConvertible {

	public static final Pattern
		NAME_PATTERN = Pattern.compile("[a-z0-9]{3,32}"),
		ARGUMENT_NAME_PATTERN = Pattern.compile("[a-z0-9]{3,32}");

	@JSONValue
	@JavaScriptValue(getter = "getName", setter = "setName")
	private String name;

	@JSONValue
	@JavaScriptValue(getter = "getPermission", setter = "setPermission")
	private String permission;

	@JSONValue
	@JSONComplexListType(CommandAction.class)
	@JavaScriptValue(getter = "getActions", setter = "setActions")
	private List<CommandAction> actions;

	private String slashCommandID;

	@JSONConstructor
	@JavaScriptConstructor
	private GraphiteCustomCommand() {}

	public GraphiteCustomCommand(String name, String permission, List<CommandAction> actions, String slashCommandID) {
		this.name = name;
		this.permission = permission;
		this.actions = actions;
		this.slashCommandID = slashCommandID;
	}

	public GraphiteCustomCommand(String name) {
		this(name, null, new ArrayList<>(), null);
	}

	public String getName() {
		return name;
	}

	public void setPermission(String permission) {
		this.permission = permission;
	}

	public String getPermission() {
		return permission;
	}

	public void addAction(CommandAction action) {
		actions.add(action);
	}

	public void removeAction(CommandAction action) {
		actions.remove(action);
	}

	public void setActions(List<CommandAction> actions) {
		this.actions = actions;
	}

	public List<CommandAction> getActions() {
		return actions;
	}

	public String getSlashCommandID() {
		return slashCommandID;
	}

	public void remapIDs(IDMappings mappings) {
		actions.forEach(a -> {
			a.getPropertyRefs().forEach(pr -> {
				if(pr.isArgument()) return;
				CommandActionProperty p = a.getType().getProperty(pr.getForProperty());
				if(p.getType() == CommandParameterType.ROLE
						|| p.getType() == CommandParameterType.TEXT_CHANNEL
						|| p.getType() == CommandParameterType.VOICE_CHANNEL) {
					if("sent-from".equals(pr.getValue())) return;
					pr.setValue(mappings.getNewID((String) pr.getValue()));
				}
			});
		});
	}

	@Override
	public GraphiteCustomCommand clone() {
		return new GraphiteCustomCommand(name, permission, actions, null);
	}

	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof GraphiteCustomCommand)) return false;
		GraphiteCustomCommand cmd = (GraphiteCustomCommand) obj;
		return cmd.getName().equals(name); // Commands are only identified by their name to allow updating (e.g. via webinterface)
	}

	@JavaScriptFunction(calling = "createCustomCommand", returning = "command", withGuild = true)
	public static void createCustomCommand(@JavaScriptParameter(name = "name") String name) {}

	@JavaScriptFunction(calling = "deleteCustomCommand", withGuild = true)
	public static void deleteCustomCommand(@JavaScriptParameter(name = "name") String name) {}

	@JavaScriptFunction(calling = "deleteAllCustomCommands", withGuild = true)
	public static void deleteAllCustomCommands() {}

	@JavaScriptFunction(calling = "getCustomCommands", returning = "commands", withGuild = true)
	public static void getCustomCommands() {}

	@JavaScriptFunction(calling = "getCustomCommandByName", returning = "command", withGuild = true)
	public static void getCustomCommandByName(@JavaScriptParameter(name = "name") String name) {}

	public void invoke(CommandInvokedEvent event) {
		List<CustomCommandInvokedEvent> evs = new ArrayList<>();
		for(CommandAction action : actions) {
			CustomCommandInvokedEvent e = CustomCommandInvokedEvent.create(event, this, action);
			if(e == null) return; // No message, create() sends message if necessary
			evs.add(e);
		}

		for(int i = 0; i < actions.size(); i++) {
			try {
				actions.get(i).getType().run(evs.get(i));
			}catch(Exception e) {
				if(!(e instanceof CustomCommandExecutionException)) GraphiteDebug.log(DebugCategory.CUSTOM_COMMAND, e);
				sendErrorMessage(event.getChannel(), e);
			}
		}

		if(event.isUsingSlashCommand() && !event.getJDASlashCommandEvent().isAcknowledged()) {
			event.replyEphemeral("CustomCommand successfully executed!");
		}
	}

	public static void sendErrorMessage(GraphiteMessageChannel<?> channel, Exception e) {
		if(e instanceof CustomCommandExecutionException) {
			channel.sendMessage(new EmbedBuilder()
					.setDescription(
							  "Exception occurred in action " + ((CustomCommandExecutionException) e).getEvent().getAction().getType() + "\n"
							+ "```\n" + e.getMessage() + "\n```")
					.build());
		}else {
			channel.sendMessage(new EmbedBuilder()
					.setDescription("Unexpected exception occured: ```\n" + e.toString() + "\n```")
					.build());
		}
	}

	public void sendCommandHelp(GraphiteMessageChannel<?> channel) {
		channel.sendMessage(getCommandHelpFor(channel.getOwner()));
	}

	public <T extends CommandSender> MessageEmbed getCommandHelpFor(T it) {
		EmbedBuilder b = new EmbedBuilder();
		b.setColor(Color.YELLOW);
		b.addField(DefaultLocaleString.COMMAND_HELP_MESSAGE_COMMAND.getFor(it), it.getPrefix() + getName(), false);

		b.addField(DefaultLocaleString.COMMAND_HELP_MESSAGE_USAGE.getFor(it), "`" + getUsage(it) + "`", false);

		if(permission != null) {
			b.addField(DefaultLocaleString.COMMAND_HELP_MESSAGE_PERMISSION.getFor(it), "`" + permission + "`", false);
		}

		b.addField(DefaultLocaleString.COMMAND_HELP_MESSAGE_DESCRIPTION.getFor(it), DefaultLocaleString.COMMAND_HELP_MESSAGE_CUSTOMCOMMAND_ACTIONS.getFor(it, "actions", actions.stream()
				.map(a -> a.getType().getDescription())
				.collect(Collectors.joining(", "))), false);

		b.setFooter(DefaultLocaleString.COMMAND_HELP_MESSAGE_FOOTER.getFor(it, "prefix", it.getPrefix()), null);
		return b.build();
	}

	private <T extends GraphiteLocalizable> String getUsage(T it) {
		Map<String, Set<String>> namesAndTypes = new LinkedHashMap<>();

		for(CommandAction ac : actions) {
			for(CommandActionPropertyRef r : ac.getPropertyRefs()) {
				if(!r.isArgument()) continue;
				CommandActionProperty prop = ac.getType().getProperty(r.getForProperty());
				Set<String> at = namesAndTypes.getOrDefault(r.getArgumentName(), new LinkedHashSet<>());
				at.add(prop.getFriendlyName());
				namesAndTypes.put(r.getArgumentName(), at);
			}
		}

		List<String> argDescs = new ArrayList<>();
		for(Map.Entry<String, Set<String>> nameAndType : namesAndTypes.entrySet()) {
			argDescs.add(nameAndType.getValue().stream().collect(Collectors.joining("/", "<", ">")));
		}

		return (it.isUser() ? Graphite.getBotInfo().getDefaultPrefix() : it.asGuild().getConfig().getPrefix()) + getName() + argDescs.stream()
					.collect(Collectors.joining(" ", " ", " "))
				+ "[...]";
	}

	public void createOrUpdateSlashCommand(GraphiteGuild guild) {
		if(CommandHandler.getCommandExact(name) != null) return; // CustomCommand would hide default command
		CommandCreateAction a = guild.getJDAGuild().upsertCommand(name, "CustomCommand");
		a.addOptions(getOptions());
		slashCommandID = a.complete().getId();
	}

	public void deleteSlashCommand(GraphiteGuild guild) {
		if(slashCommandID != null) guild.getJDAGuild().deleteCommandById(slashCommandID).queue(null, e -> {});
	}

	public List<OptionData> getOptions() {
		List<OptionData> dts = new ArrayList<>();
		for(CommandAction a : actions) {
			for(OptionData d : a.getOptions()) {
				OptionData d2 = dts.stream().filter(dt -> dt.getName().equals(d.getName())).findFirst().orElse(null);
				if(d2 != null) {
					if(d.getType() != d2.getType()) return null;
					continue;
				}

				dts.add(d);
			}
		}
		return dts;
	}

	public boolean checkValid() {
		return
			NAME_PATTERN.matcher(name).matches()
			&& (actions.stream().allMatch(a -> a.getType().getProperties().stream().allMatch(p -> a.getPropertyRefs().stream().anyMatch(r -> r.getForProperty() != null && r.getForProperty().equals(p.getName()))))
				|| getOptions() == null)
			&& (actions.stream().allMatch(a -> a.getPropertyRefs().stream().allMatch(p -> p.getArgumentName() == null || ARGUMENT_NAME_PATTERN.matcher(p.getArgumentName()).matches())));
	}

	@JavaScriptFunction(calling = "updateCustomCommand", withGuild = true)
	public static void updateCustomCommand(@JavaScriptParameter(name = "command") GraphiteCustomCommand command) {};

}

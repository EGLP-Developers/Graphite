package me.eglp.gv2.commands.admin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import me.eglp.gv2.guild.GraphiteModule;
import me.eglp.gv2.util.command.Command;
import me.eglp.gv2.util.command.CommandCategory;
import me.eglp.gv2.util.command.CommandInvokedEvent;
import me.eglp.gv2.util.command.ParentCommand;
import me.eglp.gv2.util.emote.JDAEmote;
import me.eglp.gv2.util.lang.DefaultLocaleString;
import me.eglp.gv2.util.lang.DefaultMessage;
import me.eglp.gv2.util.permission.DefaultPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class CommandModule extends ParentCommand {

	public CommandModule() {
		super(null, CommandCategory.ADMIN, "module");
		setDescription(DefaultLocaleString.COMMAND_MODULE_DESCRIPTION);

		addSubCommand(new Command(this, "enable") {

			@Override
			public void action(CommandInvokedEvent event) {
				String module = (String) event.getOption("module");
				GraphiteModule mod = GraphiteModule.getByName(module);
				if(mod == null) {
					DefaultMessage.COMMAND_MODULE_INVALID_MODULE.reply(event, "modules", Arrays.stream(GraphiteModule.values()).map(m -> m.getName()).collect(Collectors.joining(", ")));
					return;
				}

				if(event.getGuild().getConfig().hasModuleEnabled(mod)) {
					DefaultMessage.COMMAND_MODULE_ENABLE_ALREADY_ENABLED.reply(event);
					return;
				}

				event.getGuild().getConfig().addEnabledModule(mod);
				DefaultMessage.COMMAND_MODULE_ENABLE_SUCCESS.reply(event, "module", mod.getName());
			}

			@Override
			public List<OptionData> getOptions() {
				OptionData d = new OptionData(OptionType.STRING, "module", "The module to enable", true);
				for(GraphiteModule m : GraphiteModule.values()) {
					d.addChoice(m.getName(), m.getName());
				}
				return Arrays.asList(d);
			}
		})
		.setPermission(DefaultPermissions.ADMIN_MODULE_ENABLE)
		.setDescription(DefaultLocaleString.COMMAND_MODULE_ENABLE_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_MODULE_ENABLE_USAGE);

		addSubCommand(new Command(this, "disable") {

			@Override
			public void action(CommandInvokedEvent event) {
				String module = (String) event.getOption("module");
				GraphiteModule mod = GraphiteModule.getByName(module);
				if(mod == null) {
					DefaultMessage.COMMAND_MODULE_INVALID_MODULE.reply(event, "modules", Arrays.stream(GraphiteModule.values()).map(m -> m.getName()).collect(Collectors.joining(", ")));
					return;
				}

				if(!event.getGuild().getConfig().hasModuleEnabled(mod)) {
					DefaultMessage.COMMAND_MODULE_DISABLE_NOT_ENABLED.reply(event);
					return;
				}

				event.getGuild().getConfig().removeEnabledModule(mod);
				DefaultMessage.COMMAND_MODULE_DISABLE_SUCCESS.reply(event, "module", mod.getName());
			}

			@Override
			public List<OptionData> getOptions() {
				OptionData d = new OptionData(OptionType.STRING, "module", "The module to disable", true);
				for(GraphiteModule m : GraphiteModule.values()) {
					d.addChoice(m.getName(), m.getName());
				}
				return Arrays.asList(d);
			}
		})
		.setPermission(DefaultPermissions.ADMIN_MODULE_DISABLE)
		.setDescription(DefaultLocaleString.COMMAND_MODULE_DISABLE_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_MODULE_DISABLE_USAGE);

		addSubCommand(new Command(this, "list") {

			@Override
			public void action(CommandInvokedEvent event) {
				Set<GraphiteModule> enabled = event.getGuild().getConfig().getEnabledModules();

				List<String> lines = new ArrayList<>();
				for(GraphiteModule m : GraphiteModule.values()) {
					lines.add((enabled.contains(m) ? JDAEmote.WHITE_CHECK_MARK.getUnicode() : JDAEmote.X.getUnicode()) + " " + m.getName());
				}

				DefaultMessage.COMMAND_MODULE_LIST_MESSAGE.reply(event, "modules", lines.stream().collect(Collectors.joining("\n")));
			}

			@Override
			public List<OptionData> getOptions() {
				return Collections.emptyList();
			}
		})
		.setPermission(DefaultPermissions.ADMIN_MODULE_LIST)
		.setDescription(DefaultLocaleString.COMMAND_MODULE_LIST_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_MODULE_LIST_USAGE);
	}

}

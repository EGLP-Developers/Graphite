package me.eglp.gv2.commands.scripting;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import me.eglp.gv2.guild.GraphiteModule;
import me.eglp.gv2.util.command.Command;
import me.eglp.gv2.util.command.CommandCategory;
import me.eglp.gv2.util.command.CommandInvokedEvent;
import me.eglp.gv2.util.command.ParentCommand;
import me.eglp.gv2.util.input.ButtonInput;
import me.eglp.gv2.util.lang.DefaultLocaleString;
import me.eglp.gv2.util.lang.DefaultMessage;
import me.eglp.gv2.util.permission.DefaultPermissions;
import me.eglp.gv2.util.scripting.GraphiteScript;
import me.mrletsplay.mrcore.http.HttpRequest;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.utils.FileUpload;

public class CommandScript extends ParentCommand {

	public static final Pattern SCRIPT_NAME_PATTERN = Pattern.compile("(\\w|-){1,32}");

	public CommandScript() {
		super(GraphiteModule.SCRIPTING, CommandCategory.SCRIPTING, "script");
		setDescription(DefaultLocaleString.COMMAND_SCRIPT_DESCRIPTION);

		addSubCommand(new Command(this, "upload") {

			@Override
			public void action(CommandInvokedEvent event) {
				String name = (String) event.getOption("name");
				Attachment file = (Attachment) event.getOption("file");

				Matcher m = SCRIPT_NAME_PATTERN.matcher(name);
				if(!m.matches()) {
					DefaultMessage.COMMAND_SCRIPT_UPLOAD_INVALID_SHORT.reply(event);
					return;
				}

				if(file.getSize() > 8192) {
					DefaultMessage.COMMAND_SCRIPT_UPLOAD_TOO_LARGE.reply(event);
					return;
				}

				byte[] bytes = HttpRequest.createGet(file.getUrl()).execute().asRaw();

				if(event.getGuild().getScripts().getScript(name) != null) {
					ButtonInput<Integer> inp = new ButtonInput<Integer>(event.getAuthor(), ev -> {
						if(ev.getItem() == -1) {
							var result = event.getGuild().getScripts().addOrReplaceScript(name, bytes);
							if(!result.isPresent()) {
								DefaultMessage.COMMAND_SCRIPT_UPLOAD_INVALID_FILE.reply(event, "error", result.getException().getMessage());
								return;
							}

							DefaultMessage.COMMAND_SCRIPT_UPLOAD_SUCCESS.reply(event);
						}
					})
					.autoRemove(true)
					.removeMessage(true);

					inp.addOption(ButtonStyle.DANGER, "Overwrite", -1); // NONBETA: msg
					inp.addOption(ButtonStyle.SECONDARY, "Cancel", -2);
					inp.replyEphemeral(event, DefaultLocaleString.COMMAND_SCRIPT_UPLOAD_ALREADY_EXISTS);
					return;
				}

				var result = event.getGuild().getScripts().addOrReplaceScript(name, bytes);
				if(!result.isPresent()) {
					DefaultMessage.COMMAND_SCRIPT_UPLOAD_INVALID_FILE.reply(event, "error", result.getException().getMessage());
					return;
				}

				DefaultMessage.COMMAND_SCRIPT_UPLOAD_SUCCESS.reply(event);
			}

			@Override
			public List<OptionData> getOptions() {
				return Arrays.asList(
						new OptionData(OptionType.STRING, "name", "The name of script", true),
						new OptionData(OptionType.ATTACHMENT, "file", "The script file to upload", true)
					);
			}
		})
		.setDescription(DefaultLocaleString.COMMAND_SCRIPT_UPLOAD_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_SCRIPT_UPLOAD_USAGE)
		.setPermission(DefaultPermissions.SCRIPT_UPLOAD);

		addSubCommand(new Command(this, "delete") {

			@Override
			public void action(CommandInvokedEvent event) {
				GraphiteScript s = event.getGuild().getScripts().getScript((String) event.getOption("script"));
				if(s == null) {
					DefaultMessage.COMMAND_SCRIPT_DELETE_INVALID_SCRIPT.reply(event);
					return;
				}

				event.getGuild().getScripts().removeScript(s.getName());
				DefaultMessage.COMMAND_SCRIPT_DELETE_SUCCESS.reply(event);
			}

			@Override
			public List<OptionData> getOptions() {
				return Arrays.asList(
						new OptionData(OptionType.STRING, "script", "The script you want to delete", true)
					);
			}

		})
		.setDescription(DefaultLocaleString.COMMAND_SCRIPT_DELETE_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_SCRIPT_DELETE_USAGE)
		.setPermission(DefaultPermissions.SCRIPT_DELETE);

		addSubCommand(new Command(this, "list") {

			@Override
			public void action(CommandInvokedEvent event) {
				DefaultMessage.COMMAND_SCRIPT_LIST_MESSAGE.reply(event, "scripts", event.getGuild().getScripts().getScripts().stream().map(s -> s.getName()).collect(Collectors.joining(", ")));
			}

			@Override
			public List<OptionData> getOptions() {
				return Collections.emptyList();
			}

		})
		.setDescription(DefaultLocaleString.COMMAND_SCRIPT_LIST_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_SCRIPT_LIST_USAGE)
		.setPermission(DefaultPermissions.SCRIPT_LIST);

		addSubCommand(new Command(this, "download") {

			@Override
			public void action(CommandInvokedEvent event) {
				if(event.isFromGuild() && !event.getTextChannel().canAttachFiles()) {
					DefaultMessage.ERROR_LACKING_PERMISSION.reply(event, "permission", Permission.MESSAGE_ATTACH_FILES.getName());
					return;
				}

				String scriptName = (String) event.getOption("script");

				GraphiteScript script = event.getGuild().getScripts().getScript(scriptName);
				if(script == null) {
					event.reply("E"); // NONBETA: msg
					return;
				}

				event.getChannel().sendFiles(FileUpload.fromData(script.getContent().getBytes(StandardCharsets.UTF_8), scriptName + ".js"));
				event.reply("Yay!"); // NONBETA: msg
			}

			@Override
			public List<OptionData> getOptions() {
				return Arrays.asList(
						new OptionData(OptionType.STRING, "script", "The script to download", true)
					);
			}
		})
		.setDescription(DefaultLocaleString.COMMAND_SCRIPT_DOWNLOAD_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_SCRIPT_DOWNLOAD_USAGE)
		.setPermission(DefaultPermissions.SCRIPT_DOWNLOAD);
	}

}

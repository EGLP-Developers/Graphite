package me.eglp.gv2.commands.admin;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.command.Command;
import me.eglp.gv2.util.command.CommandCategory;
import me.eglp.gv2.util.command.CommandInvokedEvent;
import me.eglp.gv2.util.command.ParentCommand;
import me.eglp.gv2.util.input.ButtonInput;
import me.eglp.gv2.util.lang.DefaultLocaleString;
import me.eglp.gv2.util.lang.DefaultMessage;
import me.eglp.gv2.util.lang.GraphiteLocale;
import me.eglp.gv2.util.permission.DefaultPermissions;
import me.mrletsplay.mrcore.config.ConfigException;
import me.mrletsplay.mrcore.config.ConfigLoader;
import me.mrletsplay.mrcore.config.ConfigValueType;
import me.mrletsplay.mrcore.config.CustomConfig;
import me.mrletsplay.mrcore.http.HttpRequest;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.utils.FileUpload;

public class CommandLocale extends ParentCommand {

	private static final Pattern LOCALE_NAME_PATTERN = Pattern.compile("\\w{1,16}");

	public CommandLocale() {
		super(null, CommandCategory.ADMIN, "locale");
		setDescription(DefaultLocaleString.COMMAND_LOCALE_DESCRIPTION);
		setPermission(DefaultPermissions.ADMIN_LOCALE);

		addSubCommand(new Command(this, "list") {

			@Override
			public void action(CommandInvokedEvent event) {
				Set<String> locales = event.getGuild().getLocale().getAvailableLocales();
				EmbedBuilder b = new EmbedBuilder();
				b.setColor(Color.YELLOW);
				b.addField("Available Locales", "`" + (locales.isEmpty() ? "(none)" : locales.stream().collect(Collectors.joining(", "))) + "`", false);
				b.addField("Current Locale", "`" + event.getGuild().getConfig().getLocale() + "`", false);
				event.reply(b.build());
			}

			@Override
			public List<OptionData> getOptions() {
				return Collections.emptyList();
			}
		})
		.setDescription(DefaultLocaleString.COMMAND_LOCALE_LIST_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_LOCALE_LIST_USAGE)
		.setPermission(DefaultPermissions.ADMIN_LOCALE_LIST);

		addSubCommand(new Command(this, "set") {

			@Override
			public void action(CommandInvokedEvent event) {
				String locale = (String) event.getOption("locale");
				if(!event.getGuild().hasLocale(locale)) {
					DefaultMessage.COMMAND_LOCALE_INVALID_LOCALE.reply(event);
					return;
				}

				event.getGuild().getConfig().setLocale(locale);
				DefaultMessage.COMMAND_LOCALE_SET_MESSAGE.reply(event, "locale", locale);
			}

			@Override
			public List<OptionData> getOptions() {
				return Arrays.asList(
						new OptionData(OptionType.STRING, "locale", "The new locale to use", true)
					);
			}
		})
		.setDescription(DefaultLocaleString.COMMAND_LOCALE_SET_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_LOCALE_SET_USAGE)
		.setPermission(DefaultPermissions.ADMIN_LOCALE_SET);

		addSubCommand(new Command(this, "download") {

			@Override
			public void action(CommandInvokedEvent event) {
				if(event.isFromGuild() && !event.getTextChannel().canAttachFiles()) {
					DefaultMessage.ERROR_LACKING_PERMISSION.reply(event, "permission", Permission.MESSAGE_ATTACH_FILES.getName());
					return;
				}

				String localeName = (String) event.getOption("locale");
				ByteArrayOutputStream o = new ByteArrayOutputStream();
				if(localeName == null) {
					localeName = "en";
					Graphite.generateDefaultLocale().save(o);
				}else {
					if(!event.getGuild().getLocale().hasLocale(localeName)) {
						DefaultMessage.COMMAND_LOCALE_INVALID_LOCALE.reply(event);
						return;
					}

					event.getGuild().getLocale().generateLocaleFile(localeName).save(o);
				}

				byte[] b = o.toByteArray();
				event.getChannel().sendFiles(FileUpload.fromData(b, localeName + ".yml"));

				EmbedBuilder eb = new EmbedBuilder();
				eb.addField(
						DefaultLocaleString.COMMAND_LOCALE_DOWNLOAD_TITLE.getFor(event.getSender()),
						DefaultLocaleString.COMMAND_LOCALE_DOWNLOAD_VALUES.getFor(event.getSender(), "prefix", event.getGuild().getPrefix()), true);
				eb.setFooter(DefaultLocaleString.COMMAND_LOCALE_DOWNLOAD_FOOTER.getFor(event.getSender(), "amount", ""+Graphite.getCustomizableMessageAmount()), null);
				event.reply(eb.build());
			}

			@Override
			public List<OptionData> getOptions() {
				return Arrays.asList(
						new OptionData(OptionType.STRING, "locale", "The locale to download")
					);
			}
		})
		.setDescription(DefaultLocaleString.COMMAND_LOCALE_DOWNLOAD_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_LOCALE_DOWNLOAD_USAGE)
		.setPermission(DefaultPermissions.ADMIN_LOCALE_DOWNLOAD)
		.requirePermissions(Permission.MESSAGE_ATTACH_FILES);

		addSubCommand(new Command(this, "upload") {

			@Override
			public void action(CommandInvokedEvent event) {
				String name = (String) event.getOption("name");
				Attachment at = (Attachment) event.getOption("file");

				Matcher m = LOCALE_NAME_PATTERN.matcher(name);
				if(!m.matches()) {
					DefaultMessage.COMMAND_LOCALE_UPLOAD_INVALID_SHORT.reply(event);
					return;
				}

				if(event.getGuild().hasLocale(name)) {
					ButtonInput<Integer> inp = new ButtonInput<Integer>(event.getAuthor(), ev -> {
						if(ev.getItem() == -1) {
							createLocale(event, name, at.getUrl());
							DefaultMessage.COMMAND_LOCALE_UPLOAD_SUCCESS.reply(event, "prefix", event.getPrefixUsed(), "locale", name);
						}
					})
					.autoRemove(true)
					.removeMessage(true);

					inp.addOption(ButtonStyle.DANGER, "Overwrite", -1); // NONBETA: msg
					inp.addOption(ButtonStyle.SECONDARY, "Cancel", -2);
					inp.replyEphemeral(event, DefaultLocaleString.COMMAND_LOCALE_UPLOAD_ALREADY_EXISTS);
					return;
				}

				createLocale(event, name, at.getUrl());
				DefaultMessage.COMMAND_LOCALE_UPLOAD_SUCCESS.reply(event, "prefix", event.getPrefixUsed(), "locale", name);
			}

			@Override
			public List<OptionData> getOptions() {
				return Arrays.asList(
						new OptionData(OptionType.STRING, "name", "The name of the new locale", true),
						new OptionData(OptionType.ATTACHMENT, "file", "The locale file to upload", true)
					);
			}
		})
		.setDescription(DefaultLocaleString.COMMAND_LOCALE_UPLOAD_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_LOCALE_UPLOAD_USAGE)
		.setPermission(DefaultPermissions.ADMIN_LOCALE_UPLOAD);

		addSubCommand(new Command(this, "delete") {

			@Override
			public void action(CommandInvokedEvent event) {
				GraphiteGuild guild = event.getGuild();
				String localeIdentifier = (String) event.getOption("");
				if(!guild.hasLocale(localeIdentifier)) {
					DefaultMessage.COMMAND_LOCALE_DELETE_INVALID_LOCALE.reply(event);
					return;
				}

				if(guild.getConfig().getLocale().equals(localeIdentifier)) {
					guild.getConfig().setLocale(GraphiteLocale.DEFAULT_LOCALE_KEY);
				}

				guild.getLocale().deleteLocale(localeIdentifier);

				DefaultMessage.COMMAND_LOCALE_DELETE_SUCCESS.reply(event, "locale", localeIdentifier);
			}

			@Override
			public List<OptionData> getOptions() {
				return Arrays.asList(
						new OptionData(OptionType.STRING, "locale", "The name of the locale to delete", true)
					);
			}
		})
		.setDescription(DefaultLocaleString.COMMAND_LOCALE_DELETE_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_LOCALE_DELETE_USAGE)
		.setPermission(DefaultPermissions.ADMIN_LOCALE_DELETE);
	}

	private void createLocale(CommandInvokedEvent event, String locale, String fileURL) {
		byte[] bytes = HttpRequest.createGet(fileURL).execute().asRaw();
		try {
			Map<String, String> messages = new HashMap<>();

			CustomConfig c = ConfigLoader.loadStreamConfig(new ByteArrayInputStream(bytes), true);
			for(String key : c.getKeys(true, true)) {
				if(c.getTypeOf(key) != ConfigValueType.STRING) continue;
				String v = c.getString(key);

				DefaultLocaleString s = DefaultLocaleString.getByPath(key);
				if(s != null) {
					if(s.getFallback().equals(v)) continue; // Ignore the values left at default
					messages.put(key, v);
					continue;
				}

				DefaultMessage dm = DefaultMessage.getByPath(key);
				if(dm != null) {
					if(dm.getFallback().equals(v)) continue; // Ignore the values left at default
					messages.put(key, v);
					continue;
				}

				// Ignore invalid values
			}

			event.getGuild().getLocale().createOrOverrideLocale(locale, messages);
		}catch(ConfigException e) {
			e.printStackTrace();
			DefaultMessage.COMMAND_LOCALE_UPLOAD_INVALID_FILE.reply(event);
			return;
		}
	}

}

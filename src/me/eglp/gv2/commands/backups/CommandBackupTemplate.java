package me.eglp.gv2.commands.backups;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.GraphiteIcon;
import me.eglp.gv2.util.backup.GuildBackup;
import me.eglp.gv2.util.backup.RestoreSelector;
import me.eglp.gv2.util.backup.TemplateBackup;
import me.eglp.gv2.util.command.Command;
import me.eglp.gv2.util.command.CommandInvokedEvent;
import me.eglp.gv2.util.command.ParentCommand;
import me.eglp.gv2.util.command.slash.DeferredReply;
import me.eglp.gv2.util.lang.DefaultLocaleString;
import me.eglp.gv2.util.lang.DefaultMessage;
import me.eglp.gv2.util.lang.LocalizedTimeUnit;
import me.eglp.gv2.util.permission.DefaultPermissions;
import me.eglp.gv2.util.queue.GraphiteQueue;
import me.eglp.gv2.util.queue.GraphiteTaskInfo;
import me.eglp.gv2.util.queue.QueueTask;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class CommandBackupTemplate extends ParentCommand {

	public static final List<RestoreSelector> FORBIDDEN_PARAMETERS = Arrays.asList(
			RestoreSelector.DISCORD_CHAT_HISTORY,
			RestoreSelector.DISCORD_THREAD_CHAT_HISTORY,
			RestoreSelector.DISCORD_ROLE_ASSIGNMENTS,
			RestoreSelector.DISCORD_BANS
		);

	public CommandBackupTemplate(Command parent) {
		super(parent, "template");
		setDescription(DefaultLocaleString.COMMAND_BACKUP_TEMPLATE_DESCRIPTION);

		addSubCommand(new Command(this, "create") {

			@Override
			public void action(CommandInvokedEvent event) {
				long cd = TemplateBackup.getTemplateCreateCooldown(event.getAuthor());
				if(cd > 0) {
					DefaultMessage.COMMAND_TEMPLATE_CREATE_COOLDOWN.reply(event, "time", LocalizedTimeUnit.formatTime(event.getGuild(), cd));
					return;
				}

				EmbedBuilder eb = new EmbedBuilder();
				GuildBackup b = event.getGuild().getBackupByName((String) event.getOption("backup"));
				if(b == null) {
					eb.setAuthor(DefaultLocaleString.COMMAND_BACKUP_CANT_FIND_BACKUP_TITLE.getFor(event.getSender()), null, GraphiteIcon.ERROR.getPath());
					eb.setDescription(DefaultLocaleString.COMMAND_BACKUP_CANT_FIND_BACKUP_VALUE.getFor(event.getSender()));
					event.reply(eb.build());
					return;
				}

				String name = (String) event.getOption("name");
				String description = (String) event.getOption("description");

				if(name.length() > TemplateBackup.MAX_TEMPLATE_NAME_LENGTH) {
					eb.setAuthor(DefaultLocaleString.COMMAND_BACKUP_TEMPLATE_NAME_TOO_LONG.getFor(event.getSender(), "max_chars", String.valueOf(TemplateBackup.MAX_TEMPLATE_NAME_LENGTH)), null, GraphiteIcon.ERROR.getPath());
					event.reply(eb.build());
					return;
				}

				if(description != null && description.length() > TemplateBackup.MAX_TEMPLATE_DESCRIPTION_LENGTH) {
					eb.setAuthor(DefaultLocaleString.COMMAND_BACKUP_TEMPLATE_DESCRIPTION_TOO_LONG.getFor(event.getSender(), "max_chars", String.valueOf(TemplateBackup.MAX_TEMPLATE_DESCRIPTION_LENGTH)), null, GraphiteIcon.ERROR.getPath());
					event.reply(eb.build());
					return;
				}

				TemplateBackup template = TemplateBackup.createNew(b, event.getAuthor(), name, description);

				eb.setAuthor(DefaultLocaleString.COMMAND_BACKUP_TEMPLATE_CREATED_TITLE.getFor(event.getSender()), null, GraphiteIcon.CHECKMARK.getPath());
				eb.setDescription(DefaultLocaleString.COMMAND_BACKUP_TEMPLATE_CREATED_VALUE.getFor(event.getSender(), "template_id", String.valueOf(template.getID())));
				event.reply(eb.build());
			}

			@Override
			public List<OptionData> getOptions() {
				return Arrays.asList(
						new OptionData(OptionType.STRING, "backup", "The backup you want to create a template from", true),
						new OptionData(OptionType.STRING, "name", "A short name for your template", true),
						new OptionData(OptionType.STRING, "description", "A short but informative description", true)
					);
			}

		})
		.setDescription(DefaultLocaleString.COMMAND_BACKUP_TEMPLATE_CREATE_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_BACKUP_TEMPLATE_CREATE_USAGE)
		.setPermission(DefaultPermissions.BACKUP_TEMPLATE_CREATE);

		addSubCommand(new Command(this, "delete") {

			@Override
			public void action(CommandInvokedEvent event) {
				EmbedBuilder eb = new EmbedBuilder();
				TemplateBackup b = TemplateBackup.getTemplateByID((String) event.getOption("template"));
				if(b == null) {
					eb.setAuthor(DefaultLocaleString.COMMAND_BACKUP_CANT_FIND_BACKUP_TITLE.getFor(event.getSender()), null, GraphiteIcon.ERROR.getPath());
					eb.setDescription(DefaultLocaleString.COMMAND_BACKUP_CANT_FIND_BACKUP_VALUE.getFor(event.getSender()));
					event.reply(eb.build());
					return;
				}

				if(!b.getAuthor().equals(event.getMember())) {
					eb.setAuthor(DefaultLocaleString.COMMAND_BACKUP_TEMPLATE_CANT_DELETE_TITLE.getFor(event.getSender()), null, GraphiteIcon.ERROR.getPath());
					eb.setDescription(DefaultLocaleString.COMMAND_BACKUP_TEMPLATE_CANT_DELETE_VALUE.getFor(event.getSender(), "discord", Graphite.getBotInfo().getLinks().getDiscord()));
					event.reply(eb.build());
					return;
				}

				b.delete();

				eb.setAuthor(DefaultLocaleString.COMMAND_BACKUP_TEMPLATE_DELETED.getFor(event.getSender()), null, GraphiteIcon.CHECKMARK.getPath());
				event.reply(eb.build());
			}

			@Override
			public List<OptionData> getOptions() {
				return Arrays.asList(
						new OptionData(OptionType.STRING, "template", "The template you want to delete", true)
					);
			}

		})
		.setDescription(DefaultLocaleString.COMMAND_BACKUP_TEMPLATE_DELETE_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_BACKUP_TEMPLATE_DELETE_USAGE)
		.setPermission(DefaultPermissions.BACKUP_TEMPLATE_DELETE);

		addSubCommand(new Command(this, "load") {

			@Override
			public void action(CommandInvokedEvent event) {
				EmbedBuilder eb = new EmbedBuilder();

				TemplateBackup b = TemplateBackup.getTemplateByID((String) event.getOption("template"));
				if(b == null) {
					eb.setAuthor(DefaultLocaleString.COMMAND_BACKUP_CANT_FIND_BACKUP_TITLE.getFor(event.getSender()), null, GraphiteIcon.ERROR.getPath());
					eb.setDescription(DefaultLocaleString.COMMAND_BACKUP_CANT_FIND_BACKUP_VALUE.getFor(event.getSender()));
					event.reply(eb.build());
					return;
				}

				DeferredReply r = event.deferReply();
				CommandBackup.selectParameters(event.getAuthor(), event, false, true, params -> {
					if(params.contains(RestoreSelector.DISCORD_ROLES) && !event.getGuild().isAboveUserRoles()) {
						eb.setAuthor(DefaultLocaleString.COMMAND_BACKUP_NOT_HIGHEST_ROLE_TITLE.getFor(event.getSender()), null, GraphiteIcon.ERROR.getPath());
						eb.setDescription(DefaultLocaleString.COMMAND_BACKUP_NOT_HIGHEST_ROLE_VALUE.getFor(event.getSender()));
						r.editOriginal(eb.build());
						return;
					}

					GraphiteQueue q = Graphite.getQueue();
					if(q.isHeavyBusy()) DefaultMessage.OTHER_HEAVY_BUSY.reply(event, "patreon", Graphite.getBotInfo().getLinks().getPatreon());
					QueueTask<Long> tm = q.queueHeavy(event.getGuild(), new GraphiteTaskInfo(GuildBackup.TASK_ID,  "Load template backup (backup template)"), () -> b.restore(event.getGuild(), params));
					tm.thenAccept(t -> {
						if(t == -1) {
							eb.setAuthor(DefaultLocaleString.COMMAND_BACKUP_TEMPLATE_LOAD_FAILED_TITLE.getFor(event.getSender()), null, GraphiteIcon.ERROR.getPath());
							eb.setDescription(DefaultLocaleString.ERROR_TRY_AGAIN.getFor(event.getSender(), "discord", Graphite.getBotInfo().getLinks().getDiscord()));
							r.editOriginal(eb.build());
							return;
						}
					}).exceptionally(e -> {
						if(tm.isCancelled()) return null;
						r.editOriginal(DefaultMessage.ERROR_EXCEPTION.createEmbed(event.getSender(), "error_message", e.getMessage()));
						return null;
					});
				});
			}

			@Override
			public List<OptionData> getOptions() {
				return Arrays.asList(
						new OptionData(OptionType.STRING, "template", "The template you want to load", true)
					);
			}
		})
		.setDescription(DefaultLocaleString.COMMAND_BACKUP_TEMPLATE_LOAD_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_BACKUP_TEMPLATE_LOAD_USAGE)
		.setPermission(DefaultPermissions.BACKUP_TEMPLATE_LOAD);

		addSubCommand(new Command(this, "search") {

			@Override
			public void action(CommandInvokedEvent event) {
				String query = (String) event.getOption("query");
				List<TemplateBackup> results = TemplateBackup.getTemplateBackups().stream()
						.filter(b -> b.getName().toLowerCase().contains(query.toLowerCase())
								|| (b.getDescription() != null && b.getDescription().toLowerCase().contains(query.toLowerCase())))
						.limit(20)
						.collect(Collectors.toList());
				EmbedBuilder eb = new EmbedBuilder();
				eb.setTitle(DefaultLocaleString.COMMAND_BACKUP_TEMPLATE_SEARCH_RESULTS_TITLE.getFor(event.getAuthor(), "query", query));
				results.forEach(r -> {
					eb.addField(DefaultLocaleString.COMMAND_BACKUP_TEMPLATE_SEARCH_RESULTS_FIELD_TITLE.getFor(event.getAuthor(), "name", r.getName()),
								DefaultLocaleString.COMMAND_BACKUP_TEMPLATE_SEARCH_RESULTS_FIELD_VALUE.getFor(event.getAuthor(), "id", r.getID(), "description", r.getDescription() == null ? "" : r.getDescription()), false);
				});
				event.reply(eb.build());
			}

			@Override
			public List<OptionData> getOptions() {
				return Arrays.asList(
						new OptionData(OptionType.STRING, "query", "A search query which template you search for")
					);
			}

		})
		.setDescription(DefaultLocaleString.COMMAND_BACKUP_TEMPLATE_SEARCH_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_BACKUP_TEMPLATE_SEARCH_USAGE)
		.setPermission(DefaultPermissions.BACKUP_TEMPLATE_SEARCH);
	}

}

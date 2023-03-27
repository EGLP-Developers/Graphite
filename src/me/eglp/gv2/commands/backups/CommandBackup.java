package me.eglp.gv2.commands.backups;

import java.awt.Color;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.GraphiteIcon;
import me.eglp.gv2.util.backup.GuildBackup;
import me.eglp.gv2.util.backup.RestoreSelector;
import me.eglp.gv2.util.backup.data.channels.ChannelsData;
import me.eglp.gv2.util.backup.data.roles.RolesData;
import me.eglp.gv2.util.base.guild.GraphiteGuild;
import me.eglp.gv2.util.base.guild.GraphiteModule;
import me.eglp.gv2.util.base.user.GraphiteUser;
import me.eglp.gv2.util.command.Command;
import me.eglp.gv2.util.command.CommandCategory;
import me.eglp.gv2.util.command.CommandInvokedEvent;
import me.eglp.gv2.util.command.ParentCommand;
import me.eglp.gv2.util.command.slash.CommandCompleter;
import me.eglp.gv2.util.command.slash.DeferredReply;
import me.eglp.gv2.util.crypto.GraphiteCrypto;
import me.eglp.gv2.util.emote.JDAEmote;
import me.eglp.gv2.util.input.ButtonInput;
import me.eglp.gv2.util.lang.DefaultLocaleString;
import me.eglp.gv2.util.lang.DefaultMessage;
import me.eglp.gv2.util.lang.LocalizedTimeUnit;
import me.eglp.gv2.util.permission.DefaultPermissions;
import me.eglp.gv2.util.queue.GraphiteQueue;
import me.eglp.gv2.util.queue.GraphiteTaskInfo;
import me.eglp.gv2.util.queue.QueueTask;
import me.eglp.gv2.util.webinterface.base.GraphiteWebinterfaceUser;
import me.mrletsplay.mrcore.http.HttpRequest;
import me.mrletsplay.mrcore.misc.NullableOptional;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

public class CommandBackup extends ParentCommand {
	
	public static final Pattern PARAMS_PATTERN = Pattern.compile("([+-])\\[(.*?)\\]");
	public static final Pattern BACKUP_NAME_PATTERN = Pattern.compile("(?:\\w| |-){1,64}");

	public CommandBackup() {
		super(GraphiteModule.BACKUPS, CommandCategory.BACKUPS, "backup");
		setDescription(DefaultLocaleString.COMMAND_BACKUP_DESCRIPTION);
		
		addSubCommand(new Command(this, "create") {
			
			@Override
			public void action(CommandInvokedEvent event) {
				EmbedBuilder eb = new EmbedBuilder();
				if(!event.getGuild().canCreateBackup()) {
					eb.setAuthor(DefaultLocaleString.COMMAND_BACKUP_LIMIT_REACHED_TITLE.getFor(event.getSender()), null, GraphiteIcon.ERROR.getPath());
					eb.setDescription(DefaultLocaleString.COMMAND_BACKUP_LIMIT_REACHED_VALUE.getFor(event.getSender(), "patreon", Graphite.getMainBotInfo().getLinks().getPatreon()));
					event.reply(eb.build());
					return;
				}
				
				int messageCount = 20;
				
				if(event.hasOption("messages")) {
					long messages = (long) event.getOption("messages");
					if(messages < 0 || messages > 100) {
						eb.setAuthor(DefaultLocaleString.COMMAND_BACKUP_TOO_MANY_MESSAGES_TITLE.getFor(event.getSender()), null, GraphiteIcon.ERROR.getPath());
						eb.setDescription(DefaultLocaleString.COMMAND_BACKUP_TOO_MANY_MESSAGES_VALUE.getFor(event.getSender()));
						event.reply(eb.build());
						return;
					}
					messageCount = (int) messages;
				}
				
				eb.setAuthor(
						DefaultLocaleString.COMMAND_BACKUP_CREATING_TITLE.getFor(event.getSender()),
						null,
						GraphiteIcon.LOADING_GIF.getPath());
				eb.setDescription(DefaultLocaleString.COMMAND_BACKUP_CREATING_VALUE.getFor(event.getSender()));
				DeferredReply d = event.deferReply(eb.build());
				
				KeyPair kp = messageCount > 0 ? GraphiteCrypto.generateKeyPair() : null;
				GuildBackup b = event.getGuild().createBackup(kp != null ? kp.getPublic() : null, messageCount, false);
				
				if(kp != null) {
					byte[] privateKeyData = kp.getPrivate().getEncoded();
					
					MessageEmbed embed = new EmbedBuilder()
							.setDescription(DefaultLocaleString.COMMAND_BACKUP_CREATE_KEY_DESCRIPTION.getFor(event.getSender(), "faq", Graphite.getMainBotInfo().getWebsite().getFAQURL()))
							.setColor(Color.ORANGE)
							.build();
					
					FileUpload file = FileUpload.fromData(privateKeyData, "backup-" + event.getGuild().getID() + "-" + b.getName() + ".key");
					
					event.getAuthorChannel().getJDAChannel()
						.sendFiles(file)
						.setEmbeds(embed)
						.queue(null, new ErrorHandler()
								.handle(ErrorResponse.CANNOT_SEND_TO_USER, ex -> event.getChannel().getJDAChannel()
										.sendFiles(file)
										.setEmbeds(embed)
										.queue()));
				}
				
				EmbedBuilder eb2 = new EmbedBuilder();
				eb2.setAuthor(
						DefaultLocaleString.COMMAND_BACKUP_CREATED_TITLE.getFor(event.getSender()),
						null,
						GraphiteIcon.CHECKMARK.getPath());
				eb2.setDescription(DefaultLocaleString.COMMAND_BACKUP_CREATED_VALUE.getFor(event.getSender(), "backup_name", b.getName()));
				eb2.addField(DefaultLocaleString.COMMAND_BACKUP_CREATED_FIELD_1_TITLE.getFor(event.getSender()), 
						DefaultLocaleString.COMMAND_BACKUP_CREATED_FIELD_1_VALUE.getFor(event.getSender(), "prefix", event.getPrefixUsed(), "webinterface", Graphite.getMainBotInfo().getWebsite().getWebinterfaceURL()), false);
				d.editOriginal(eb2.build());
			}
			
			@Override
			public List<OptionData> getOptions() {
				return Arrays.asList(
						new OptionData(OptionType.INTEGER, "messages", "The amount of messages you want to save. Max: 100", false).setRequiredRange(0, 100)
					);
			}
		})
		.setDescription(DefaultLocaleString.COMMAND_BACKUP_CREATE_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_BACKUP_CREATE_USAGE)
		.setPermission(DefaultPermissions.BACKUP_CREATE);
		
		addSubCommand(new Command(this, "cancel") {
			
			@Override
			public void action(CommandInvokedEvent event) {
				EmbedBuilder eb = new EmbedBuilder();
				GraphiteQueue q = event.getGuild().getResponsibleQueue();
				GraphiteTaskInfo i;
				if(!q.isHeavyBusy(event.getGuild()) || !(i = q.getHeavyTask(event.getGuild())).getID().equals(GuildBackup.TASK_ID)) {
					eb.setAuthor(DefaultLocaleString.COMMAND_BACKUP_CANCEL_FAILURE_TITLE.getFor(event.getSender()), null, GraphiteIcon.ERROR.getPath());
					eb.setDescription(DefaultLocaleString.COMMAND_BACKUP_CANCEL_FAILURE_VALUE.getFor(event.getSender()));
					event.reply(eb.build());
					return;
				}
				i.getTask().cancel();
				eb.setAuthor(DefaultLocaleString.COMMAND_BACKUP_CANCELLED_TITLE.getFor(event.getSender()), null, GraphiteIcon.CHECKMARK.getPath());
				eb.setDescription(DefaultLocaleString.COMMAND_BACKUP_CANCELLED_VALUE.getFor(event.getSender()));
				event.reply(eb.build());
			}
			
			@Override
			public List<OptionData> getOptions() {
				return Collections.emptyList();
			}
		})
		.setDescription(DefaultLocaleString.COMMAND_BACKUP_CANCEL_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_BACKUP_CANCEL_USAGE)
		.setPermission(DefaultPermissions.BACKUP_CANCEL)
		.setBypassQueue(true);
		
		Command restore = addSubCommand(new Command(this, "restore") {
			
			@Override
			public void action(CommandInvokedEvent event) {
				String name = (String) event.getOption("name");
				Attachment key = (Attachment) event.getOption("key");
				
				long cd = event.getGuild().getBackupCooldown();
				if(cd > 0) {
					EmbedBuilder eb = new EmbedBuilder();
					eb.setAuthor(DefaultLocaleString.COMMAND_BACKUP_RATELIMIT_TITLE.getFor(event.getSender()), null, GraphiteIcon.ERROR.getPath());
					eb.setDescription(DefaultLocaleString.COMMAND_BACKUP_RATELIMIT_VALUE.getFor(event.getSender(), "time", LocalizedTimeUnit.formatTime(event.getGuild(), cd)));
					event.replyEphemeral(eb.build());
					return;
				}
				
				GuildBackup b = event.getGuild().getBackupByName(name);
				if(b == null) {
					EmbedBuilder eb = new EmbedBuilder();
					eb.setAuthor(DefaultLocaleString.COMMAND_BACKUP_CANT_FIND_BACKUP_TITLE.getFor(event.getSender()), null, GraphiteIcon.ERROR.getPath());
					eb.setDescription(DefaultLocaleString.COMMAND_BACKUP_CANT_FIND_BACKUP_VALUE.getFor(event.getSender()));
					event.replyEphemeral(eb.build());
					return;
				}

				PrivateKey pk = null;
				
				if(key != null) {
					byte[] bs = HttpRequest.createGet(key.getUrl()).execute().asRaw();
					pk = GraphiteCrypto.decodePrivateKey(bs);
					
					if(pk == null) {
						EmbedBuilder eb = new EmbedBuilder();
						eb.setAuthor(DefaultLocaleString.COMMAND_BACKUP_INVALID_KEY_TITLE.getFor(event.getSender()), null, GraphiteIcon.ERROR.getPath());
						eb.setDescription(DefaultLocaleString.COMMAND_BACKUP_INVALID_KEY_VALUE.getFor(event.getSender()));
						event.replyEphemeral(eb.build());
						return;
					}
					
					if(!b.isCorrectKey(pk)) {
						EmbedBuilder eb = new EmbedBuilder();
						eb.setAuthor(DefaultLocaleString.COMMAND_BACKUP_INVALID_KEY_TITLE.getFor(event.getSender()), null, GraphiteIcon.ERROR.getPath());
						eb.setDescription(DefaultLocaleString.COMMAND_BACKUP_INVALID_KEY_VALUE.getFor(event.getSender()));
						event.replyEphemeral(eb.build());
						return;
					}
				}
				
				final PrivateKey decryptionKey = pk;
				
				selectParameters(event.getAuthor(), event, decryptionKey != null, false, params -> {
					if(params.contains(RestoreSelector.DISCORD_ROLES) && !event.getGuild().isAboveUserRoles()) {
						EmbedBuilder eb = new EmbedBuilder();
						eb.setAuthor(DefaultLocaleString.COMMAND_BACKUP_NOT_HIGHEST_ROLE_TITLE.getFor(event.getSender()), null, GraphiteIcon.ERROR.getPath());
						eb.setDescription(DefaultLocaleString.COMMAND_BACKUP_NOT_HIGHEST_ROLE_VALUE.getFor(event.getSender()));
						event.replyEphemeral(eb.build());
						return;
					}
					
					GraphiteQueue q = Graphite.getQueue(event.getGuild());
					if(q.isHeavyBusy()) DefaultMessage.OTHER_HEAVY_BUSY.reply(event, "patreon", Graphite.getMainBotInfo().getLinks().getPatreon());
					QueueTask<Long> tm = q.queueHeavy(event.getGuild(), new GraphiteTaskInfo(GuildBackup.TASK_ID,  "Restoring backup (backup restore)"), () -> b.restore(event.getGuild(), decryptionKey, params));
					tm.thenAccept(t -> {
						if(t == -1) {
							EmbedBuilder eb = new EmbedBuilder();
							eb.setAuthor(DefaultLocaleString.COMMAND_BACKUP_RESTORING_FAILED_TITLE.getFor(event.getSender()), null, GraphiteIcon.ERROR.getPath());
							eb.setDescription(DefaultLocaleString.ERROR_TRY_AGAIN.getFor(event.getSender(), "discord", Graphite.getMainBotInfo().getLinks().getDiscord()));
							event.reply(eb.build());
							return;
						}

						event.getAuthor().openPrivateChannel().sendMessage(DefaultMessage.COMMAND_BACKUP_RESTORED, "time_taken", LocalizedTimeUnit.formatTime(event.getGuild(), t));
					}).exceptionally(e -> {
						if(tm.isCancelled()) return null;
						DefaultMessage.ERROR_EXCEPTION.reply(event, "error_message", e.getMessage());
						return null;
					});
				});
			}
			
			@Override
			public List<OptionData> getOptions() {
				return Arrays.asList(
						new OptionData(OptionType.STRING, "name", "The name of the backup", true, true),
						new OptionData(OptionType.ATTACHMENT, "key", "The decryption key for the backup", false)
					);
			}
		});
		restore.setDescription(DefaultLocaleString.COMMAND_BACKUP_RESTORE_DESCRIPTION)
			.setUsage(DefaultLocaleString.COMMAND_BACKUP_RESTORE_USAGE)
			.setPermission(DefaultPermissions.BACKUP_RESTORE);
		restore.registerCompleter("name", CommandCompleter.ofString(event -> {
			return Graphite.getGuild(event.getGuild()).getBackups().stream()
					.map(b -> b.getName())
					.collect(Collectors.toList());
		}));

		Command copy = addSubCommand(new Command(this, "copy") {
			
			@Override
			public void action(CommandInvokedEvent event) {
				String fromID = (String) event.getOption("from");
				String name = (String) event.getOption("name");
				Attachment key = (Attachment) event.getOption("key");
				
				GraphiteGuild from = Graphite.getGuild(fromID);
				
				if(from == null) {
					EmbedBuilder eb = new EmbedBuilder();
					eb.setAuthor(DefaultLocaleString.COMMAND_BACKUP_COPY_CANT_FIND_SERVER_TITLE.getFor(event.getSender()), null, GraphiteIcon.ERROR.getPath());
					eb.setDescription(DefaultLocaleString.COMMAND_BACKUP_COPY_CANT_FIND_SERVER_VALUE.getFor(event.getSender()));
					event.replyEphemeral(eb.build());
					return;
				}
				
				boolean hasPerm = from.getPermissionManager().hasPermission(event.getAuthor(), DefaultPermissions.BACKUP_COPY_TO_OTHER);
				if(!hasPerm) {
					EmbedBuilder eb = new EmbedBuilder();
					eb.setAuthor(DefaultLocaleString.COMMAND_BACKUP_COPY_MISSING_PERMISSION_TITLE.getFor(event.getSender()), null, GraphiteIcon.ERROR.getPath());
					eb.setDescription(DefaultLocaleString.COMMAND_BACKUP_COPY_MISSING_PERMISSION_VALUE.getFor(event.getSender()));
					event.replyEphemeral(eb.build());
					return;
				}
				
				GuildBackup b = from.getBackupByName(name);
				if(b == null) {
					EmbedBuilder eb = new EmbedBuilder();
					eb.setAuthor(DefaultLocaleString.COMMAND_BACKUP_CANT_FIND_BACKUP_TITLE.getFor(event.getSender()), null, GraphiteIcon.ERROR.getPath());
					eb.setDescription(DefaultLocaleString.COMMAND_BACKUP_CANT_FIND_BACKUP_VALUE.getFor(event.getSender()));
					event.replyEphemeral(eb.build());
					return;
				}
				
				PrivateKey pk = null;
				
				if(key != null) {
					byte[] bs = HttpRequest.createGet(key.getUrl()).execute().asRaw();
					pk = GraphiteCrypto.decodePrivateKey(bs);
					
					if(pk == null) {
						EmbedBuilder eb = new EmbedBuilder();
						eb.setAuthor(DefaultLocaleString.COMMAND_BACKUP_INVALID_KEY_TITLE.getFor(event.getSender()), null, GraphiteIcon.ERROR.getPath());
						eb.setDescription(DefaultLocaleString.COMMAND_BACKUP_INVALID_KEY_VALUE.getFor(event.getSender()));
						event.replyEphemeral(eb.build());
						return;
					}
					
					if(!b.isCorrectKey(pk)) {
						EmbedBuilder eb = new EmbedBuilder();
						eb.setAuthor(DefaultLocaleString.COMMAND_BACKUP_INVALID_KEY_TITLE.getFor(event.getSender()), null, GraphiteIcon.ERROR.getPath());
						eb.setDescription(DefaultLocaleString.COMMAND_BACKUP_INVALID_KEY_VALUE.getFor(event.getSender()));
						event.replyEphemeral(eb.build());
						return;
					}
				}
				
				final PrivateKey decryptionKey = pk;
				
				EmbedBuilder eb = new EmbedBuilder();
				eb.setAuthor(
						DefaultLocaleString.COMMAND_BACKUP_CREATING_TITLE.getFor(event.getSender()),
						null,
						GraphiteIcon.LOADING_GIF.getPath());
				eb.setDescription(DefaultLocaleString.COMMAND_BACKUP_CREATING_VALUE.getFor(event.getSender()));
				DeferredReply d = event.deferReply(eb.build());
				
				KeyPair kp = decryptionKey != null && b.hasMessagesData() ? GraphiteCrypto.generateKeyPair() : null;
				GuildBackup copy = GuildBackup.saveCopy(event.getGuild(), b, decryptionKey, kp != null ? kp.getPublic() : null);
				
				if(kp != null) {
					byte[] privateKeyData = kp.getPrivate().getEncoded();
					
					MessageEmbed embed = new EmbedBuilder()
							.setDescription(DefaultLocaleString.COMMAND_BACKUP_CREATE_KEY_DESCRIPTION.getFor(event.getSender(), "faq", Graphite.getMainBotInfo().getWebsite().getFAQURL()))
							.setColor(Color.ORANGE)
							.build();
					
					FileUpload file = FileUpload.fromData(privateKeyData, "backup-" + event.getGuild().getID() + "-" + copy.getName() + ".key");
					
					event.getAuthorChannel().getJDAChannel()
						.sendFiles(file)
						.setEmbeds(embed)
						.queue(null, new ErrorHandler()
								.handle(ErrorResponse.CANNOT_SEND_TO_USER, ex -> event.getChannel().getJDAChannel()
										.sendFiles(file)
										.setEmbeds(embed)
										.queue()));
				}
				
				EmbedBuilder eb2 = new EmbedBuilder();
				eb2.setAuthor(
						DefaultLocaleString.COMMAND_BACKUP_COPIED_TITLE.getFor(event.getSender()),
						null,
						GraphiteIcon.CHECKMARK.getPath());
				eb2.setDescription(DefaultLocaleString.COMMAND_BACKUP_CREATED_VALUE.getFor(event.getSender(), "backup_name", copy.getName()));
				eb2.addField(DefaultLocaleString.COMMAND_BACKUP_CREATED_FIELD_1_TITLE.getFor(event.getSender()), 
						DefaultLocaleString.COMMAND_BACKUP_CREATED_FIELD_1_VALUE.getFor(event.getSender(), "prefix", event.getPrefixUsed(), "webinterface", Graphite.getMainBotInfo().getWebsite().getWebinterfaceURL()), false);
				d.editOriginal(eb2.build());
			}
			
			@Override
			public List<OptionData> getOptions() {
				return Arrays.asList(
						new OptionData(OptionType.STRING, "from", "The guild to copy the backup from", true, true),
						new OptionData(OptionType.STRING, "name", "The name of the backup", true, true),
						new OptionData(OptionType.ATTACHMENT, "key", "The decryption key for the backup", false)
					);
			}
		});
		copy.setDescription(DefaultLocaleString.COMMAND_BACKUP_COPY_DESCRIPTION)
			.setUsage(DefaultLocaleString.COMMAND_BACKUP_COPY_USAGE)
			.setPermission(DefaultPermissions.BACKUP_COPY);
		copy.registerCompleter("name", CommandCompleter.ofString(event -> {
			return Graphite.getGuild(event.getGuild()).getBackups().stream()
					.map(b -> b.getName())
					.collect(Collectors.toList());
		}));
		copy.registerCompleter("to", event -> {
			GraphiteWebinterfaceUser user = Graphite.getWebinterfaceUser(event.getUser().getId());
			if(user == null) return Collections.emptyList();
			return user.getGuilds().stream()
					.map(g -> new Choice(g.getName(), g.getID()))
					.collect(Collectors.toList());
		});
		
		addSubCommand(new Command(this, "delete") {
			
			@Override
			public void action(CommandInvokedEvent event) {
				EmbedBuilder eb = new EmbedBuilder();
				GuildBackup b = event.getGuild().getBackupByName((String) event.getOption("backup"));
				if(b == null) {
					eb.setAuthor(DefaultLocaleString.COMMAND_BACKUP_CANT_FIND_BACKUP_TITLE.getFor(event.getSender()), null, GraphiteIcon.ERROR.getPath());
					eb.setDescription(DefaultLocaleString.COMMAND_BACKUP_CANT_FIND_BACKUP_VALUE.getFor(event.getSender()));
					event.reply(eb.build());
					return;
				}
				b.delete();
				eb.setAuthor(DefaultLocaleString.COMMAND_BACKUP_DELETED.getFor(event.getSender()), null, GraphiteIcon.CHECKMARK.getPath());
				event.reply(eb.build());
			}
			
			@Override
			public List<OptionData> getOptions() {
				return Arrays.asList(
						new OptionData(OptionType.STRING, "backup", "The backup you want to delete", true)
					);
			}
			
		})
		.setDescription(DefaultLocaleString.COMMAND_BACKUP_DELETE_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_BACKUP_DELETE_USAGE)
		.setPermission(DefaultPermissions.BACKUP_DELETE);
		
		addSubCommand(new Command(this, "info") {
			
			@Override
			public void action(CommandInvokedEvent event) {
				EmbedBuilder eb = new EmbedBuilder();
				GuildBackup b = event.getGuild().getBackupByName((String) event.getOption("backup"));
				if(b == null) {
					eb.setAuthor(DefaultLocaleString.COMMAND_BACKUP_CANT_FIND_BACKUP_TITLE.getFor(event.getSender()), null, GraphiteIcon.ERROR.getPath());
					eb.setDescription(DefaultLocaleString.COMMAND_BACKUP_CANT_FIND_BACKUP_VALUE.getFor(event.getSender()));
					event.reply(eb.build());
					return;
				}
				
				eb.setAuthor(DefaultLocaleString.COMMAND_BACKUP_INFO_MESSAGE_AUTHOR.getFor(event.getSender(), "backup_name", b.getName()), null, GraphiteIcon.INFORMATION.getPath());
				
				ChannelsData channels = b.loadChannelsData();
				RolesData rolesD = b.loadRolesData();
				
				String tlTc = channels.getTextChannels().isEmpty() ? "" : channels.getTextChannels().stream().map(m -> "# " + m.getName()).collect(Collectors.joining("\n", "", "\n"));
				String tlNc = channels.getNewsChannels().isEmpty() ? "" : channels.getNewsChannels().stream().map(m -> JDAEmote.MEGA.getUnicode() + " " + m.getName()).collect(Collectors.joining("\n", "", "\n"));
				String tlVc = channels.getVoiceChannels().isEmpty() ? "" : channels.getVoiceChannels().stream().map(m -> JDAEmote.LOUD_SOUND.getUnicode() + " " + m.getName()).collect(Collectors.joining("\n", "", "\n"));
				String tlSc = channels.getStageChannels().isEmpty() ? "" : channels.getStageChannels().stream().map(m -> JDAEmote.MICROPHONE.getUnicode() + " " + m.getName()).collect(Collectors.joining("\n", "", "\n"));
				
				String cats = channels.getCategories().stream().map(m -> {
						return
								"> " + m.getName() + "\n" +
								(m.getTextChannels().isEmpty() ? "" : m.getTextChannels().stream().map(c -> "    # " + c.getName()).collect(Collectors.joining("\n", "", "\n"))) +
								(m.getNewsChannels().isEmpty() ? "" : m.getNewsChannels().stream().map(c -> "    " + JDAEmote.MEGA.getUnicode() + " " + c.getName()).collect(Collectors.joining("\n", "", "\n"))) +
								(m.getVoiceChannels().isEmpty() ? "" : m.getVoiceChannels().stream().map(c -> "    " + JDAEmote.LOUD_SOUND.getUnicode() + " " + c.getName()).collect(Collectors.joining("\n", "", "\n"))) +
								(m.getStageChannels().isEmpty() ? "" : m.getStageChannels().stream().map(c -> "    " + JDAEmote.MICROPHONE.getUnicode() + " " + c.getName()).collect(Collectors.joining("\n", "", "\n")));
					}).collect(Collectors.joining());	
				
				String txt = tlTc + tlNc + tlVc + tlSc + cats;
				
				eb.addField(DefaultLocaleString.COMMAND_BACKUP_INFO_CHANNEL_FIELD_NAME.getFor(event.getSender()), 
							"```" + (txt.length() > MessageEmbed.VALUE_MAX_LENGTH - 6 ? txt.substring(0, MessageEmbed.VALUE_MAX_LENGTH - 9) + "..." : txt) + "```",
							true);
				
				eb.addBlankField(true);
				
				String roles = rolesD.getRoles().stream().map(m -> m.getName()).collect(Collectors.joining("\n"));
				
				String txt2 = DefaultLocaleString.COMMAND_BACKUP_INFO_ROLE_FIELD_VALUE.getFor(event.getSender(), "roles", roles);
				
				eb.addField(DefaultLocaleString.COMMAND_BACKUP_INFO_ROLE_FIELD_NAME.getFor(event.getSender()),
						"```" + (txt2.length() > MessageEmbed.VALUE_MAX_LENGTH - 6 ? txt2.substring(0, MessageEmbed.VALUE_MAX_LENGTH - 9) + "..." : txt2) + "```",
							true);
				event.reply(eb.build());
			}
			
			@Override
			public List<OptionData> getOptions() {
				return Arrays.asList(
						new OptionData(OptionType.STRING, "backup", "The backup you want to show information about", true)
					);
			}
		})
		.setDescription(DefaultLocaleString.COMMAND_BACKUP_INFO_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_BACKUP_INFO_USAGE)
		.setPermission(DefaultPermissions.BACKUP_INFO);
		
		addSubCommand(new Command(this, "list") {
			
			@Override
			public void action(CommandInvokedEvent event) {
				EmbedBuilder eb = new EmbedBuilder();
				List<GuildBackup> backups = event.getGuild().getBackups();
				if(backups.isEmpty()) {
					eb.setAuthor(DefaultLocaleString.COMMAND_BACKUP_NO_BACKUPS_TITLE.getFor(event.getSender()), null, GraphiteIcon.ERROR.getPath());
					event.reply(eb.build());
					return;
				}
				event.reply(DefaultLocaleString.COMMAND_BACKUP_LIST_LIST.getFor(event.getSender(), "backups", backups.stream().map(GuildBackup::getName).collect(Collectors.joining("\n"))));
			}
			
			@Override
			public List<OptionData> getOptions() {
				return Collections.emptyList();
			}
			
		})
		.setDescription(DefaultLocaleString.COMMAND_BACKUP_LIST_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_BACKUP_LIST_USAGE)
		.setPermission(DefaultPermissions.BACKUP_LIST);
		
		addSubCommand(new Command(this, "interval") {
			
			@Override
			public void action(CommandInvokedEvent event) {
				EmbedBuilder eb = new EmbedBuilder();
				long intv = (long) event.getOption("interval");
				if(intv <= 0 || intv > 1000) {
					eb.setAuthor(DefaultLocaleString.COMMAND_BACKUP_INVALID_INTERVAL.getFor(event.getSender()), null, GraphiteIcon.ERROR.getPath());
					event.reply(eb.build());
					return;
				}
				
				event.getGuild().getBackupConfig().setBackupInterval((int) intv);
				eb.setAuthor(DefaultLocaleString.COMMAND_BACKUP_INTERVAL_TITLE.getFor(event.getSender()), null, GraphiteIcon.CHECKMARK.getPath());
				eb.setDescription(DefaultLocaleString.COMMAND_BACKUP_INTERVAL_VALUE.getFor(event.getSender(), "interval", String.valueOf(intv)));
				event.reply(eb.build());
			}
			
			@Override
			public List<OptionData> getOptions() {
				return Arrays.asList(
						new OptionData(OptionType.INTEGER, "interval", "The backup interval in days", true)
							.addChoice("every day", 1)
							.addChoice("2 days", 2)
							.addChoice("7 days", 7)
							.addChoice("14 days", 14)
							.addChoice("30 days", 30)
					);
			}
		})
		.setDescription(DefaultLocaleString.COMMAND_BACKUP_INTERVAL_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_BACKUP_INTERVAL_USAGE)
		.setPermission(DefaultPermissions.BACKUP_INTERVAL);
		
		addSubCommand(new Command(this, "rename") {
			
			@Override
			public void action(CommandInvokedEvent event) {
				String name = (String) event.getOption("name");
				String newName = (String) event.getOption("new-name");
				
				if(!BACKUP_NAME_PATTERN.matcher(newName).matches()) {
					DefaultMessage.COMMAND_BACKUP_RENAME_INVALID_NAME.reply(event);
					return;
				}
				
				GraphiteGuild g = event.getGuild();
				if(GuildBackup.getBackupByName(g, name) == null) {
					DefaultMessage.COMMAND_BACKUP_INVALID_BACKUP.reply(event);
					return;
				}
				
				if(GuildBackup.getBackupByName(g, newName) != null) {
					DefaultMessage.COMMAND_BACKUP_RENAME_ALREADY_EXISTS.reply(event);
					return;
				}
				
				GuildBackup.renameBackup(g, name, newName);
				DefaultMessage.COMMAND_BACKUP_RENAME_MESSAGE.reply(event, "name", name, "new_name", newName);
			}
			
			@Override
			public List<OptionData> getOptions() {
				return Arrays.asList(
						new OptionData(OptionType.STRING, "name", "The current backup name", true),
						new OptionData(OptionType.STRING, "new-name", "The new backup name", true)
					);
			}
			
		})
		.setDescription(DefaultLocaleString.COMMAND_BACKUP_RENAME_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_BACKUP_RENAME_USAGE)
		.setPermission(DefaultPermissions.BACKUP_RENAME);
		
		addSubCommand(new CommandBackupTemplate(this));
	}
	
	public static void selectParameters(GraphiteUser user, CommandInvokedEvent event, boolean keyAttached, boolean isTemplate, Consumer<EnumSet<RestoreSelector>> callback) {
		EnumSet<RestoreSelector> params = EnumSet.allOf(RestoreSelector.class);
		if(isTemplate) params.removeAll(CommandBackupTemplate.FORBIDDEN_PARAMETERS);
		
		Supplier<String> message = () -> {
			List<String> lines = new ArrayList<>();
			int idx = 1;
			for(RestoreSelector pr : RestoreSelector.values()) {
				JDAEmote e = params.contains(pr) ? JDAEmote.WHITE_CHECK_MARK : JDAEmote.X;
				boolean chK = pr == RestoreSelector.DISCORD_CHAT_HISTORY && !keyAttached;
				if(chK) e = JDAEmote.X;
				EnumSet<RestoreSelector> missing = pr.getMissingRequirements(params);
				if(chK || !missing.isEmpty()) e = JDAEmote.NO_ENTRY_SIGN;
				
				String line = e.getUnicode() + " " + idx + ". " + pr.getFriendlyName();
				
				if(chK) {
					line += " (No key attached)";
				}else if(!missing.isEmpty()) {
					line += " (requires " + missing.stream().map(p -> p.getFriendlyName()).collect(Collectors.joining(", ")) + ")";
				}
				
				lines.add(line);
				idx++;
			}
			
			return String.format(DefaultLocaleString.COMMAND_BACKUP_DISCLAIMER.getFor(event.getGuild()) + "\n```\nSelect parameters:\n%s\n```", lines.stream().collect(Collectors.joining("\n")));
		};
		
		ButtonInput<NullableOptional<RestoreSelector>> pI = new ButtonInput<NullableOptional<RestoreSelector>>(user, ev -> {
			if(!ev.getItem().isPresent()) {
				ev.markCancelled();
				return;
			}
			
			RestoreSelector s = ev.getItem().get();
			if(s == null) {
				callback.accept(params);
				ev.markDone();
				return;
			}
			
			if(s == RestoreSelector.DISCORD_CHAT_HISTORY && !keyAttached) return;
			if(!params.remove(s)) params.add(s);
			ev.getJDAEvent().editMessage(message.get()).queue();
		})
		.autoRemove(false)
		.removeMessage(false);

		int idx = 0;
		for(RestoreSelector p : RestoreSelector.values()) {
			if(idx > 0 && idx % 5 == 0) pI.newRow();
			pI.addOption(ButtonStyle.PRIMARY, JDAEmote.getKeycapNumber(idx + 1), NullableOptional.of(p));
			idx++;
		}
		pI.newRow();
		pI.addOption(ButtonStyle.SUCCESS, "Confirm", NullableOptional.of(null));
		pI.addOption(ButtonStyle.DANGER, "Cancel", NullableOptional.empty());
		pI.replyEphemeral(event, new MessageCreateBuilder().setContent(message.get()));
	}
	
}

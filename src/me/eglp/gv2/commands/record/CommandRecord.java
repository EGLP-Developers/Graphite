package me.eglp.gv2.commands.record;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import me.eglp.gv2.guild.GraphiteAudioChannel;
import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.guild.GraphiteModule;
import me.eglp.gv2.guild.config.GuildRecordingsConfig;
import me.eglp.gv2.guild.recorder.GuildRecorder;
import me.eglp.gv2.guild.recorder.recording.GuildAudioRecording;
import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.command.Command;
import me.eglp.gv2.util.command.CommandCategory;
import me.eglp.gv2.util.command.CommandInvokedEvent;
import me.eglp.gv2.util.command.ParentCommand;
import me.eglp.gv2.util.command.slash.DeferredReply;
import me.eglp.gv2.util.lang.DefaultLocaleString;
import me.eglp.gv2.util.lang.DefaultMessage;
import me.eglp.gv2.util.lang.GraphiteTimeParser;
import me.eglp.gv2.util.permission.DefaultPermissions;
import me.eglp.gv2.util.queue.GraphiteQueue;
import me.eglp.gv2.util.queue.GraphiteTaskInfo;
import me.eglp.gv2.util.queue.QueueTask;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

public class CommandRecord extends ParentCommand {

	public static final String RECORD_STOP_TASK_ID = "record_stop";
	public static final Pattern RECORDING_NAME_PATTERN = Pattern.compile("(?:\\w| |-){1,64}");

	public CommandRecord() {
		super(GraphiteModule.RECORD, CommandCategory.RECORD, "record");
		addAlias("rec");
		setDescription(DefaultLocaleString.COMMAND_RECORD_DESCRIPTION);

		addSubCommand(new Command(this, "start") {

			@Override
			public void action(CommandInvokedEvent event) {
				if(event.getMember().getCurrentAudioChannel() == null) {
					DefaultMessage.COMMAND_RECORD_NOT_IN_AUDIOCHANNEL.reply(event);
					return;
				}

				GraphiteGuild g = event.getGuild();

				GuildRecorder rec = g.getRecorder();
				if(rec.isRecording()) {
					DefaultMessage.COMMAND_RECORD_ALREADY_RECORDING.reply(event);
					return;
				}

				Member selfMember = event.getGuild().getSelfMember().getMember();
				if(selfMember.getVoiceState() != null
						&& selfMember.getVoiceState().getChannel() != null
						&& selfMember.getVoiceState().isDeafened()) {
					event.getGuild().getJDAGuild().getAudioManager().setSelfDeafened(false);
				}

				GraphiteAudioChannel vc = event.getMember().getCurrentAudioChannel();
				rec.record(vc);

				DefaultMessage.COMMAND_RECORD_RECORDING.reply(event, "voice_channel", vc.getName(), "user", event.getAuthor().getAsMention());
			}

			@Override
			public List<OptionData> getOptions() {
				return Collections.emptyList();
			}
		})
		.setDescription(DefaultLocaleString.COMMAND_RECORD_START_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_RECORD_START_USAGE)
		.setPermission(DefaultPermissions.RECORD_START);

		addSubCommand(new Command(this, "stop") {

			@Override
			public void action(CommandInvokedEvent event) {
				GuildRecorder rec = event.getGuild().getRecorder();
				if(!rec.isRecording()) {
					DefaultMessage.COMMAND_RECORD_NOT_RECORDING.reply(event);
					return;
				}

				GraphiteQueue q = Graphite.getQueue();
				if(q.isHeavyBusy()) {
					DefaultMessage.OTHER_HEAVY_BUSY.reply(event, "patreon", Graphite.getBotInfo().getLinks().getPatreon());
				}

				DeferredReply reply = event.deferReply(DefaultLocaleString.COMMAND_RECORD_SAVING_RECORDING.getFor(event.getSender()));

				QueueTask<GuildAudioRecording> tm = q.queueHeavy(event.getGuild(), new GraphiteTaskInfo(RECORD_STOP_TASK_ID, "Processing recording (record stop)"), () -> rec.stop());
				tm.thenAccept(r -> {
					reply.editOriginal(DefaultMessage.COMMAND_RECORD_STOPPED_RECORDING.createEmbed(event.getSender(), "name", r.getName(), "webinterface", Graphite.getBotInfo().getWebsite().getWebinterfaceURL()));
				}).exceptionally(e -> {
					if(tm.isCancelled()) return null;
					DefaultMessage.ERROR_EXCEPTION.reply(event, "error_message", e.getMessage());
					return null;
				});
			}

			@Override
			public List<OptionData> getOptions() {
				return Collections.emptyList();
			}
		})
		.setDescription(DefaultLocaleString.COMMAND_RECORD_STOP_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_RECORD_STOP_USAGE)
		.setPermission(DefaultPermissions.RECORD_STOP);

		addSubCommand(new Command(this, "download") {

			@Override
			public void action(CommandInvokedEvent event) {
				if(event.isFromGuild() && !event.getTextChannel().canAttachFiles()) {
					DefaultMessage.ERROR_LACKING_PERMISSION.reply(event, "permission", Permission.MESSAGE_ATTACH_FILES.getName());
					return;
				}

				String rName = (String) event.getOption("recording");
				GuildAudioRecording r = event.getGuild().getRecordingsConfig().getRecording(rName);
				if(r == null) {
					DefaultMessage.COMMAND_RECORD_INVALID_RECORDING.reply(event);
					return;
				}

				if(r.getSize() > 8388608) {
					DefaultMessage.COMMAND_RECORD_RECORDING_TOO_LARGE.reply(event, "webinterface", Graphite.getBotInfo().getWebsite().getWebinterfaceURL());
					return;
				}

				event.getChannel().sendFiles(FileUpload.fromData(r.loadAudioData(),  rName + ".mp3"));
			}

			@Override
			public List<OptionData> getOptions() {
				return Arrays.asList(
						new OptionData(OptionType.STRING, "recording", "Name of the recording to download", true)
					);
			}
		})
		.setDescription(DefaultLocaleString.COMMAND_RECORD_DOWNLOAD_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_RECORD_DOWNLOAD_USAGE)
		.setPermission(DefaultPermissions.RECORD_DOWNLOAD);

		addSubCommand(new Command(this, "delete") {

			@Override
			public void action(CommandInvokedEvent event) {
				String rID = (String) event.getOption("recording");
				GuildAudioRecording r = event.getGuild().getRecordingsConfig().getRecording(rID);
				if(r == null) {
					DefaultMessage.COMMAND_RECORD_INVALID_RECORDING.reply(event);
					return;
				}

				r.remove();
				DefaultMessage.COMMAND_RECORD_DELETED_RECORDING.reply(event, "id", rID);
			}

			@Override
			public List<OptionData> getOptions() {
				return Arrays.asList(
						new OptionData(OptionType.STRING, "recording", "Name of the recording to delete", true)
					);
			}
		})
		.setDescription(DefaultLocaleString.COMMAND_RECORD_DELETE_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_RECORD_DELETE_USAGE)
		.setPermission(DefaultPermissions.RECORD_DELETE);

		addSubCommand(new Command(this, "list") {

			@Override
			public void action(CommandInvokedEvent event) {
				List<GuildAudioRecording> recordings = event.getGuild().getRecordingsConfig().getRecordings();

				MessageCreateBuilder mb = new MessageCreateBuilder();

				mb.addContent(DefaultLocaleString.COMMAND_RECORD_LIST_TITLE.getFor(event.getSender(), "prefix", event.getGuild().getPrefix()));
				mb.addContent("\n");

				if(!recordings.isEmpty()) {
					StringBuilder sb = new StringBuilder();
					List<String> s = recordings.stream().map(recording -> "[Channel: " + recording.getChannelName() + "] Name: " + recording.getName() + " (Length: " + GraphiteTimeParser.getTimestamp(recording.getAudioLength()) + ")\n").collect(Collectors.toList());
					s.forEach(recording -> sb.append(recording));
					mb.addContent("```\n" + sb + "\n```");
				}

				event.reply(mb.build());
			}

			@Override
			public List<OptionData> getOptions() {
				return Collections.emptyList();
			}
		})
		.setDescription(DefaultLocaleString.COMMAND_RECORD_LIST_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_RECORD_LIST_USAGE)
		.setPermission(DefaultPermissions.RECORD_LIST);

		addSubCommand(new Command(this, "rename") {

			@Override
			public void action(CommandInvokedEvent event) {
				String recording = (String) event.getOption("recording");
				String newname = (String) event.getOption("new-name");

				if(!RECORDING_NAME_PATTERN.matcher(newname).matches()) {
					DefaultMessage.COMMAND_RECORD_RENAME_INVALID_NAME.reply(event);
					return;
				}

				GraphiteGuild g = event.getGuild();
				GuildRecordingsConfig rc = g.getRecordingsConfig();
				if(rc.getRecording(recording) == null) {
					DefaultMessage.COMMAND_RECORD_INVALID_RECORDING.reply(event);
					return;
				}

				if(rc.getRecording(newname) != null) {
					DefaultMessage.COMMAND_RECORD_RENAME_ALREADY_EXISTS.reply(event);
					return;
				}

				rc.renameRecording(recording, newname);
				DefaultMessage.COMMAND_RECORD_RENAME_MESSAGE.reply(event, "name", recording, "new_name", newname);
			}

			@Override
			public List<OptionData> getOptions() {
				return Arrays.asList(
						new OptionData(OptionType.STRING, "recording", "Name of the recording to rename", true),
						new OptionData(OptionType.STRING, "new-name", "New name for the recording", true)
					);
			}
		})
		.setDescription(DefaultLocaleString.COMMAND_RECORD_RENAME_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_RECORD_RENAME_USAGE)
		.setPermission(DefaultPermissions.RECORD_RENAME);
	}

}

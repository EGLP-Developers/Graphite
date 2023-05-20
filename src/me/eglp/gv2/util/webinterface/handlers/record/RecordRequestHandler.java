package me.eglp.gv2.util.webinterface.handlers.record;

import java.util.Base64;
import java.util.List;

import me.eglp.gv2.commands.record.CommandRecord;
import me.eglp.gv2.guild.GraphiteAudioChannel;
import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.guild.GraphiteMember;
import me.eglp.gv2.guild.config.GuildRecordingsConfig;
import me.eglp.gv2.guild.recorder.GuildRecorder;
import me.eglp.gv2.guild.recorder.recording.GuildAudioRecording;
import me.eglp.gv2.util.permission.DefaultPermissions;
import me.eglp.gv2.util.webinterface.WebinterfaceHandler;
import me.eglp.gv2.util.webinterface.base.WebinterfaceRequestEvent;
import me.eglp.gv2.util.webinterface.base.WebinterfaceResponse;
import me.mrletsplay.mrcore.json.JSONArray;
import me.mrletsplay.mrcore.json.JSONObject;
import net.dv8tion.jda.api.entities.Member;

public class RecordRequestHandler {

	@WebinterfaceHandler(requestMethod = "getRecording", requireGuild = true, requirePermissions = DefaultPermissions.WEBINTERFACE_RECORD)
	public static WebinterfaceResponse getRecording(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		GuildRecordingsConfig c = g.getRecordingsConfig();
		String name = event.getRequestData().getString("recording_name");
		GuildAudioRecording r = c.getRecording(name);
		if(r == null) {
			return WebinterfaceResponse.error("Invalid recording name");
		}
		JSONObject res = new JSONObject();
		res.put("recording", r.toWebinterfaceObject());
		return WebinterfaceResponse.success(res);
	}

	@WebinterfaceHandler(requestMethod = "getRecordingAudio", requireGuild = true, requirePermissions = DefaultPermissions.WEBINTERFACE_RECORD)
	public static WebinterfaceResponse getRecordingAudio(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		GuildRecordingsConfig c = g.getRecordingsConfig();
		String name = event.getRequestData().getString("recording_name");
		GuildAudioRecording r = c.getRecording(name);
		if(r == null) {
			return WebinterfaceResponse.error("Invalid recording name");
		}
		JSONObject res = new JSONObject();
		res.put("audio", Base64.getEncoder().encodeToString(r.loadAudioData()));
		return WebinterfaceResponse.success(res);
	}

	@WebinterfaceHandler(requestMethod = "getRecordings", requireGuild = true, requirePermissions = DefaultPermissions.WEBINTERFACE_RECORD)
	public static WebinterfaceResponse getRecordings(WebinterfaceRequestEvent event) {
		List<GuildAudioRecording> r = event.getSelectedGuild().getRecordingsConfig().getRecordings();
		JSONObject rec = new JSONObject();
		JSONArray recs = new JSONArray();
		r.forEach(rc -> recs.add(rc.toWebinterfaceObject()));
		rec.put("recordings", recs);
		return WebinterfaceResponse.success(rec);
	}

	@WebinterfaceHandler(requestMethod = "startRecording", requireGuild = true, requirePermissions = DefaultPermissions.WEBINTERFACE_RECORD)
	public static WebinterfaceResponse startRecording(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		GraphiteMember m = g.getMember(event.getUser().getDiscordUser());

		if(m.getCurrentAudioChannel() == null) {
			return WebinterfaceResponse.error("You must be in an audio channel");
		}

		GuildRecorder rec = g.getRecorder();
		if(rec.isRecording()) {
			return WebinterfaceResponse.success();
		}

		Member selfMember = g.getSelfMember().getMember();
		if(selfMember.getVoiceState() != null
				&& selfMember.getVoiceState().getChannel() != null
				&& selfMember.getVoiceState().isDeafened()) {
			g.getJDAGuild().getAudioManager().setSelfDeafened(false);
		}

		GraphiteAudioChannel ac = m.getCurrentAudioChannel();
		rec.record(ac);

		return WebinterfaceResponse.success();
	}

	@WebinterfaceHandler(requestMethod = "isRecording", requireGuild = true, requirePermissions = DefaultPermissions.WEBINTERFACE_RECORD)
	public static WebinterfaceResponse isRecording(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		GuildRecorder rec = g.getRecorder();

		JSONObject obj = new JSONObject();
		obj.put("is_recording", rec.isRecording());

		return WebinterfaceResponse.success(obj);
	}

	@WebinterfaceHandler(requestMethod = "stopRecording", requireGuild = true, requirePermissions = DefaultPermissions.WEBINTERFACE_RECORD)
	public static WebinterfaceResponse stopRecording(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		GuildRecorder rec = g.getRecorder();
		if(!rec.isRecording()) {
			return WebinterfaceResponse.success();
		}
		rec.stop();
		return WebinterfaceResponse.success();
	}

	@WebinterfaceHandler(requestMethod = "deleteRecording", requireGuild = true, requirePermissions = DefaultPermissions.WEBINTERFACE_RECORD)
	public static WebinterfaceResponse deleteRecording(WebinterfaceRequestEvent event) {
		String name = event.getRequestData().getString("recording_name");
		GuildAudioRecording r = event.getSelectedGuild().getRecordingsConfig().getRecording(name);
		if(r == null) return WebinterfaceResponse.success();
		r.remove();
		return WebinterfaceResponse.success();
	}

	@WebinterfaceHandler(requestMethod = "deleteAllRecordings", requireGuild = true, requirePermissions = DefaultPermissions.WEBINTERFACE_RECORD)
	public static WebinterfaceResponse deleteAllRecordings(WebinterfaceRequestEvent event) {
		List<GuildAudioRecording> r = event.getSelectedGuild().getRecordingsConfig().getRecordings();
		r.forEach(rec -> rec.remove());
		return WebinterfaceResponse.success();
	}

	@WebinterfaceHandler(requestMethod = "renameRecording", requireGuild = true, requirePermissions = DefaultPermissions.WEBINTERFACE_RECORD)
	public static WebinterfaceResponse renameRecording(WebinterfaceRequestEvent event) {
		String name = event.getRequestData().getString("name");
		String newName = event.getRequestData().getString("new_name");

		GraphiteGuild g = event.getSelectedGuild();
		GuildAudioRecording r = g.getRecordingsConfig().getRecording(name);

		if(r == null) {
			return WebinterfaceResponse.error("Recording doesn't exist");
		}

		GuildAudioRecording nR = g.getRecordingsConfig().getRecording(newName);
		if(nR != null) {
			return WebinterfaceResponse.error("A recording with this name already exists");
		}

		if(!CommandRecord.RECORDING_NAME_PATTERN.matcher(newName).matches()) {
			return WebinterfaceResponse.error("New recording name doesn't match our requirements");
		}

		GuildRecordingsConfig rc = g.getRecordingsConfig();
		rc.renameRecording(name, newName);

		return WebinterfaceResponse.success();
	}
}

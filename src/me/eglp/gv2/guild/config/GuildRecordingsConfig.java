package me.eglp.gv2.guild.config;

import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.sql.rowset.serial.SerialBlob;

import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.guild.recorder.recording.GuildAudioRecording;
import me.eglp.gv2.guild.recorder.recording.RecordingEvent;
import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.GraphiteUtil;
import me.eglp.gv2.util.mysql.SQLTable;
import me.mrletsplay.mrcore.json.JSONArray;
import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.mrcore.json.converter.JSONConverter;
import me.mrletsplay.mrcore.json.converter.SerializationOption;
import me.mrletsplay.mrcore.misc.FriendlyException;

@SQLTable(
	name = "guilds_recordings",
	columns = {
		"GuildId varchar(255) NOT NULL",
		"RecordingName varchar(255) NOT NULL",
		"Timestamp bigint NOT NULL",
		"AudioData longblob NOT NULL",
		"AudioLength bigint NOT NULL",
		"ChannelName text NOT NULL",
		"Events longtext NOT NULL"
	},
	guildReference = "GuildId"
)
public class GuildRecordingsConfig {

	private GraphiteGuild guild;

	public GuildRecordingsConfig(GraphiteGuild guild) {
		this.guild = guild;
	}

	public GraphiteGuild getGuild() {
		return guild;
	}

	public GuildAudioRecording createRecording(byte[] audioData, long audioLength, String channelName, List<RecordingEvent> events) {
		return Graphite.getMySQL().run(con -> {
			String id = GraphiteUtil.randomShortID();
			long timestamp = System.currentTimeMillis();
			try(PreparedStatement s = con.prepareStatement("INSERT INTO guilds_recordings(GuildId, RecordingName, Timestamp, AudioData, AudioLength, ChannelName, Events) VALUES(?, ?, ?, ?, ?, ?, ?)")) {
				s.setString(1, guild.getID());
				s.setString(2, id);
				s.setLong(3, timestamp);
				s.setBlob(4, new SerialBlob(audioData));
				s.setLong(5, audioLength);
				s.setString(6, channelName);
				s.setString(7, new JSONArray(events.stream().map(e -> e.toJSON(SerializationOption.DONT_INCLUDE_CLASS)).collect(Collectors.toList())).toString());
				s.execute();
				return new GuildAudioRecording(guild, id, timestamp, audioLength, channelName, events);
			}
		}).orElseThrowOther(e -> new FriendlyException("Failed to save recording to MySQL", e));
	}

	public List<GuildAudioRecording> getRecordings() {
		return Graphite.getMySQL().run(con -> {
			try(PreparedStatement s = con.prepareStatement("SELECT * FROM guilds_recordings WHERE GuildId = ?")) {
				s.setString(1, guild.getID());
				try(ResultSet r = s.executeQuery()) {
					List<GuildAudioRecording> recordings = new ArrayList<>();
					while(r.next()) {
						recordings.add(loadRecording(r));
					}
					return recordings;
				}
			}
		}).orElseThrowOther(e -> new FriendlyException("Failed to load recordings from MySQL", e));
	}

	public GuildAudioRecording getRecording(String name) {
		return Graphite.getMySQL().run(con -> {
			try(PreparedStatement s = con.prepareStatement("SELECT * FROM guilds_recordings WHERE GuildId = ? AND RecordingName = ?")) {
				s.setString(1, guild.getID());
				s.setString(2, name);
				try(ResultSet r = s.executeQuery()) {
					if(!r.next()) return null;
					return loadRecording(r);
				}
			}
		}).orElseThrowOther(e -> new FriendlyException("Failed to load recording from MySQL", e));
	}

	private GuildAudioRecording loadRecording(ResultSet r) throws SQLException {
		return new GuildAudioRecording(guild, r.getString("RecordingName"), r.getLong("Timestamp"), r.getLong("AudioLength"), r.getString("ChannelName"), new JSONArray(r.getString("Events")).stream()
				.map(o -> JSONConverter.decodeObject((JSONObject) o, RecordingEvent.class))
				.collect(Collectors.toList()));
	}

	public void deleteRecording(String name) {
		Graphite.getMySQL().run(con -> {
			try(PreparedStatement s = con.prepareStatement("DELETE FROM guilds_recordings WHERE GuildId = ? AND RecordingName = ?")) {
				s.setString(1, guild.getID());
				s.setString(2, name);
				s.execute();
			}
		});
	}

	public long getRecordingSize(String name) {
		return Graphite.getMySQL().run(con -> {
			try(PreparedStatement s = con.prepareStatement("SELECT LENGTH(AudioData) AS Size FROM guilds_recordings WHERE GuildId = ? AND RecordingName = ?")) {
				s.setString(1, guild.getID());
				s.setString(2, name);
				try(ResultSet r = s.executeQuery()) {
					if(!r.next()) return null;
					return r.getLong("Size");
				}
			}
		}).orElseThrowOther(e -> new FriendlyException("Failed to load recording length from MySQL", e));
	}

	public byte[] getRecordingAudioData(String name) {
		return Graphite.getMySQL().run(con -> {
			try(PreparedStatement s = con.prepareStatement("SELECT AudioData FROM guilds_recordings WHERE GuildId = ? AND RecordingName = ?")) {
				s.setString(1, guild.getID());
				s.setString(2, name);
				try(ResultSet r = s.executeQuery()) {
					if(!r.next()) return null;
					Blob b = r.getBlob("AudioData");
					byte[] bytes = b.getBytes(1, (int) b.length());
					return bytes;
				}
			}
		}).orElseThrowOther(e -> new FriendlyException("Failed to load recording audio data from MySQL", e));
	}

	public void renameRecording(String name, String newName) {
		Graphite.getMySQL().query("UPDATE guilds_recordings SET RecordingName = ? WHERE GuildId = ? AND RecordingName = ?", newName, guild.getID(), name);
	}

}

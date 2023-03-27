package me.eglp.gv2.util.music;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioTrack;
import com.sedmelluq.discord.lavaplayer.source.getyarn.GetyarnAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.getyarn.GetyarnAudioTrack;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioTrack;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioTrack;
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioTrack;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.GraphiteUtil;
import me.eglp.gv2.util.base.GraphiteMusical;
import me.eglp.gv2.util.mysql.SQLTable;
import me.mrletsplay.mrcore.misc.FriendlyException;

@SQLTable(
	name = "global_playlists",
	columns = {
		"OwnerId varchar(255) NOT NULL",
		"PlaylistName varchar(255) NOT NULL",
		"TrackIndex int NOT NULL",
		"TrackType varchar(255) NOT NULL",
		"TrackIdentifier text NOT NULL",
		"TrackTitle text",
		"TrackAuthor text",
		"TrackLength bigint",
		"TrackIsStream bool NOT NULL",
		"TrackUri text",
		"PRIMARY KEY (OwnerId, PlaylistName, TrackIndex)"
	},
	guildReference = "OwnerId"
)
public class GraphiteMusic {

	private GraphiteMusical owner;
	
	public GraphiteMusic(GraphiteMusical owner) {
		this.owner = owner;
	}
	
	public GraphitePlaylist createPlaylist(List<GraphiteTrack> tracks, String playlistName) {
		if(playlistName == null) playlistName = GraphiteUtil.randomShortID();
		final String fName = playlistName;
		Graphite.getMySQL().run(con -> {
			try(PreparedStatement s = con.prepareStatement("INSERT INTO global_playlists(OwnerId, PlaylistName, TrackIndex, TrackType, TrackIdentifier, TrackTitle, TrackAuthor, TrackLength, TrackIsStream, TrackUri) VALUES(?,?,?,?,?,?,?,?,?,?)")) {
				int idx = 0;
				for(GraphiteTrack t : tracks) {
					s.setString(1, owner.getID());
					s.setString(2, fName);
					s.setInt(3, idx);
					
					AudioTrackInfo inf = t.getLavaTrack().getInfo();
					GraphiteTrackType type = t.getType();
					if(type == null) continue;
					s.setString(4, type.name());
					s.setString(5, inf.identifier);
					s.setString(6, inf.title);
					s.setString(7, inf.author);
					s.setLong(8, inf.length);
					s.setBoolean(9, inf.isStream);
					s.setString(10, inf.uri);
					s.addBatch();
					
					idx++;
				}
				
				s.executeBatch();
			}
		});
		
		return new GraphitePlaylist(owner, playlistName, new ArrayList<>(tracks));
	}
	
	public void deletePlaylist(GraphitePlaylist playlist) {
		deletePlaylist(playlist.getName());
	}
	
	public void deletePlaylist(String playlistName) {
		Graphite.getMySQL().query("DELETE FROM global_playlists WHERE OwnerId = ? AND PlaylistName = ?", owner.getID(), playlistName);
	}
	
	public void renamePlaylist(String name, String newName) {
		Graphite.getMySQL().query("UPDATE global_playlists SET PlaylistName = ? WHERE OwnerId = ? AND PlaylistName = ?", newName, owner.getID(), name);
	}
	
	public List<GraphitePlaylist> getPlaylists() {
		return Graphite.getMySQL().queryArray(String.class, "SELECT DISTINCT PlaylistName FROM global_playlists WHERE OwnerId = ?", owner.getID())
				.orElseThrowOther(e -> new FriendlyException("Failed to load playlists from MySQL", e)).stream()
				.map(name -> getPlaylistByName(name))
				.collect(Collectors.toList());
	}
	
	public GraphitePlaylist getPlaylistByName(String playlistName) {
		return Graphite.getMySQL().run(con -> {
			try(PreparedStatement s = con.prepareStatement("SELECT * FROM global_playlists WHERE OwnerId = ? AND PlaylistName = ? ORDER BY TrackIndex")) {
				s.setString(1, owner.getID());
				s.setString(2, playlistName);
				try(ResultSet r = s.executeQuery()) {
					List<GraphiteTrack> tracks = new ArrayList<>();
					while(r.next()) {
						tracks.add(loadTrack(r));
					}
					
					if(tracks.isEmpty()) return null;
					return new GraphitePlaylist(owner, playlistName, tracks);
				}
			}
		}).orElseThrowOther(e -> new FriendlyException("Failed to load playlist from MySQL", e));
	}
	
	private GraphiteTrack loadTrack(ResultSet r) throws SQLException {
		GraphiteTrackType type = GraphiteTrackType.valueOf(r.getString("TrackType"));
		AudioTrackInfo inf = new AudioTrackInfo(r.getString("TrackTitle"), r.getString("TrackAuthor"), r.getLong("TrackLength"), r.getString("TrackIdentifier"), r.getBoolean("TrackIsStream"), r.getString("TrackUri"));
		AudioTrack track = null;
		switch(type) {
			case BANDCAMP:
				track = new BandcampAudioTrack(inf, new BandcampAudioSourceManager());
				break;
			case SOUNDCLOUD:
				track = new SoundCloudAudioTrack(inf, SoundCloudAudioSourceManager.createDefault());
				break;
			case TWITCH:
				track = new TwitchStreamAudioTrack(inf, new TwitchStreamAudioSourceManager());
				break;
			case VIMEO:
				track = new VimeoAudioTrack(inf, new VimeoAudioSourceManager());
				break;
			case YOUTUBE:
				track = new YoutubeAudioTrack(inf, new YoutubeAudioSourceManager());
				break;
			case GETYARN:
				track = new GetyarnAudioTrack(inf, new GetyarnAudioSourceManager());
				break;
		}
		return new GraphiteTrack(track);
	}
	
}

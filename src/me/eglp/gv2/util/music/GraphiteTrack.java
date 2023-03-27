package me.eglp.gv2.util.music;

import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioTrack;
import com.sedmelluq.discord.lavaplayer.source.getyarn.GetyarnAudioTrack;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioTrack;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioTrack;
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioTrack;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

public class GraphiteTrack {

	private AudioTrack lavaTrack;
	private int startAt;
	
	public GraphiteTrack(AudioTrack lavaTrack) {
		this.lavaTrack = lavaTrack;
	}
	
	public AudioTrack getLavaTrack() {
		return lavaTrack;
	}
	
	public void setStartAt(int startAt) {
		this.startAt = startAt;
	}
	
	public int getStartAt() {
		return startAt;
	}
	
	public GraphiteTrackType getType() {
		if(lavaTrack instanceof YoutubeAudioTrack) {
			return GraphiteTrackType.YOUTUBE;
		}else if(lavaTrack instanceof VimeoAudioTrack) {
			return GraphiteTrackType.VIMEO;
		}else if(lavaTrack instanceof TwitchStreamAudioTrack) {
			return GraphiteTrackType.TWITCH;
		}else if(lavaTrack instanceof SoundCloudAudioTrack) {
			return GraphiteTrackType.SOUNDCLOUD;
		}else if(lavaTrack instanceof BandcampAudioTrack) {
			return GraphiteTrackType.BANDCAMP;
		}else if(lavaTrack instanceof GetyarnAudioTrack) {
			return GraphiteTrackType.GETYARN;
		}else {
			return null;
		}
	}
	
	public boolean canBeSaved() {
		return getType() != null;
	}
	
	@Override
	public String toString() {
		return "{TRACK: " + lavaTrack.getIdentifier() + " VIA " + getType() + "}";
	}
	
}

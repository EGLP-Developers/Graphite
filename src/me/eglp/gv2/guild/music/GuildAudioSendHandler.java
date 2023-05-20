package me.eglp.gv2.guild.music;

import java.nio.ByteBuffer;

import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;

import me.eglp.gv2.guild.GraphiteGuild;
import net.dv8tion.jda.api.audio.AudioSendHandler;

public class GuildAudioSendHandler implements AudioSendHandler {

	private GraphiteGuild guild;
	private AudioFrame lastFrame;

	public GuildAudioSendHandler(GraphiteGuild guild) {
		this.guild = guild;
	}

	@Override
	public boolean canProvide() {
		if (lastFrame == null) {
			lastFrame = guild.getMusic().getTrackManager().getPlayer().provide();
		}
		return lastFrame != null;
	}

	@Override
	public ByteBuffer provide20MsAudio() {
		if (lastFrame == null)
			lastFrame = guild.getMusic().getTrackManager().getPlayer().provide();
		byte[] data = lastFrame != null ? lastFrame.getData() : null;
		lastFrame = null;
		return ByteBuffer.wrap(data);
	}

	@Override
	public boolean isOpus() {
		return true;
	}
}

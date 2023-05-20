package me.eglp.gv2.guild.recorder;

import me.eglp.gv2.guild.GraphiteAudioChannel;
import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.guild.recorder.recording.GuildAudioRecording;
import me.eglp.gv2.guild.recorder.recording.InProgressRecording;

public class GuildRecorder {

	private GraphiteGuild guild;
	private GuildAudioReceiveHandler audioReceiveHandler;

	public GuildRecorder(GraphiteGuild guild) {
		this.guild = guild;
	}

	private GuildAudioReceiveHandler ensureAudioReceiveHandler() {
		if(audioReceiveHandler == null) {
			audioReceiveHandler = new GuildAudioReceiveHandler(guild);
		}

		guild.getJDAGuild().getAudioManager().setReceivingHandler(audioReceiveHandler);
		return audioReceiveHandler;
	}

	public void record(GraphiteAudioChannel channel) {
		guild.getJDAGuild().getAudioManager().openAudioConnection(channel.getJDAChannel());
		InProgressRecording rec = new InProgressRecording(channel);
		ensureAudioReceiveHandler().setCurrentRecording(rec);
	}

	public boolean isRecording() {
		return guild.getJDAGuild().getAudioManager().getReceivingHandler() != null;
	}

	public GuildAudioRecording stop() {
		if(!guild.getMusic().isPlaying()) guild.getJDAGuild().getAudioManager().closeAudioConnection();
		if(!isRecording()) return null;
		GuildAudioRecording rec = ensureAudioReceiveHandler().stopRecording();
		guild.getJDAGuild().getAudioManager().setReceivingHandler(null);
		return rec;
	}

}

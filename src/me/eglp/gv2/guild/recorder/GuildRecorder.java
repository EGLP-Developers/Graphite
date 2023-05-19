package me.eglp.gv2.guild.recorder;

import java.util.HashMap;
import java.util.Map;

import me.eglp.gv2.guild.GraphiteAudioChannel;
import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.guild.recorder.recording.GuildAudioRecording;
import me.eglp.gv2.guild.recorder.recording.InProgressRecording;
import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.multiplex.GraphiteMultiplex;
import me.eglp.gv2.multiplex.bot.MultiplexBot;

public class GuildRecorder {

	private GraphiteGuild guild;
	private Map<MultiplexBot, GuildAudioReceiveHandler> audioReceiveHandlers;

	public GuildRecorder(GraphiteGuild guild) {
		this.guild = guild;
		this.audioReceiveHandlers = new HashMap<>();
	}

	private GuildAudioReceiveHandler ensureAudioReceiveHandler(MultiplexBot bot) {
		GuildAudioReceiveHandler m = audioReceiveHandlers.get(bot);
		if(m == null) {
			m = new GuildAudioReceiveHandler(guild, bot);
			audioReceiveHandlers.put(bot, m);
		}
		final var b = m;
		Graphite.withBot(bot, () -> guild.getJDAGuild().getAudioManager().setReceivingHandler(b));
		return m;
	}

	private GuildAudioReceiveHandler ensureAudioReceiveHandler() {
		return ensureAudioReceiveHandler(GraphiteMultiplex.getCurrentBot());
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

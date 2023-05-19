package me.eglp.gv2.guild.recorder;

import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.guild.recorder.recording.GuildAudioRecording;
import me.eglp.gv2.guild.recorder.recording.InProgressRecording;
import me.eglp.gv2.main.DebugCategory;
import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.main.GraphiteDebug;
import me.eglp.gv2.multiplex.GraphiteFeature;
import me.eglp.gv2.multiplex.bot.MultiplexBot;
import net.dv8tion.jda.api.audio.AudioNatives;
import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.audio.CombinedAudio;

public class GuildAudioReceiveHandler implements AudioReceiveHandler {

	private GraphiteGuild guild;
	private MultiplexBot bot;
	private InProgressRecording currentRecording;

	public GuildAudioReceiveHandler(GraphiteGuild guild, MultiplexBot bot) {
		this.guild = guild;
		this.bot = bot;
		AudioNatives.ensureOpus(); // Load libs
	}

	public void setCurrentRecording(InProgressRecording currentRecording) {
		this.currentRecording = currentRecording;
		guild.getJDAGuild().getSelfMember().modifyNickname("Recording!").queue();
	}

	public MultiplexBot getBot() {
		return bot;
	}

	@Override
	public boolean canReceiveCombined() {
		return true;
	}

	public GuildAudioRecording stopRecording() {
		if(currentRecording == null) return null;
		InProgressRecording iR = currentRecording;
		currentRecording = null;

		GuildAudioRecording r = iR.stop();
		guild.getJDAGuild().getSelfMember().modifyNickname(null).queue();

		Graphite.getWebinterface().sendRequestToGuildUsers("updateRecordings", null, guild.getID(), GraphiteFeature.MUSIC);
		return r;
	}

	@Override
	public void handleCombinedAudio(CombinedAudio combinedAudio) {
		if(currentRecording == null) return;
		try {
			currentRecording.writeAudioFrame(combinedAudio);
		} catch (Exception e) {
			GraphiteDebug.log(DebugCategory.RECORD, e);
		}
	}

}

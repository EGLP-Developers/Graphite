package me.eglp.gv2.util.base.guild.recorder.recording;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import me.eglp.gv2.main.DebugCategory;
import me.eglp.gv2.main.GraphiteDebug;
import me.eglp.gv2.multiplex.ContextHandle;
import me.eglp.gv2.multiplex.GraphiteMultiplex;
import me.eglp.gv2.util.base.guild.GraphiteAudioChannel;
import me.mrletsplay.mrcore.misc.FriendlyException;
import net.dv8tion.jda.api.audio.CombinedAudio;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

public class InProgressRecording {
	
	private GraphiteAudioChannel channel;
	private Set<Member> lastMembers;
	private Set<User> lastTalkingMembers;
	private List<RecordingEvent> events;
	private Process lameProcess;
	private ByteArrayOutputStream mp3Out;
	private Thread transferThread;
	private int frame;
	private boolean isDone;
	private ContextHandle handle;
	private long startedAt;
	
	public InProgressRecording(GraphiteAudioChannel channel) {
		this.channel = channel;
		this.events = new ArrayList<>();
		this.handle = GraphiteMultiplex.handle();
		try {
			this.lameProcess = new ProcessBuilder("lame", // Lame: convert from 48KHz 16bit stereo signed BigEndian PCM to MP3
					"-r",
					"-s", "48",
					"--signed",
					"--bitwidth", "16",
					"--big-endian",
					"-m", "s",
					"-", "-").start();
			this.mp3Out = new ByteArrayOutputStream();
			this.transferThread = new Thread(() -> {
				try {
					lameProcess.getInputStream().transferTo(mp3Out);
				} catch (IOException e) {
					// TODO: msg?
					handle.reset();
					channel.getGuild().getRecorder().stop();
					GraphiteDebug.log(DebugCategory.RECORD, e);
				}
			});
			transferThread.start();
		}catch(IOException e) {
			GraphiteDebug.log(DebugCategory.RECORD, e);
		}
	}
	
	public void writeAudioFrame(CombinedAudio audio) throws IOException {
		if(isDone) return;
		if(startedAt == 0) startedAt = System.currentTimeMillis();
		if(frame > 50 * 60 * 60 * 5) {
			isDone = true;
			stop();
			return;
		}
		
		handle.reset();
		lameProcess.getOutputStream().write(audio.getAudioData(1));
		lameProcess.getOutputStream().flush();
		
		Set<Member> members = new HashSet<>(channel.getJDAChannel().getMembers());
		Set<User> talkingMembers = new HashSet<>(audio.getUsers());
		if(lastMembers == null || !members.equals(lastMembers)) {
			lastMembers = members;
			events.add(new RecordingEvent(frame, RecordingEventType.UPDATE_USERS, members.stream().map(Member::getId).collect(Collectors.toList())));
		}
		
		if(lastTalkingMembers == null || !talkingMembers.equals(lastTalkingMembers)) {
			lastTalkingMembers = talkingMembers;
			events.add(new RecordingEvent(frame, RecordingEventType.UPDATE_TALKING_USERS, talkingMembers.stream().map(User::getId).collect(Collectors.toList())));
		}
		
		frame++;
	}
	
	public GuildAudioRecording stop() {
		try {
			isDone = true;
			
			long length = System.currentTimeMillis() - startedAt;
			
			try {
				lameProcess.getOutputStream().close();
				lameProcess.waitFor(60, TimeUnit.SECONDS);
				if(lameProcess.isAlive()) {
					lameProcess.destroy();
					// Lame failed to exit on time
					// TODO: handle more gracefully?
					throw new FriendlyException("lame didn't exit on time");
				}
			} catch (InterruptedException e) {
				// TODO: msg?
				handle.reset();
				channel.getGuild().getRecorder().stop();
				GraphiteDebug.log(DebugCategory.RECORD, e);
			}
			
			return channel.getGuild().getRecordingsConfig().createRecording(mp3Out.toByteArray(), length, channel.getName(), events);
		} catch (IOException e) {
			GraphiteDebug.log(DebugCategory.RECORD, e);
			throw new FriendlyException(e);
		}
	}
	
	public boolean isDone() {
		return isDone;
	}
	
}

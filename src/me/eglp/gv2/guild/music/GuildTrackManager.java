package me.eglp.gv2.guild.music;

import java.util.ArrayList;
import java.util.List;

import com.github.natanbc.lavadsp.timescale.TimescalePcmAudioFilter;
import com.sedmelluq.discord.lavaplayer.filter.AudioFilter;
import com.sedmelluq.discord.lavaplayer.filter.PcmFilterFactory;
import com.sedmelluq.discord.lavaplayer.filter.UniversalPcmAudioFilter;
import com.sedmelluq.discord.lavaplayer.filter.equalizer.Equalizer;
import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.InternalAudioTrack;

import me.eglp.gv2.guild.GraphiteAudioChannel;
import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.main.DebugCategory;
import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.main.GraphiteDebug;
import me.eglp.gv2.multiplex.GraphiteFeature;
import me.eglp.gv2.multiplex.bot.MultiplexBot;
import me.eglp.gv2.util.music.GraphiteTrack;
import me.eglp.gv2.util.webinterface.js.JavaScriptGetter;
import me.eglp.gv2.util.webinterface.js.WebinterfaceObject;
import net.dv8tion.jda.api.managers.AudioManager;

public class GuildTrackManager extends AudioEventAdapter implements WebinterfaceObject{

	static {
		//TimescaleNativeLibLoader.loadTimescaleLibrary();
	}

	public static final float[] BASS_BOOST_LEVELS = {
		0.05f,
		0.075f,
		0.1f
	};

	public static final double
		MIN_SPEED = 0.25d,
		MAX_SPEED = 5d,
		MIN_PITCH = 0.25d,
		MAX_PITCH = 5d;

	private MultiplexBot bot;
	private GraphiteGuild guild;
	private AudioPlayer player;
	private AudioManager audioManager;
	private GraphiteAudioChannel channel;
	private MusicQueue queue;
	private GraphiteTrack playingTrack;
	private boolean
		looping,
		endless;
	private int bassBoostLevel;
	private double speed;
	private double pitch;

	public GuildTrackManager(MultiplexBot bot, GraphiteGuild guild) {
		this.bot = bot;
		this.guild = guild;
		this.speed = 1.0d;
		this.pitch = 1.0d;
		this.player = Graphite.getAudioPlayerManager().createPlayer();
		player.addListener(this);
		queue = new MusicQueue();
	}

	public void setChannel(GraphiteAudioChannel channel) {
		if(channel == null) throw new NullPointerException("Channel can't be null. Use stop() instead");
		this.channel = channel;
	}

	public GraphiteAudioChannel getChannel() {
		return channel;
	}

	public AudioPlayer getPlayer() {
		return player;
	}

	public void queueTrack(GraphiteTrack track) {
		queue.add(track);

		Graphite.getWebinterface().sendRequestToGuildUsers("updateQueue", null, guild.getID(), GraphiteFeature.MUSIC);
	}

	public void skip(int amount) {
		GraphiteTrack track = next(amount);
		if(track == null) return;
		stopCurrentTrack();
		playTrack(track);
	}

	public void jump(int absoluteIndex) {
		GraphiteTrack track = queue.jump(absoluteIndex);
		stopCurrentTrack();
		playTrack(track);

		Graphite.getWebinterface().sendRequestToGuildUsers("updatePlayingTrack", null, guild.getID(), GraphiteFeature.MUSIC);
	}

	public GraphiteTrack removeRelative(int idx) {
		if(idx == 0) next(1);
		return queue.removeRelative(idx);
	}

	public GraphiteTrack removeAbsolute(int idx) {
		if(idx == queue.getCurrentIndex()) next(1);
		return queue.removeAbsolute(idx);
	}

	public void play() {
		if(channel == null) throw new IllegalStateException("Channel is null");
		if(queue.hasReachedEnd()) return;
		if(playingTrack != null) return;

		Graphite.withBot(bot, () -> {
			audioManager = guild.getJDAGuild().getAudioManager();
			if(!audioManager.isConnected()) audioManager.setSelfDeafened(true);
			audioManager.openAudioConnection(channel.getJDAChannel());
			playTrack(queue.next());
		});

		Graphite.getWebinterface().sendRequestToGuildUsers("updatePlayingTrack", null, guild.getID(), GraphiteFeature.MUSIC);
	}

	public void stopCurrentTrack() {
		if(player.getPlayingTrack() != null) player.stopTrack();
		playingTrack = null;
	}

	public void stop() {
		stopCurrentTrack();

		Graphite.withBot(bot, () -> {
			if(audioManager != null && !guild.getRecorder().isRecording()) audioManager.closeAudioConnection();
		});

		audioManager = null;
		channel = null;
		looping = false;
		endless = false;
		bassBoostLevel = 0;
		speed = 1.0d;
		pitch = 1.0d;
		player.setFilterFactory(null);
		if(player.getVolume() > 100) player.setVolume(100);
		queue.clear();

		Graphite.getWebinterface().sendRequestToGuildUsers("stopMusic", null, guild.getID(), GraphiteFeature.MUSIC);
	}

	public GraphiteTrack getPlayingTrack() {
		return playingTrack;
	}

	public void setLooping(boolean looping) {
		this.looping = looping;

		Graphite.getWebinterface().sendRequestToGuildUsers("updatePlayerState", null, guild.getID(), GraphiteFeature.MUSIC);
	}

	@JavaScriptGetter(name = "isLooping", returning = "looping")
	public boolean isLooping() {
		return looping;
	}

	public void setEndless(boolean endless) {
		this.endless = endless;

		Graphite.getWebinterface().sendRequestToGuildUsers("updatePlayerState", null, guild.getID(), GraphiteFeature.MUSIC);
	}

	@JavaScriptGetter(name = "isEndless", returning = "endless")
	public boolean isEndless() {
		return endless;
	}

	public void shuffleQueue() {
		queue.shuffle();
	}

	public MusicQueue getQueue() {
		return queue;
	}

	public void setBassBoostLevel(int bassBoostLevel) {
		if(bassBoostLevel < 0 || bassBoostLevel > BASS_BOOST_LEVELS.length) throw new IllegalArgumentException("Level is too high/low");
		this.bassBoostLevel = bassBoostLevel;
		updateAudioFilters();
		player.setFrameBufferDuration(null);

		Graphite.getWebinterface().sendRequestToGuildUsers("updatePlayerState", null, guild.getID(), GraphiteFeature.MUSIC);
	}

	@JavaScriptGetter(name = "getBassBoostLevel", returning = "bassBoostLevel")
	public int getBassBoostLevel() {
		return bassBoostLevel;
	}

	public void setSpeed(double speed) {
		if(speed < MIN_SPEED || speed > MAX_SPEED) throw new IllegalArgumentException("Speed too high/low");
		this.speed = speed;
		updateAudioFilters();

		Graphite.getWebinterface().sendRequestToGuildUsers("updatePlayerState", null, guild.getID(), GraphiteFeature.MUSIC);
	}

	public double getSpeed() {
		return speed;
	}

	public void setPitch(double pitch) {
		if(pitch < MIN_PITCH || pitch > MAX_PITCH) throw new IllegalArgumentException("Pitch too high/low");
		this.pitch = pitch;
		updateAudioFilters();

		Graphite.getWebinterface().sendRequestToGuildUsers("updatePlayerState", null, guild.getID(), GraphiteFeature.MUSIC);
	}

	public void setSpeedAndPitch(double speed, double pitch) {
		if(speed < MIN_SPEED || speed > MAX_SPEED) throw new IllegalArgumentException("Speed too high/low");
		if(pitch < MIN_PITCH || pitch > MAX_PITCH) throw new IllegalArgumentException("Pitch too high/low");
		this.speed = speed;
		this.pitch = pitch;
		updateAudioFilters();

		Graphite.getWebinterface().sendRequestToGuildUsers("updatePlayerState", null, guild.getID(), GraphiteFeature.MUSIC);
	}

	public void reset() {
		this.speed = 1.0d;
		this.pitch = 1.0d;
		this.bassBoostLevel = 0;
		player.setVolume(100);
		updateAudioFilters();

		Graphite.getWebinterface().sendRequestToGuildUsers("updatePlayerState", null, guild.getID(), GraphiteFeature.MUSIC);
	}

	public double getPitch() {
		return pitch;
	}

	private void updateAudioFilters() {
		if(bassBoostLevel == 0 && speed == 1d && pitch == 1d) {
			player.setFilterFactory(null);
		}else {
			player.setFilterFactory(new PcmFilterFactory() {

				@Override
				public List<AudioFilter> buildChain(AudioTrack track, AudioDataFormat format, UniversalPcmAudioFilter output) {
					Equalizer eq = new Equalizer(format.channelCount, output);

					if(bassBoostLevel != 0) {
						for(int i = Equalizer.BAND_COUNT; i > 0; i--) {
							if(i > Equalizer.BAND_COUNT / 4) {
								eq.setGain(i, (float) -BASS_BOOST_LEVELS[bassBoostLevel - 1]);
							}else {
								eq.setGain(i, (float) BASS_BOOST_LEVELS[bassBoostLevel - 1]);
							}
						}
					}

					List<AudioFilter> filters = new ArrayList<>();
					TimescalePcmAudioFilter speedAndPitch = new TimescalePcmAudioFilter(eq, format.channelCount, format.sampleRate);
					speedAndPitch.setSpeed(speed);
					speedAndPitch.setPitch(pitch);
					filters.add(speedAndPitch);
					filters.add(eq);
					return filters;
				}

			});
		}

		// TODO: maybe improve? (or just reduce buffer duration)
		((InternalAudioTrack) player.getPlayingTrack()).getActiveExecutor().getAudioBuffer().clear();
		player.getPlayingTrack().setPosition(player.getPlayingTrack().getPosition());
	}

	private void playTrack(GraphiteTrack track) {
		if(channel == null) throw new IllegalStateException("Channel is null");
		if(playingTrack != null) player.stopTrack();

		this.playingTrack = track;
		player.playTrack(playingTrack.getLavaTrack().makeClone());
		player.getPlayingTrack().setPosition(track.getStartAt() * 1000);
	}

	@Override
	public void onTrackStart(AudioPlayer player, AudioTrack track) {
		// Empty for now
	}

	@Override
	public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
		Graphite.getScheduler().execute(() -> {
			if(endReason == AudioTrackEndReason.STOPPED) return;

			if(endReason == AudioTrackEndReason.LOAD_FAILED && track instanceof YoutubeAudioTrack) {
				try {
					AudioReference ref = new AudioReference("https://invidious.fdn.fr/latest_version?id=" + track.getIdentifier() + "&itag=18", "Cringeman returns");
					HttpAudioSourceManager man = new HttpAudioSourceManager();
					AudioItem it = man.loadItem(Graphite.getAudioPlayerManager(), ref);
					while(it instanceof AudioReference) {
						it = man.loadItem(Graphite.getAudioPlayerManager(), (AudioReference) it);
					}

					if(it != null) {
						player.playTrack((AudioTrack) it);
						return;
					}
				}catch(Exception e) {
					e.printStackTrace();
				}
			}

			if(looping) {
				playTrack(playingTrack);
				return;
			}

			GraphiteTrack next = next(1);
			if(next == null) {
				Graphite.getWebinterface().sendRequestToGuildUsers("stopMusic", null, guild.getID(), GraphiteFeature.MUSIC);
				return;
			}

			playTrack(next);

			Graphite.getWebinterface().sendRequestToGuildUsers("updatePlayingTrack", null, guild.getID(), GraphiteFeature.MUSIC);
		});
	}

	private GraphiteTrack next(int amount) {
		GraphiteTrack next = queue.next(amount);
		if(next == null) {
			if(endless) {
				queue.playFromTop();
				next = queue.next();
			}else {
				stop();
				return null;
			}
		}

		Graphite.getWebinterface().sendRequestToGuildUsers("updatePlayingTrack", null, guild.getID(), GraphiteFeature.MUSIC);
		return next;
	}

	@Override
	public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
		Graphite.getScheduler().execute(() -> {
			GraphiteDebug.log(DebugCategory.MUSIC, exception);
		});
	}

}

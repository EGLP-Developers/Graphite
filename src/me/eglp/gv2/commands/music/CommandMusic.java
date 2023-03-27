package me.eglp.gv2.commands.music;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

import me.eglp.genius.entity.GeniusSearchHit;
import me.eglp.genius.entity.GeniusSongResult;
import me.eglp.genius.entity.lyrics.GeniusLyricsSection;
import me.eglp.genius.entity.lyrics.GeniusSongLyrics;
import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.GraphiteIcon;
import me.eglp.gv2.util.base.guild.GraphiteAudioChannel;
import me.eglp.gv2.util.base.guild.GraphiteGuild;
import me.eglp.gv2.util.base.guild.GraphiteModule;
import me.eglp.gv2.util.base.guild.music.GuildMusic;
import me.eglp.gv2.util.base.guild.music.GuildTrackManager;
import me.eglp.gv2.util.base.user.EasterEgg;
import me.eglp.gv2.util.command.Command;
import me.eglp.gv2.util.command.CommandCategory;
import me.eglp.gv2.util.command.CommandInvokedEvent;
import me.eglp.gv2.util.command.ParentCommand;
import me.eglp.gv2.util.command.slash.DeferredReply;
import me.eglp.gv2.util.emote.JDAEmote;
import me.eglp.gv2.util.input.ButtonInput;
import me.eglp.gv2.util.lang.DefaultLocaleString;
import me.eglp.gv2.util.lang.DefaultMessage;
import me.eglp.gv2.util.lang.GraphiteTimeParser;
import me.eglp.gv2.util.message.BigEmbedBuilder;
import me.eglp.gv2.util.music.GraphiteTrack;
import me.eglp.gv2.util.permission.DefaultPermissions;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;

public class CommandMusic extends ParentCommand {
	
	private static final int
		QUEUE_PAGE_SIZE = 10,
		SEARCH_RESULTS = 5;
	
	public CommandMusic() {
		super(GraphiteModule.MUSIC, CommandCategory.MUSIC, "music");
		setDescription(DefaultLocaleString.COMMAND_MUSIC_DESCRIPTION);
		
		addSubCommand(new Command(this, "play") {
			
			@Override
			public void action(CommandInvokedEvent event) {
				GraphiteAudioChannel channel = event.getMember().getCurrentAudioChannel();
				if(channel == null) {
					DefaultMessage.ERROR_NOT_IN_AUDIOCHANNEL.reply(event);
					return;
				}
				
				GraphiteGuild g = event.getGuild();
				
				String link = (String) event.getOption("what");
				if(!(link.startsWith("https://") || link.startsWith("http://"))) {
					link = "ytsearch: " + link;
				}
				
				DeferredReply r = event.deferReply();
				GuildMusic.loadTrack(link).thenAccept(result -> {
					List<GraphiteTrack> tracks = result.getResults();
					if(tracks.isEmpty()) {
						DefaultMessage.COMMAND_MUSIC_PLAY_TRACK_NOT_FOUND.reply(event);
						return;
					}
					
					if(result.isPlaylist() || result.getResults().size() == 1) {
						tracks.forEach(t -> g.getMusic().queue(t));
						g.getMusic().join(channel);
						
						r.editOriginal(buildPlayOrQueueMessage(g, tracks));
					}else {
						EmbedBuilder eb = new EmbedBuilder();
						eb.setDescription(DefaultLocaleString.COMMAND_MUSIC_PLAY_MORE_THAN_ONE_RESULT.getFor(g));
						List<GraphiteTrack> smolResults = tracks.subList(0, Math.min(tracks.size(), SEARCH_RESULTS));
						
						ButtonInput<Integer> inp = new ButtonInput<Integer>(event.getAuthor(), evt -> {
							if(evt.getItem() == -1) {
								evt.markCancelled();
								return;
							}
							
							g.getMusic().queue(smolResults.get(evt.getItem()));
							g.getMusic().join(channel);
							evt.getJDAEvent().editMessage(new MessageEditBuilder().setEmbeds(buildPlayOrQueueMessage(g, Collections.singletonList(smolResults.get(evt.getItem())))).build())
								.setReplace(true)
								.queue();
//							evt.acknowledge();
						})
						.autoRemove(true)
						.removeMessage(false);
						int i = 0;
						for(GraphiteTrack tr : smolResults) {
							eb.addField((i + 1) + ") " + tr.getLavaTrack().getInfo().title, tr.getLavaTrack().getInfo().author, false);
							inp.addOption(ButtonStyle.PRIMARY, JDAEmote.getKeycapNumber(i + 1), i);
							i++;
						}
						if(smolResults.size() % 5 == 0) inp.newRow();
						inp.addOption(ButtonStyle.SECONDARY, "Cancel", -1);
						
						MessageEditBuilder mb = new MessageEditBuilder().setEmbeds(eb.build());
						inp.apply(mb);
						r.editOriginal(mb.build());
					}
				}).exceptionally(ex -> {
					DefaultMessage.COMMAND_MUSIC_PLAY_CANNOT_PLAY.reply(event, "error_message", ex.getCause().getMessage());
					return null;
				});
			}
			
			@Override
			public List<OptionData> getOptions() {
				return Arrays.asList(
						new OptionData(OptionType.STRING, "what", "Link or search query", true)
					);
			}
		})
		.setDescription(DefaultLocaleString.COMMAND_MUSIC_PLAY_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_MUSIC_PLAY_USAGE)
		.setPermission(DefaultPermissions.MUSIC_PLAY);
		
		addSubCommand(new Command(this, "pause") {
			
			@Override
			public void action(CommandInvokedEvent event) {
				if(!event.getGuild().getMusic().isPlaying()) {
					DefaultMessage.COMMAND_MUSIC_NOT_PLAYING.reply(event);
					return;
				}
				event.getGuild().getMusic().setPaused(true);
				DefaultMessage.COMMAND_MUSIC_PAUSE_MESSAGE.reply(event, "prefix", event.getPrefixUsed());
			}
			
			@Override
			public List<OptionData> getOptions() {
				return Collections.emptyList();
			}
		})
		.setDescription(DefaultLocaleString.COMMAND_MUSIC_PAUSE_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_MUSIC_PAUSE_USAGE)
		.setPermission(DefaultPermissions.MUSIC_PAUSE);
		
		addSubCommand(new Command(this, "stop") {
			
			@Override
			public void action(CommandInvokedEvent event) {
				if(!event.getGuild().getMusic().isPlaying()) {
					DefaultMessage.COMMAND_MUSIC_NOT_PLAYING.reply(event);
					return;
				}
				
				event.getGuild().getMusic().stop();
				event.react(JDAEmote.WAVE);
			}
			
			@Override
			public List<OptionData> getOptions() {
				return Collections.emptyList();
			}
		})
		.setDescription(DefaultLocaleString.COMMAND_MUSIC_STOP_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_MUSIC_STOP_USAGE)
		.setPermission(DefaultPermissions.MUSIC_STOP);
		
		addSubCommand(new Command(this, "loop") {
			
			@Override
			public void action(CommandInvokedEvent event) {
				if(!event.getGuild().getMusic().isPlaying()) {
					DefaultMessage.COMMAND_MUSIC_NOT_PLAYING.reply(event);
					return;
				}
				
				if(event.getGuild().getMusic().isLooping()) {
					event.getGuild().getMusic().setLooping(false);
					DefaultMessage.COMMAND_MUSIC_UNLOOP_MESSAGE.reply(event);
				}else {
					event.getGuild().getMusic().setLooping(true);
					event.react(JDAEmote.REPEAT_ONE);
				}
			}
			
			@Override
			public List<OptionData> getOptions() {
				return Collections.emptyList();
			}
		})
		.setDescription(DefaultLocaleString.COMMAND_MUSIC_LOOP_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_MUSIC_LOOP_USAGE)
		.setPermission(DefaultPermissions.MUSIC_LOOP);
		
		addSubCommand(new Command(this, "volume") {
			
			@Override
			public void action(CommandInvokedEvent event) {
				if(!event.getGuild().getMusic().isPlaying()) {
					DefaultMessage.COMMAND_MUSIC_NOT_PLAYING.reply(event);
					return;
				}
				
				long volume = (long) event.getOption("volume");
				if(volume == 9001) {
					event.getGuild().getMusic().setVolume(300);
					event.reply("UzUzUz let's earrape. Set volume to over 9000");
					if(!event.getAuthor().getConfig().hasFoundEasterEgg(EasterEgg.MUSIC_VOLUME_EARRAPE)) {
						event.getAuthor().getConfig().addEasterEgg(EasterEgg.MUSIC_VOLUME_EARRAPE, true);
					}
					return;
				}
				
				if(volume < 0 || volume > 100) {
					DefaultMessage.COMMAND_MUSIC_VOLUME_INVALID_VOLUME.reply(event);
					return;
				}
				
				event.getGuild().getMusic().setVolume((int) volume);
				DefaultMessage.COMMAND_MUSIC_VOLUME_MESSAGE.reply(event, "volume", String.valueOf(volume));				
			}
			
			@Override
			public List<OptionData> getOptions() {
				return Arrays.asList(
						new OptionData(OptionType.INTEGER, "volume", "The new volume", true)
					);
			}
		})
		.setDescription(DefaultLocaleString.COMMAND_MUSIC_VOLUME_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_MUSIC_VOLUME_USAGE)
		.setPermission(DefaultPermissions.MUSIC_VOLUME);
		
		addSubCommand(new Command(this, "queue") {
			
			@Override
			public void action(CommandInvokedEvent event) {
				if(!event.getGuild().getMusic().isPlaying()) {
					DefaultMessage.COMMAND_MUSIC_NOT_PLAYING.reply(event);
					return;
				}
				
				List<GraphiteTrack> full = event.getGuild().getMusic().getQueue().getFullQueue();
				if(full.isEmpty()) {
					DefaultMessage.COMMAND_MUSIC_EMPTY_QUEUE.reply(event);
					return;
				}
				
				int maxPage = (int) Math.ceil(full.size() / (double) QUEUE_PAGE_SIZE);
				int page = event.hasOption("page") ? (int) ((long) event.getOption("page")) : (Math.floorDiv(event.getGuild().getMusic().getQueue().getCurrentIndex(), QUEUE_PAGE_SIZE) + 1);
				if(page > maxPage || page <= 0) {
					DefaultMessage.COMMAND_MUSIC_QUEUE_INVALID_PAGE.reply(event,
							"max_page", ""+maxPage);
					return;
				}
				
				List<String> tracks = new ArrayList<>();
				GraphiteTrack[] qa = full.toArray(new GraphiteTrack[full.size()]);
				for(int i = (page - 1) * QUEUE_PAGE_SIZE; i < Math.min(page * QUEUE_PAGE_SIZE, qa.length); i++) {
					tracks.add(buildQueueMessage(event.getGuild(), i + 1, qa[i].getLavaTrack().getInfo(), event.getGuild().getMusic().getQueue().getCurrentIndex() == i));
				}
				
				String out = tracks.stream().collect(Collectors.joining("\n\n"));
				DefaultMessage.COMMAND_MUSIC_QUEUE_MESSAGE.reply(event,
						"tracks", "" + full.size(),
						"page", "" + page,
						"max_page", "" + maxPage,
						"queue", out);
			}
			
			@Override
			public List<OptionData> getOptions() {
				return Arrays.asList(
						new OptionData(OptionType.INTEGER, "page", "Page of the queue to show")
					);
			}
		})
		.setDescription(DefaultLocaleString.COMMAND_MUSIC_QUEUE_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_MUSIC_QUEUE_USAGE)
		.setPermission(DefaultPermissions.MUSIC_QUEUE);
		
		addSubCommand(new Command(this, "remove") {
			
			@Override
			public void action(CommandInvokedEvent event) {
				if(!event.getGuild().getMusic().isPlaying()) {
					DefaultMessage.COMMAND_MUSIC_NOT_PLAYING.reply(event);
					return;
				}
				
				int max = event.getGuild().getMusic().getQueue().getFullQueue().size();
				
				int idx = (int) ((long) event.getOption("index"));
				GraphiteTrack t = event.getGuild().getMusic().removeAbsolute(idx - 1);
				if(t == null) {
					DefaultMessage.COMMAND_MUSIC_REMOVE_INVALID_INDEX.reply(event, "min_index", "1", "max_index", ""+max);
					return;
				}
				
				DefaultMessage.COMMAND_MUSIC_REMOVE_REMOVED.reply(event, "track", t.getLavaTrack().getInfo().title);
			}
			
			@Override
			public List<OptionData> getOptions() {
				return Arrays.asList(
						new OptionData(OptionType.INTEGER, "index", "Index of the track to remove", true)
					);
			}
		})
		.setDescription(DefaultLocaleString.COMMAND_MUSIC_REMOVE_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_MUSIC_REMOVE_USAGE)
		.setPermission(DefaultPermissions.MUSIC_REMOVE);
		
		addSubCommand(new Command(this, "seek") {
			
			@Override
			public void action(CommandInvokedEvent event) {
				if(!event.getGuild().getMusic().isPlaying()) {
					DefaultMessage.COMMAND_MUSIC_NOT_PLAYING.reply(event);
					return;
				}
				
				String where = (String) event.getOption("where");
				long dur = GraphiteTimeParser.parseDuration(event.getGuild(), where);
				if(dur == -1) {
					dur = GraphiteTimeParser.parseTimestamp(where);
					if(dur == -1) {
						DefaultMessage.ERROR_INVALID_TIMESTAMP.reply(event);
						return;
					}
				}
				
				boolean b = event.getGuild().getMusic().seek(dur);
				if(!b) {
					DefaultMessage.COMMAND_MUSIC_SEEK_CANNOT_SEEK.reply(event);
					return;
				}
				
				DefaultMessage.COMMAND_MUSIC_SEEK_MESSAGE.reply(event);
			}
			
			@Override
			public List<OptionData> getOptions() {
				return Arrays.asList(
						new OptionData(OptionType.STRING, "where", "Where to seek to", true)
					);
			}
		})
		.setDescription(DefaultLocaleString.COMMAND_MUSIC_SEEK_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_MUSIC_SEEK_USAGE)
		.setPermission(DefaultPermissions.MUSIC_SEEK);
		
		addSubCommand(new Command(this, "fastforward") {
			
			@Override
			public void action(CommandInvokedEvent event) {
				if(!event.getGuild().getMusic().isPlaying()) {
					DefaultMessage.COMMAND_MUSIC_NOT_PLAYING.reply(event);
					return;
				}
				
				String amount = (String) event.getOption("amount");
				long dur = GraphiteTimeParser.parseDuration(event.getGuild(), amount);
				if(dur == -1) {
					dur = GraphiteTimeParser.parseTimestamp(amount);
					if(dur == -1) {
						DefaultMessage.ERROR_INVALID_TIMESTAMP.reply(event);
						return;
					}
				}
				
				boolean b = event.getGuild().getMusic().fastForward(dur);
				if(!b) {
					DefaultMessage.COMMAND_MUSIC_FASTFORWARD_CANNOT_FAST_FORWARD.reply(event);
					return;
				}
				
				DefaultMessage.COMMAND_MUSIC_FASTFORWARD_MESSAGE.reply(event);
			}
			
			@Override
			public List<OptionData> getOptions() {
				return Arrays.asList(
						new OptionData(OptionType.STRING, "amount", "How far to fast-forward", true)
					);
			}
		})
		.addAlias("ff")
		.setDescription(DefaultLocaleString.COMMAND_MUSIC_FASTFORWARD_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_MUSIC_FASTFORWARD_USAGE)
		.setPermission(DefaultPermissions.MUSIC_FASTFORWARD);
		
		addSubCommand(new Command(this, "rewind") {
			
			@Override
			public void action(CommandInvokedEvent event) {
				if(!event.getGuild().getMusic().isPlaying()) {
					DefaultMessage.COMMAND_MUSIC_NOT_PLAYING.reply(event);
					return;
				}
				
				String amount = (String) event.getOption("amount");
				long dur = GraphiteTimeParser.parseDuration(event.getGuild(), amount);
				if(dur == -1) {
					dur = GraphiteTimeParser.parseTimestamp(amount);
					if(dur == -1) {
						DefaultMessage.ERROR_INVALID_TIMESTAMP.reply(event);
						return;
					}
				}
				
				boolean b = event.getGuild().getMusic().rewind(dur);
				if(!b) {
					DefaultMessage.COMMAND_MUSIC_REWIND_CANNOT_REWIND.reply(event);
					return;
				}
				
				DefaultMessage.COMMAND_MUSIC_REWIND_MESSAGE.reply(event);
			}
			
			@Override
			public List<OptionData> getOptions() {
				return Arrays.asList(
						new OptionData(OptionType.STRING, "amount", "How far to rewind", true)
					);
			}
		})
		.addAlias("rw")
		.setDescription(DefaultLocaleString.COMMAND_MUSIC_REWIND_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_MUSIC_REWIND_USAGE)
		.setPermission(DefaultPermissions.MUSIC_REWIND);
		
		addSubCommand(new Command(this, "resume") {
			
			@Override
			public void action(CommandInvokedEvent event) {
				if(!event.getGuild().getMusic().isPlaying()) {
					DefaultMessage.COMMAND_MUSIC_NOT_PLAYING.reply(event);
					return;
				}
				event.getGuild().getMusic().setPaused(false);
				DefaultMessage.COMMAND_MUSIC_RESUME_MESSAGE.reply(event);
			}
			
			@Override
			public List<OptionData> getOptions() {
				return Collections.emptyList();
			}
		})
		.setDescription(DefaultLocaleString.COMMAND_MUSIC_RESUME_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_MUSIC_RESUME_USAGE)
		.setPermission(DefaultPermissions.MUSIC_RESUME);
		
		addSubCommand(new Command(this, "endless") {
			
			@Override
			public void action(CommandInvokedEvent event) {
				if(!event.getGuild().getMusic().isPlaying()) {
					DefaultMessage.COMMAND_MUSIC_NOT_PLAYING.reply(event);
					return;
				}
				
				boolean isEndless = event.getGuild().getMusic().isEndless();
				if(isEndless) {
					event.getGuild().getMusic().setEndless(false);
					DefaultMessage.COMMAND_MUSIC_SET_ENDLESS_FALSE.reply(event);
				}else {
					event.getGuild().getMusic().setEndless(true);
					DefaultMessage.COMMAND_MUSIC_SET_ENDLESS_TRUE.reply(event);
				}
			}
			
			@Override
			public List<OptionData> getOptions() {
				return Collections.emptyList();
			}
		})
		.setDescription(DefaultLocaleString.COMMAND_MUSIC_ENDLESS_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_MUSIC_ENDLESS_USAGE)
		.setPermission(DefaultPermissions.MUSIC_ENDLESS);
		
		addSubCommand(new Command(this, "shuffle") {
			
			@Override
			public void action(CommandInvokedEvent event) {
				if(!event.getGuild().getMusic().isPlaying()) {
					DefaultMessage.COMMAND_MUSIC_NOT_PLAYING.reply(event);
					return;
				}
				
				event.getGuild().getMusic().shuffleQueue();
				DefaultMessage.COMMAND_MUSIC_SHUFFLE_MESSAGE.reply(event);
			}
			
			@Override
			public List<OptionData> getOptions() {
				return Collections.emptyList();
			}
		})
		.setDescription(DefaultLocaleString.COMMAND_MUSIC_SHUFFLE_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_MUSIC_SHUFFLE_USAGE)
		.setPermission(DefaultPermissions.MUSIC_SHUFFLE);
		
		addSubCommand(new Command(this, "skip") {
			
			@Override
			public void action(CommandInvokedEvent event) {
				if(!event.getGuild().getMusic().isPlaying()) {
					DefaultMessage.COMMAND_MUSIC_NOT_PLAYING.reply(event);
					return;
				}
				
				int amount = event.hasOption("amount") ? (int) ((long) event.getOption("amount")) : 1;
				if(amount < 1) {
					DefaultMessage.COMMAND_MUSIC_SKIP_INVALID_AMOUNT.reply(event);
					return;
				}
				
				event.getGuild().getMusic().skip(amount);
				DefaultMessage.COMMAND_MUSIC_SKIP_MESSAGE.reply(event, "amount", ""+amount);
			}
			
			@Override
			public List<OptionData> getOptions() {
				return Arrays.asList(
						new OptionData(OptionType.INTEGER, "amount", "Amount of songs to skip", false)
					);
			}
		})
		.setDescription(DefaultLocaleString.COMMAND_MUSIC_SKIP_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_MUSIC_SKIP_USAGE)
		.setPermission(DefaultPermissions.MUSIC_SKIP);
		
		addSubCommand(new Command(this, "jump") {
			
			@Override
			public void action(CommandInvokedEvent event) {
				if(!event.getGuild().getMusic().isPlaying()) {
					DefaultMessage.COMMAND_MUSIC_NOT_PLAYING.reply(event);
					return;
				}
				
				int index = (int) ((long) event.getOption("index"));
				if(index < 1) {
					DefaultMessage.COMMAND_MUSIC_JUMP_INVALID_VALUE.reply(event);
					return;
				}
				
				event.getGuild().getMusic().jump(index - 1);
				DefaultMessage.COMMAND_MUSIC_JUMP_MESSAGE.reply(event, "index", String.valueOf(index));
			}
			
			@Override
			public List<OptionData> getOptions() {
				return Arrays.asList(
						new OptionData(OptionType.INTEGER, "index", "Index of the song to jump to", true)
					);
			}
		})
		.setDescription(DefaultLocaleString.COMMAND_MUSIC_JUMP_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_MUSIC_JUMP_USAGE)
		.setPermission(DefaultPermissions.MUSIC_JUMP);
		
		addSubCommand(new Command(this, "nowplaying") {
			
			@Override
			public void action(CommandInvokedEvent event) {
				GraphiteGuild g = event.getGuild();
				if(!g.getMusic().isPlaying()) {
					DefaultMessage.COMMAND_MUSIC_NOT_PLAYING.reply(event);
					return;
				}
				
				event.reply(buildPlayOrQueueMessage(g, null));
			}
			
			@Override
			public List<OptionData> getOptions() {
				return Collections.emptyList();
			}
		})
		.addAlias("np")
		.setDescription(DefaultLocaleString.COMMAND_MUSIC_NOWPLAYING_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_MUSIC_NOWPLAYING_USAGE)
		.setPermission(DefaultPermissions.MUSIC_NOWPLAYING);
		
		addSubCommand(new Command(this, "lyrics") {
			
			@Override
			public void action(CommandInvokedEvent event) {
				List<String> translationAccounts = Arrays.asList( // To filter translations
						"https://genius.com/artists/Genius-arabic-translations",
						"https://genius.com/artists/Genius-azrbaycan-trcum",
						"https://genius.com/artists/Genius-brasil-traducoes",
						"https://genius.com/artists/Genius-nederlandse-vertalingen",
						"https://genius.com/artists/Genius-english-translations",
						"https://genius.com/artists/Genius-farsi-translations",
						"https://genius.com/artists/Genius-traductions-francaises",
						"https://genius.com/artists/Genius-deutsche-ubersetzungen",
						"https://genius.com/artists/Genius-hebrew-translations",
						"https://genius.com/artists/Genius-traduzioni-italiane",
						"https://genius.com/artists/Genius-japanese-translations",
						"https://genius.com/artists/Genius-polska-tumaczenia",
						"https://genius.com/artists/Genius-romanizations",
						"https://genius.com/artists/Genius-russian-translations",
						"https://genius.com/artists/Genius-slovensky-preklad",
						"https://genius.com/artists/Genius-south-africa-translations",
						"https://genius.com/artists/Genius-traducciones-al-espanol",
						"https://genius.com/artists/Genius-swedish-translations",
						"https://genius.com/artists/Genius-thai-translations",
						"https://genius.com/artists/Genius-turkce-ceviri"
					);
					
					AudioTrackInfo info;
					String search;
					if(!event.hasOption("query")) {
						if(!event.getGuild().getMusic().isPlaying()) {
							DefaultMessage.COMMAND_MUSIC_NOT_PLAYING.reply(event);
							return;
						}
		
						info = event.getGuild().getMusic().getPlayingTrack().getLavaTrack().getInfo();
						search = info.title // Title with extra info stripped out to make search results better
								.replaceAll("\\[.+?\\]", "")
								.replaceAll("\\(.+?\\)", "")
								.replaceAll("(ft\\.|feat\\.).++", "")
								.trim();
					}else {
						info = null;
						search = (String) event.getOption("query");
					}
					
					EmbedBuilder b2 = new EmbedBuilder();
					b2.setDescription(DefaultLocaleString.COMMAND_MUSIC_LYRICS_AUTHOR_SEARCHING_DESCRIPTION.getFor(event.getSender(), "song", search));
					b2.setAuthor(
							DefaultLocaleString.COMMAND_MUSIC_LYRICS_AUTHOR_SEARCHING.getFor(event.getSender()),
							null,
							GraphiteIcon.LOADING_GIF.getPath());
					
					DeferredReply reply = event.deferReply(b2.build());
					
					List<GeniusSearchHit> hit = Graphite.getGenius().getGeniusAPI().search(search);
					
					List<GeniusSongResult> filteredResults = hit.stream()
							.map(h -> h.getResult())
							.filter(r -> !translationAccounts.contains(r.getPrimaryArtist().getURL()))
							.collect(Collectors.toList());
					
					if(filteredResults.isEmpty()) {
						reply.editOriginal(DefaultMessage.COMMAND_MUSIC_LYRICS_NO_RESULTS.createEmbed(event.getSender()));
						return;
					}
					
					GeniusSongResult res = filteredResults.stream()
							.filter(r -> Graphite.getGenius().getGeniusAPI().getSong(r.getID()).getMedia().stream()
									.anyMatch(me -> info == null || me.getURL().contains(info.identifier)))
							.findFirst().orElse(filteredResults.get(0));
					
					GeniusSongLyrics lyrics = res.retrieveLyrics();
					
					BigEmbedBuilder b = new BigEmbedBuilder();
					for(GeniusLyricsSection s : lyrics.getSections()) b.addField(s.getTitle(),  s.getText().isEmpty() ? EmbedBuilder.ZERO_WIDTH_SPACE : s.getText(), false);
					b.setAuthor(res.getFullTitle(), res.getURL(), res.getSongArtImageThumbnailURL());
					b.setFooter("Lyrics may not be accurate | Powered by Genius");
					List<MessageEmbed> embeds = b.build(BigEmbedBuilder.SPLIT_NEWLINES);
					reply.editOriginal(embeds.remove(0));
					for(MessageEmbed e : embeds) {
						event.reply(e);
					}
			}
			
			@Override
			public List<OptionData> getOptions() {
				return Arrays.asList(
						new OptionData(OptionType.STRING, "query", "Search query for when you want to search for lyrics yourself")
					);
			}
		})
		.setDescription(DefaultLocaleString.COMMAND_MUSIC_LYRICS_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_MUSIC_LYRICS_USAGE)
		.setPermission(DefaultPermissions.MUSIC_LYRICS);
		
		addSubCommand(new Command(this, "bassboost") {
			
			@Override
			public void action(CommandInvokedEvent event) {
				if(!event.getGuild().getMusic().isPlaying()) {
					DefaultMessage.COMMAND_MUSIC_NOT_PLAYING.reply(event);
					return;
				}
				
				int level = (int) ((long) event.getOption("level"));
				if(level < 0 || level > GuildTrackManager.BASS_BOOST_LEVELS.length) {
					DefaultMessage.COMMAND_MUSIC_BASSBOOST_INVALID_LEVEL.reply(event, "max_level", ""+GuildTrackManager.BASS_BOOST_LEVELS.length);
					return;
				}
				
				event.getGuild().getMusic().setBassBoostLevel(level);
				if(level == 0) {
					DefaultMessage.COMMAND_MUSIC_BASSBOOST_DISABLED.reply(event);
				}else {
					DefaultMessage.COMMAND_MUSIC_BASSBOOST_ENABLED.reply(event, "level", ""+level);
				}
			}
			
			@Override
			public List<OptionData> getOptions() {
				OptionData d = new OptionData(OptionType.INTEGER, "level", "Amount of bass boost to add", true);
				for(int i = 0; i <= GuildTrackManager.BASS_BOOST_LEVELS.length; i++) {
					d.addChoice(i == 0 ? "Disabled" : "Level " + i, i);
				}
				return Arrays.asList(
						d
					);
			}
		})
		.setDescription(DefaultLocaleString.COMMAND_MUSIC_BASSBOOST_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_MUSIC_BASSBOOST_USAGE)
		.setPermission(DefaultPermissions.MUSIC_BASSBOOST)
		.addAlias("bb");
		
		addSubCommand(new Command(this, "speed") {
			
			@Override
			public void action(CommandInvokedEvent event) {
				if(!event.getGuild().getMusic().isPlaying()) {
					DefaultMessage.COMMAND_MUSIC_NOT_PLAYING.reply(event);
					return;
				}
				
				String speedStr = (String) event.getOption("speed");
				double speed;
				try {
					speed = Double.parseDouble(speedStr);
				}catch(NumberFormatException e) {
					DefaultMessage.COMMAND_MUSIC_SPEED_INVALID_SPEED.reply(event, "min_speed", String.valueOf(GuildTrackManager.MIN_SPEED), "max_speed", String.valueOf(GuildTrackManager.MAX_SPEED));
					return;
				}
				
				if(speed < GuildTrackManager.MIN_SPEED || speed > GuildTrackManager.MAX_SPEED) {
					DefaultMessage.COMMAND_MUSIC_SPEED_INVALID_SPEED.reply(event, "min_speed", String.valueOf(GuildTrackManager.MIN_SPEED), "max_speed", String.valueOf(GuildTrackManager.MAX_SPEED));
					return;
				}
				
				event.getGuild().getMusic().setSpeed(speed);
				DefaultMessage.COMMAND_MUSIC_SPEED_MESSAGE.reply(event, "speed", String.valueOf(speed));
			}
			
			@Override
			public List<OptionData> getOptions() {
				return Arrays.asList(new OptionData(OptionType.STRING, "speed", "The new playback speed (" + GuildTrackManager.MIN_SPEED + " - " + GuildTrackManager.MAX_SPEED + ")", true));
			}
		})
		.setDescription(DefaultLocaleString.COMMAND_MUSIC_SPEED_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_MUSIC_SPEED_USAGE)
		.setPermission(DefaultPermissions.MUSIC_SPEED);
		
		addSubCommand(new Command(this, "pitch") {
			
			@Override
			public void action(CommandInvokedEvent event) {
				if(!event.getGuild().getMusic().isPlaying()) {
					DefaultMessage.COMMAND_MUSIC_NOT_PLAYING.reply(event);
					return;
				}
				
				String pitchStr = (String) event.getOption("pitch");
				double pitch;
				try {
					pitch = Double.parseDouble(pitchStr);
				}catch(NumberFormatException e) {
					DefaultMessage.COMMAND_MUSIC_PITCH_INVALID_PITCH.reply(event, "min_pitch", String.valueOf(GuildTrackManager.MIN_PITCH), "max_pitch", String.valueOf(GuildTrackManager.MAX_PITCH));
					return;
				}
				
				if(pitch < GuildTrackManager.MIN_PITCH || pitch > GuildTrackManager.MAX_PITCH) {
					DefaultMessage.COMMAND_MUSIC_PITCH_INVALID_PITCH.reply(event, "min_pitch", String.valueOf(GuildTrackManager.MIN_PITCH), "max_pitch", String.valueOf(GuildTrackManager.MAX_PITCH));
					return;
				}
				
				event.getGuild().getMusic().setPitch(pitch);
				DefaultMessage.COMMAND_MUSIC_PITCH_MESSAGE.reply(event, "pitch", String.valueOf(pitch));
			}
			
			@Override
			public List<OptionData> getOptions() {
				return Arrays.asList(new OptionData(OptionType.STRING, "pitch", "The new pitch (" + GuildTrackManager.MIN_PITCH + " - " + GuildTrackManager.MAX_PITCH + ")", true));
			}
		})
		.setDescription(DefaultLocaleString.COMMAND_MUSIC_PITCH_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_MUSIC_PITCH_USAGE)
		.setPermission(DefaultPermissions.MUSIC_PITCH);
		
		addSubCommand(new Command(this, "nightcore") {
			
			@Override
			public void action(CommandInvokedEvent event) {
				if(!event.getGuild().getMusic().isPlaying()) {
					DefaultMessage.COMMAND_MUSIC_NOT_PLAYING.reply(event);
					return;
				}

				event.getGuild().getMusic().setSpeedAndPitch(1.25d, 1.25d);
				DefaultMessage.COMMAND_MUSIC_NIGHTCORE_MESSAGE.reply(event);
			}
			
			@Override
			public List<OptionData> getOptions() {
				return Collections.emptyList();
			}
		})
		.setDescription(DefaultLocaleString.COMMAND_MUSIC_NIGHTCORE_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_MUSIC_NIGHTCORE_USAGE)
		.setPermission(DefaultPermissions.MUSIC_NIGHTCORE);
		
		addSubCommand(new Command(this, "reset") {
			
			@Override
			public void action(CommandInvokedEvent event) {
				if(!event.getGuild().getMusic().isPlaying()) {
					DefaultMessage.COMMAND_MUSIC_NOT_PLAYING.reply(event);
					return;
				}

				GuildMusic m = event.getGuild().getMusic();
				m.reset();
				DefaultMessage.COMMAND_MUSIC_RESET_MESSAGE.reply(event);
			}
			
			@Override
			public List<OptionData> getOptions() {
				return Collections.emptyList();
			}
		})
		.setDescription(DefaultLocaleString.COMMAND_MUSIC_RESET_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_MUSIC_RESET_USAGE)
		.setPermission(DefaultPermissions.MUSIC_RESET);
		
		addSubCommand(new CommandMusicPlaylist(this));
	}

	private String buildQueueMessage(GraphiteGuild guild, int index, AudioTrackInfo info, boolean highlight) {
		return DefaultLocaleString.COMMAND_MUSIC_QUEUE_FORMAT.getFor(guild,
				"index", String.valueOf(index),
				"duration", GraphiteTimeParser.getTimestamp(info.length),
				"author", info.author,
				"title", info.title,
				"highlight", highlight ? "**" : "");
	}
	
	public static MessageEmbed buildPlayOrQueueMessage(GraphiteGuild guild, List<GraphiteTrack> queued) {
		EmbedBuilder eb = new EmbedBuilder();

		AudioTrackInfo inf = (queued == null ? guild.getMusic().getPlayingTrack() : queued.get(0)).getLavaTrack().getInfo();
		int idx = guild.getMusic().getQueue().getCurrentIndex() + 1;
		
		String position = GraphiteTimeParser.getTimestamp(guild.getMusic().getPosition());
		String duration = GraphiteTimeParser.getTimestamp(inf.length);
		
		DefaultLocaleString title = DefaultLocaleString.COMMAND_MUSIC_PLAY_CURRENT_PLAYING_FIELD_TITLE;
		if(queued != null) title = queued.size() > 1 ? DefaultLocaleString.COMMAND_MUSIC_PLAY_QUEUED_TRACK_FIELD_TITLE_MULTIPLE : DefaultLocaleString.COMMAND_MUSIC_PLAY_QUEUED_TRACK_FIELD_TITLE;
		
		eb.addField(title.getFor(guild, "number", queued == null ? "0" : ""+queued.size()), inf.title, false);
		
		
		if(queued == null) {
			eb.addField(DefaultLocaleString.COMMAND_MUSIC_PLAY_TRACK_INDEX_TRACKS_FIELD_TITLE.getFor(guild), idx + "/" + guild.getMusic().getQueue().getFullQueue().size(), true);
			eb.addField(DefaultLocaleString.COMMAND_MUSIC_PLAY_POSITION_DURATION_FIELD_TITLE.getFor(guild), position + "/" + duration, true);
		}else {
			eb.addField(DefaultLocaleString.COMMAND_MUSIC_PLAY_TRACKS_FIELD_TITLE.getFor(guild), ""+guild.getMusic().getQueue().getFullQueue().size(), true);
			eb.addField(DefaultLocaleString.COMMAND_MUSIC_PLAY_DURATION_FIELD_TITLE.getFor(guild), duration, true);
		}
		
		eb.addField(DefaultLocaleString.COMMAND_MUSIC_PLAY_AUTHOR_FIELD_TITLE.getFor(guild), inf.author, true);
		
		if(queued == null) {
			eb.addField(DefaultLocaleString.COMMAND_MUSIC_PLAY_SPEED_FIELD_TITLE.getFor(guild), ""+guild.getMusic().getSpeed() + "x", true);
			eb.addField(DefaultLocaleString.COMMAND_MUSIC_PLAY_PITCH_FIELD_TITLE.getFor(guild), ""+guild.getMusic().getPitch() + "x", true);
			eb.addField(DefaultLocaleString.COMMAND_MUSIC_PLAY_BASS_BOOST_FIELD_TITLE.getFor(guild), ""+guild.getMusic().getBassBoostLevel(), true);
		}
		
		return eb.build();
	}

}

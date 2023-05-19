package me.eglp.gv2.commands.music;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

import me.eglp.gv2.util.command.ParentCommand;
import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.util.command.Command;
import me.eglp.gv2.util.command.CommandInvokedEvent;
import me.eglp.gv2.util.lang.DefaultLocaleString;
import me.eglp.gv2.util.lang.DefaultMessage;
import me.eglp.gv2.util.music.GraphitePlaylist;
import me.eglp.gv2.util.music.GraphiteTrack;
import me.eglp.gv2.util.permission.DefaultPermissions;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class CommandMusicPlaylist extends ParentCommand {

	public static final Pattern PLAYLIST_NAME_PATTERN = Pattern.compile("(?:\\w| |-){1,64}");

	public CommandMusicPlaylist(Command parent) {
		super(parent, "playlist");
		setDescription(DefaultLocaleString.COMMAND_MUSIC_PLAYLIST_DESCRIPTION);

		addSubCommand(new Command(this, "play") {

			@Override
			public void action(CommandInvokedEvent event) {
				if(event.getMember().getCurrentAudioChannel() == null) {
					DefaultMessage.ERROR_NOT_IN_AUDIOCHANNEL.reply(event);
					return;
				}

				GraphitePlaylist pl = event.getGuild().getMusic().getPlaylistByName((String) event.getOption("playlist"));
				if(pl == null) {
					DefaultMessage.COMMAND_MUSIC_PLAYLIST_INVALID_PLAYLIST.reply(event);
					return;
				}

				pl.getTracks().forEach(t -> event.getGuild().getMusic().queue(t));
				event.getGuild().getMusic().join(event.getMember().getCurrentAudioChannel());
				event.reply(CommandMusic.buildPlayOrQueueMessage(event.getGuild(), pl.getTracks()));
			}

			@Override
			public List<OptionData> getOptions() {
				return Arrays.asList(
						new OptionData(OptionType.STRING, "playlist", "The name of the playlist to play", true)
					);
			}
		})
		.setDescription(DefaultLocaleString.COMMAND_MUSIC_PLAYLIST_PLAY_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_MUSIC_PLAYLIST_PLAY_USAGE)
		.setPermission(DefaultPermissions.MUSIC_PLAYLIST_PLAY);

		addSubCommand(new Command(this, "save") {

			@Override
			public void action(CommandInvokedEvent event) {
				String name = (String) event.getOption("name");
				if(name != null && event.getGuild().getMusic().getPlaylistByName(name) != null) {
					DefaultMessage.COMMAND_MUSIC_PLAYLIST_DUPLICATE_NAME.reply(event);
					return;
				}

				List<GraphiteTrack> allTracks = event.getGuild().getMusic().getQueue().getFullQueue();
				List<GraphiteTrack> saveableTracks = allTracks.stream()
						.filter(GraphiteTrack::canBeSaved)
						.collect(Collectors.toList());

				if(saveableTracks.isEmpty()) {
					DefaultMessage.COMMAND_MUSIC_PLAYLIST_SAVE_NO_SAVEABLE_TRACKS.reply(event);
					return;
				}

				if(saveableTracks.size() < allTracks.size()) {
					DefaultMessage.COMMAND_MUSIC_PLAYLIST_SAVE_UNSAVEABLE_TRACKS.reply(event, "saved", ""+saveableTracks.size(), "total", ""+allTracks.size());
				}

				GraphitePlaylist pl = event.getGuild().getMusic().createPlaylist(saveableTracks, name);
				DefaultMessage.COMMAND_MUSIC_PLAYLIST_SAVE_MESSAGE.reply(event,
						"name", pl.getName());
			}

			@Override
			public List<OptionData> getOptions() {
				return Arrays.asList(
						new OptionData(OptionType.STRING, "name", "A name for the playlist")
					);
			}
		})
		.setDescription(DefaultLocaleString.COMMAND_MUSIC_PLAYLIST_SAVE_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_MUSIC_PLAYLIST_SAVE_USAGE)
		.setPermission(DefaultPermissions.MUSIC_PLAYLIST_SAVE);

		addSubCommand(new Command(this, "delete") {

			@Override
			public void action(CommandInvokedEvent event) {
				GraphitePlaylist pl = event.getGuild().getMusic().getPlaylistByName((String) event.getOption("playlist"));
				if(pl == null) {
					DefaultMessage.COMMAND_MUSIC_PLAYLIST_INVALID_PLAYLIST.reply(event);
					return;
				}

				pl.delete();
				DefaultMessage.COMMAND_MUSIC_PLAYLIST_DELETE_MESSAGE.reply(event,
						"name", pl.getName());
			}

			@Override
			public List<OptionData> getOptions() {
				return Arrays.asList(
						new OptionData(OptionType.STRING, "playlist", "The name of the playlist to delete", true)
					);
			}
		})
		.setDescription(DefaultLocaleString.COMMAND_MUSIC_PLAYLIST_DELETE_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_MUSIC_PLAYLIST_DELETE_USAGE)
		.setPermission(DefaultPermissions.MUSIC_PLAYLIST_DELETE);

		addSubCommand(new Command(this, "rename") {

			@Override
			public void action(CommandInvokedEvent event) {
				String playlist = (String) event.getOption("playlist");
				String newName = (String) event.getOption("new-name");
				if(!PLAYLIST_NAME_PATTERN.matcher(newName).matches()) {
					DefaultMessage.COMMAND_MUSIC_PLAYLIST_RENAME_INVALID_NAME.reply(event);
					return;
				}

				GraphiteGuild g = event.getGuild();
				GraphitePlaylist pl = g.getMusic().getPlaylistByName(playlist);
				if(pl == null) {
					DefaultMessage.COMMAND_MUSIC_PLAYLIST_INVALID_PLAYLIST.reply(event);
					return;
				}

				pl.setName(newName);
				DefaultMessage.COMMAND_MUSIC_PLAYLIST_RENAME_MESSAGE.reply(event, "name", playlist, "new_name", newName);
			}

			@Override
			public List<OptionData> getOptions() {
				return Arrays.asList(
						new OptionData(OptionType.STRING, "playlist", "The name of the playlist to rename", true),
						new OptionData(OptionType.STRING, "new-name", "The new name for the playlist", true)
					);
			}
		})
		.setDescription(DefaultLocaleString.COMMAND_MUSIC_PLAYLIST_RENAME_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_MUSIC_PLAYLIST_RENAME_USAGE)
		.setPermission(DefaultPermissions.MUSIC_PLAYLIST_RENAME);

		addSubCommand(new Command(this, "list") {

			@Override
			public void action(CommandInvokedEvent event) {
				if(event.getGuild().getMusic().getPlaylists().isEmpty()) {
					DefaultMessage.COMMAND_MUSIC_PLAYLISTS_NO_PLAYLISTS.reply(event);
					return;
				}

				EmbedBuilder eb = new EmbedBuilder();
				eb.addField(DefaultLocaleString.COMMAND_MUSIC_PLAYLISTS_FIELD_TITLE.getFor(event.getGuild()),
						"```css\n" + event.getGuild().getMusic().getPlaylists().stream()
						.map(pl -> DefaultLocaleString.COMMAND_MUSIC_PLAYLISTS_CONTAINING_TRACKS.getFor(event.getGuild(),
								"playlist", pl.getName(),
								"owner", (pl.getOwner().isGuild() ? pl.getOwner().asGuild().getName() : pl.getOwner().asUser().getName()),
								"tracks", ""+pl.getTracks().size()))
						.collect(Collectors.joining("\n")) + "\n```", true);
				event.reply(eb.build());
			}

			@Override
			public List<OptionData> getOptions() {
				return Collections.emptyList();
			}
		})
		.setDescription(DefaultLocaleString.COMMAND_MUSIC_PLAYLIST_LIST_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_MUSIC_PLAYLIST_LIST_USAGE)
		.setPermission(DefaultPermissions.MUSIC_PLAYLIST_LIST);

		addSubCommand(new Command(this, "info") {

			@Override
			public void action(CommandInvokedEvent event) {
				String playlist = (String) event.getOption("playlist");

				GraphiteGuild g = event.getGuild();
				GraphitePlaylist pl = g.getMusic().getPlaylistByName(playlist);
				if(pl == null) {
					DefaultMessage.COMMAND_MUSIC_PLAYLIST_INVALID_PLAYLIST.reply(event);
					return;
				}

				DefaultMessage.COMMAND_MUSIC_PLAYLIST_INFO_MESSAGE.reply(event, "playlist", pl.getName(), "tracks", pl.getTracks().stream()
					.map(t -> {
						AudioTrackInfo inf = t.getLavaTrack().getInfo();
						return inf.title;
					})
					.collect(Collectors.joining("\n")));
			}

			@Override
			public List<OptionData> getOptions() {
				return Arrays.asList(
						new OptionData(OptionType.STRING, "playlist", "The name of the playlist", true)
					);
			}
		})
		.setDescription(DefaultLocaleString.COMMAND_MUSIC_PLAYLIST_INFO_DESCRIPTION)
		.setUsage(DefaultLocaleString.COMMAND_MUSIC_PLAYLIST_INFO_USAGE)
		.setPermission(DefaultPermissions.MUSIC_PLAYLIST_INFO);
	}

}

package me.eglp.gv2.util.backup.data.channels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import me.eglp.gv2.guild.GraphiteCategory;
import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.guild.GraphiteVoiceChannel;
import me.eglp.gv2.util.backup.IDMappings;
import me.eglp.gv2.util.backup.RestoreSelector;
import me.eglp.gv2.util.webinterface.js.JavaScriptValue;
import me.eglp.gv2.util.webinterface.js.WebinterfaceObject;
import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.mrcore.json.converter.JSONComplexListType;
import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONConverter;
import me.mrletsplay.mrcore.json.converter.JSONConvertible;
import me.mrletsplay.mrcore.json.converter.JSONValue;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.managers.GuildManager;

public class ChannelsData implements JSONConvertible, WebinterfaceObject {

	@JavaScriptValue(getter = "getTextChannels")
	@JSONValue
	@JSONComplexListType(BackupTextChannel.class)
	private List<BackupTextChannel> textChannels = Collections.emptyList();

	@JavaScriptValue(getter = "getNewsChannels")
	@JSONValue
	@JSONComplexListType(BackupNewsChannel.class)
	private List<BackupNewsChannel> newsChannels = Collections.emptyList();

	@JavaScriptValue(getter = "getVoiceChannels")
	@JSONValue
	@JSONComplexListType(BackupVoiceChannel.class)
	private List<BackupVoiceChannel> voiceChannels = Collections.emptyList();

	@JavaScriptValue(getter = "getStageChannels")
	@JSONValue
	@JSONComplexListType(BackupStageChannel.class)
	private List<BackupStageChannel> stageChannels = Collections.emptyList();

	@JavaScriptValue(getter = "getForumChannels")
	@JSONValue
	@JSONComplexListType(BackupForumChannel.class)
	private List<BackupForumChannel> forumChannels = Collections.emptyList();

	@JavaScriptValue(getter = "getCategories")
	@JSONValue
	@JSONComplexListType(BackupCategory.class)
	private List<BackupCategory> categories = Collections.emptyList();

	@JSONValue
	private String communityUpdatesChannel;

	@JSONValue
	private String rulesChannel;

	@JSONConstructor
	private ChannelsData() {}

	public ChannelsData(GraphiteGuild guild) {
		if(guild.getJDAGuild() == null) throw new IllegalStateException("Unknown guild or invalid context");

		this.textChannels = new ArrayList<>(guild.getTextChannels().stream()
				.filter(o -> o.getJDAChannel().getParentCategory() == null)
				.map(BackupTextChannel::new)
				.collect(Collectors.toList()));

		this.newsChannels = new ArrayList<>(guild.getNewsChannels().stream()
				.filter(o -> o.getJDAChannel().getParentCategory() == null)
				.map(BackupNewsChannel::new)
				.collect(Collectors.toList()));

		this.voiceChannels = new ArrayList<>(guild.getVoiceChannels().stream()
				.filter(o -> shouldBackupVoiceChannel(o) && o.getJDAChannel().getParentCategory() == null)
				.map(BackupVoiceChannel::new)
				.collect(Collectors.toList()));

		this.stageChannels = new ArrayList<>(guild.getStageChannels().stream()
				.filter(o -> o.getJDAChannel().getParentCategory() == null)
				.map(BackupStageChannel::new)
				.collect(Collectors.toList()));

		this.forumChannels = new ArrayList<>(guild.getForumChannels().stream()
				.filter(o -> o.getJDAChannel().getParentCategory() == null)
				.map(BackupForumChannel::new)
				.collect(Collectors.toList()));

		this.categories = new ArrayList<>(guild.getCategories().stream()
				.filter(c -> shouldBackupCategory(c))
				.map(BackupCategory::new)
				.collect(Collectors.toList()));

		Guild jdaGuild = guild.getJDAGuild();
		if(jdaGuild.getCommunityUpdatesChannel() != null) communityUpdatesChannel = guild.getTextChannel(jdaGuild.getCommunityUpdatesChannel()).getID();
		if(jdaGuild.getRulesChannel() != null) rulesChannel = guild.getTextChannel(jdaGuild.getRulesChannel()).getID();
	}

	public static boolean shouldBackupVoiceChannel(GraphiteVoiceChannel vc) {
		return !vc.getGuild().getChannelsConfig().isAutoCreatedChannel(vc)
				&& !vc.getGuild().getChannelsConfig().isUserChannel(vc);
	}

	public static boolean shouldBackupCategory(GraphiteCategory c) {
		return !c.equals(c.getGuild().getChannelsConfig().getUserChannelCategory());
	}

	public List<BackupTextChannel> getTextChannels() {
		return textChannels;
	}

	public List<BackupNewsChannel> getNewsChannels() {
		return newsChannels;
	}

	public List<BackupVoiceChannel> getVoiceChannels() {
		return voiceChannels;
	}

	public List<BackupStageChannel> getStageChannels() {
		return stageChannels;
	}

	public List<BackupCategory> getCategories() {
		return categories;
	}

	public void restore(GraphiteGuild guild, EnumSet<RestoreSelector> selectors, IDMappings mappings) {
		// Fist, delete all existing channels

		Guild jdaGuild = guild.getJDAGuild();
		TextChannel cUpdCh = jdaGuild.getCommunityUpdatesChannel();
		TextChannel rCh = jdaGuild.getRulesChannel();

		jdaGuild.getChannels().forEach(ch -> guild.discardChannel(ch));

		jdaGuild.getTextChannels().forEach(t -> {
			if(t.equals(cUpdCh) || t.equals(rCh)) return; // Can't delete yet
			t.delete().queue();
		});

		jdaGuild.getNewsChannels().forEach(t -> t.delete().queue());
		jdaGuild.getVoiceChannels().forEach(v -> v.delete().queue());
		jdaGuild.getStageChannels().forEach(v -> v.delete().queue());
		jdaGuild.getForumChannels().forEach(v -> v.delete().queue());
		jdaGuild.getCategories().forEach(c -> c.delete().queue());

		List<BackupChannel> channels = new ArrayList<>();
		channels.addAll(textChannels);
		channels.addAll(newsChannels);
		channels.addAll(voiceChannels);
		channels.addAll(stageChannels);
		channels.sort(Comparator.comparingInt(ch -> ch.getPosition()));
		channels.forEach(ch -> ch.restore(guild, null, mappings));
		categories.forEach(c -> c.restore(guild, mappings));

		if(jdaGuild.getFeatures().contains("COMMUNITY")) {
			GuildManager m = jdaGuild.getManager();
			if(cUpdCh != null && communityUpdatesChannel != null) {
				String newChannelID = mappings.getNewID(communityUpdatesChannel);
				m.setCommunityUpdatesChannel(guild.getTextChannelByID(newChannelID).getJDAChannel());
			}

			if(rulesChannel != null) {
				String newChannelID = mappings.getNewID(rulesChannel);
				m.setRulesChannel(guild.getTextChannelByID(newChannelID).getJDAChannel());
			}

			m.complete();
			if(cUpdCh != null && communityUpdatesChannel != null) cUpdCh.delete().queue(s -> {}, t -> {});
			if(rCh != null && rulesChannel != null) rCh.delete().queue(s -> {}, t -> {});
		}
	}

	public static ChannelsData load(String json) {
		return JSONConverter.decodeObject(new JSONObject(json), ChannelsData.class);
	}

}

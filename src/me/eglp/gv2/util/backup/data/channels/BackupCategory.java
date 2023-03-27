package me.eglp.gv2.util.backup.data.channels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import me.eglp.gv2.util.backup.IDMappings;
import me.eglp.gv2.util.backup.data.channels.BackupPermissionOverride.Type;
import me.eglp.gv2.util.base.guild.GraphiteCategory;
import me.eglp.gv2.util.base.guild.GraphiteGuild;
import me.eglp.gv2.util.webinterface.js.JavaScriptValue;
import me.eglp.gv2.util.webinterface.js.WebinterfaceObject;
import me.mrletsplay.mrcore.json.converter.JSONComplexListType;
import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONConvertible;
import me.mrletsplay.mrcore.json.converter.JSONValue;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
public class BackupCategory implements JSONConvertible, WebinterfaceObject {

	@JavaScriptValue(getter = "getID")
	@JSONValue
	private String id;
	
	@JavaScriptValue(getter = "getName")
	@JSONValue
	private String name;

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

	@JSONValue
	@JSONComplexListType(BackupPermissionOverride.class)
	private List<BackupPermissionOverride> permissionOverrides;
	
	@JSONConstructor
	private BackupCategory() {}
	
	public BackupCategory(GraphiteCategory graphiteChannel) {
		Category ch = graphiteChannel.getJDACategory();
		if(ch == null) throw new IllegalStateException("Unknown category or invalid context");
		
		this.id = graphiteChannel.getID();
		this.name = ch.getName();
		this.textChannels = new ArrayList<>(graphiteChannel.getTextChannels().stream()
				.map(BackupTextChannel::new)
				.collect(Collectors.toList()));
		
		this.newsChannels = new ArrayList<>(graphiteChannel.getNewsChannels().stream()
				.map(BackupNewsChannel::new)
				.collect(Collectors.toList()));

		this.voiceChannels = new ArrayList<>(graphiteChannel.getVoiceChannels().stream()
				.filter(c -> ChannelsData.shouldBackupVoiceChannel(c))
				.map(BackupVoiceChannel::new)
				.collect(Collectors.toList()));
		
		this.stageChannels = new ArrayList<>(graphiteChannel.getStageChannels().stream()
				.map(BackupStageChannel::new)
				.collect(Collectors.toList()));

		this.permissionOverrides = new ArrayList<>(ch.getPermissionOverrides().stream()
				.map(BackupPermissionOverride::new)
				.collect(Collectors.toList()));
	}

	public String getID() {
		return id;
	}

	public String getName() {
		return name;
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

	public List<BackupPermissionOverride> getPermissionOverrides() {
		return permissionOverrides;
	}
	
	public void restore(GraphiteGuild guild, IDMappings mappings) {
		ChannelAction<Category> c = guild.getJDAGuild().createCategory(name);
		
		permissionOverrides.stream()
			.filter(o -> o.getType() == Type.MEMBER)
			.forEach(o -> c.addMemberPermissionOverride(Long.parseLong(o.getID()), o.getAllowed(), o.getDenied()));
	
		permissionOverrides.stream()
			.filter(o -> o.getType() == Type.ROLE)
			.forEach(o -> {
				String newRoleID = mappings.getNewID(o.getID());
				if(newRoleID == null) return; // Because of the role rate limit, the role might not have been restored
				c.addRolePermissionOverride(Long.parseLong(newRoleID), o.getAllowed(), o.getDenied());
			});
		
		Category cat = c.complete();
		mappings.put(id, cat.getId());
		
		List<BackupChannel> channels = new ArrayList<>();
		channels.addAll(textChannels);
		channels.addAll(newsChannels);
		channels.addAll(voiceChannels);
		channels.addAll(stageChannels);
		channels.sort(Comparator.comparingInt(ch -> ch.getPosition()));
		channels.forEach(ch -> ch.restore(guild, cat, mappings));
	}
	
}

package me.eglp.gv2.util.backup.data.config.channels;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import me.eglp.gv2.util.backup.IDMappings;
import me.eglp.gv2.util.backup.RestoreSelector;
import me.eglp.gv2.util.base.guild.GraphiteGuild;
import me.eglp.gv2.util.base.guild.GraphiteTextChannel;
import me.eglp.gv2.util.base.guild.GraphiteVoiceChannel;
import me.eglp.gv2.util.base.guild.config.GuildChannelsConfig;
import me.mrletsplay.mrcore.json.converter.JSONComplexListType;
import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONConvertible;
import me.mrletsplay.mrcore.json.converter.JSONValue;

public class ChannelsConfigData implements JSONConvertible {
	
	@JSONValue
	private String supportQueue;
	
	@JSONValue
	private String modLog;
	
	@JSONValue
	@JSONComplexListType(BackupAutoChannel.class)
	private List<BackupAutoChannel> autoChannels;
	
	@JSONConstructor
	private ChannelsConfigData() {}
	
	public ChannelsConfigData(GraphiteGuild guild) {
		GuildChannelsConfig c = guild.getChannelsConfig();
		this.supportQueue = c.getSupportQueue() == null ? null : c.getSupportQueue().getID();
		this.modLog = c.getModLogChannel() == null ? null : c.getModLogChannel().getID();
		this.autoChannels = c.getAutoChannels().stream()
				.map(BackupAutoChannel::new)
				.collect(Collectors.toList());
	}
	
	public String getSupportQueue() {
		return supportQueue;
	}
	
	public String getModLog() {
		return modLog;
	}
	
	public List<BackupAutoChannel> getAutoChannels() {
		return autoChannels;
	}
	
	public void restore(GraphiteGuild guild, EnumSet<RestoreSelector> selectors, IDMappings mappings) {
		GuildChannelsConfig c = guild.getChannelsConfig();
		
		if(RestoreSelector.SUPPORT.appliesTo(selectors)) {
			if(supportQueue != null) {
				String newSupportQueue = mappings.getNewID(supportQueue);
				GraphiteVoiceChannel ch = guild.getVoiceChannelByID(newSupportQueue);
				c.setSupportQueue(ch);
			}
		}
		
		if(RestoreSelector.MODERATION_AUTOMOD.appliesTo(selectors)) {
			if(modLog != null) {
				String newModLog = mappings.getNewID(modLog);
				GraphiteTextChannel ch = guild.getTextChannelByID(newModLog);
				c.setModLogChannel(ch);
			}
		}
		
		if(RestoreSelector.CHANNEL_MANAGEMENT.appliesTo(selectors)) {
			guild.getChannelsConfig().setAutoChannels(autoChannels.stream()
					.map(a -> a.restore(guild, mappings))
					.filter(Objects::nonNull)
					.collect(Collectors.toList()));
		}
	}

}

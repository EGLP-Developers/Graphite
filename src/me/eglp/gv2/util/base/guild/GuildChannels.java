package me.eglp.gv2.util.base.guild;

import java.util.List;
import java.util.stream.Collectors;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.NewsChannel;
import net.dv8tion.jda.api.entities.channel.concrete.StageChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

public interface GuildChannels {
	
	public Guild getJDAGuild();
	
	public GraphiteGuildChannel getGuildChannel(GuildChannel channel);
	
	public default GraphiteGuildChannel getGuildChannelByID(String id) {
		return getGuildChannel(getJDAGuild().getGuildChannelById(id));
	}
	
	public default GraphiteTextChannel getTextChannel(TextChannel channel) {
		return (GraphiteTextChannel) getGuildChannel(channel);
	}
	
	public default GraphiteTextChannel getTextChannelByID(String id) {
		return getTextChannel(getJDAGuild().getTextChannelById(id));
	}
	
	public default List<GraphiteTextChannel> getTextChannels() {
		return getJDAGuild().getTextChannels().stream().map(this::getTextChannel).collect(Collectors.toList());
	}
	
	public default List<GraphiteTextChannel> getTextChannelsByName(String name, boolean ignoreCase) {
		return getJDAGuild().getTextChannelsByName(name, ignoreCase).stream().map(this::getTextChannel).collect(Collectors.toList());
	}
	
	public default GraphiteNewsChannel getNewsChannel(NewsChannel channel) {
		return (GraphiteNewsChannel) getGuildChannel(channel);
	}
	
	public default GraphiteNewsChannel getNewsChannelByID(String id) {
		return getNewsChannel(getJDAGuild().getNewsChannelById(id));
	}
	
	public default List<GraphiteNewsChannel> getNewsChannels() {
		return getJDAGuild().getNewsChannels().stream().map(this::getNewsChannel).collect(Collectors.toList());
	}
	
	public default List<GraphiteNewsChannel> getNewsChannelsByName(String name, boolean ignoreCase) {
		return getJDAGuild().getNewsChannelsByName(name, ignoreCase).stream().map(this::getNewsChannel).collect(Collectors.toList());
	}
	
	public default GraphiteGuildMessageChannel getGuildMessageChannel(GuildMessageChannel channel) {
		return (GraphiteGuildMessageChannel) getGuildChannel((GuildMessageChannel) channel);
	}
	
	public default GraphiteGuildMessageChannel getGuildMessageChannelByID(String id) {
		GuildChannel ch = getJDAGuild().getGuildChannelById(id);
		if(!(ch instanceof GuildMessageChannel)) return null;
		return getGuildMessageChannel((GuildMessageChannel) ch);
	}
	
	public default GraphiteVoiceChannel getVoiceChannel(VoiceChannel channel) {
		return (GraphiteVoiceChannel) getGuildChannel(channel);
	}
	
	public default GraphiteVoiceChannel getVoiceChannelByID(String id) {
		return getVoiceChannel(getJDAGuild().getVoiceChannelById(id));
	}
	
	public default List<GraphiteVoiceChannel> getVoiceChannels() {
		return getJDAGuild().getVoiceChannels().stream().map(this::getVoiceChannel).collect(Collectors.toList());
	}
	
	public default List<GraphiteVoiceChannel> getVoiceChannelsByName(String name, boolean ignoreCase) {
		return getJDAGuild().getVoiceChannelsByName(name, ignoreCase).stream().map(this::getVoiceChannel).collect(Collectors.toList());
	}
	
	public default GraphiteStageChannel getStageChannel(StageChannel channel) {
		return (GraphiteStageChannel) getGuildChannel(channel);
	}
	
	public default GraphiteStageChannel getStageChannelByID(String id) {
		return getStageChannel(getJDAGuild().getStageChannelById(id));
	}
	
	public default List<GraphiteStageChannel> getStageChannels() {
		return getJDAGuild().getStageChannels().stream().map(this::getStageChannel).collect(Collectors.toList());
	}
	
	public default List<GraphiteStageChannel> getStageChannelsByName(String name, boolean ignoreCase) {
		return getJDAGuild().getStageChannelsByName(name, ignoreCase).stream().map(this::getStageChannel).collect(Collectors.toList());
	}
	
	public default GraphiteAudioChannel getAudioChannel(AudioChannel channel) {
		return (GraphiteAudioChannel) getGuildChannel((AudioChannel) channel);
	}
	
	public default GraphiteAudioChannel getAudioChannelByID(String id) {
		GuildChannel ch = getJDAGuild().getGuildChannelById(id);
		if(!(ch instanceof AudioChannel)) return null;
		return getAudioChannel((AudioChannel) ch);
	}
	
	public default GraphiteCategory getCategory(Category category) {
		return (GraphiteCategory) getGuildChannel(category);
	}
	
	public default GraphiteCategory getCategoryByID(String id) {
		return getCategory(getJDAGuild().getCategoryById(id));
	}
	
	public default List<GraphiteCategory> getCategories() {
		return getJDAGuild().getCategories().stream().map(m -> getCategory(m)).collect(Collectors.toList());
	}
	
	public default List<GraphiteCategory> getCategoriesByName(String name, boolean ignoreCase) {
		return getJDAGuild().getCategoriesByName(name, ignoreCase).stream().map(this::getCategory).collect(Collectors.toList());
	}

}

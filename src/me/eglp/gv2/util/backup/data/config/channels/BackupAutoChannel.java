package me.eglp.gv2.util.backup.data.config.channels;

import me.eglp.gv2.util.backup.IDMappings;
import me.eglp.gv2.util.base.guild.GraphiteCategory;
import me.eglp.gv2.util.base.guild.GraphiteGuild;
import me.eglp.gv2.util.base.guild.GraphiteVoiceChannel;
import me.eglp.gv2.util.base.guild.GuildAutoChannel;
import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONConvertible;
import me.mrletsplay.mrcore.json.converter.JSONValue;

public class BackupAutoChannel implements JSONConvertible {

	@JSONValue
	private String categoryID;
	
	@JSONValue
	private String channelID;
	
	@JSONConstructor
	private BackupAutoChannel() {}
	
	public BackupAutoChannel(GuildAutoChannel autoChannel) {
		this.categoryID = autoChannel.getCategory() == null ? null : autoChannel.getCategory().getID();
		this.channelID = autoChannel.getChannel().getID();
	}
	
	public String getCategoryID() {
		return categoryID;
	}
	
	public String getChannelID() {
		return channelID;
	}

	public GuildAutoChannel restore(GraphiteGuild guild, IDMappings mappings) {
		String newChannelID = mappings.getNewID(channelID);
		GraphiteVoiceChannel c = guild.getVoiceChannelByID(newChannelID);
		if(c == null) return null;
		
		String newCategoryID = mappings.getNewID(categoryID);
		GraphiteCategory cat = guild.getCategoryByID(newCategoryID);
		if(cat == null) return null;
		
		return new GuildAutoChannel(c, cat);
	}
	
}

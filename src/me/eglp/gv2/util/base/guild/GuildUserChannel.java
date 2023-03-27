package me.eglp.gv2.util.base.guild;

import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONConvertible;

public class GuildUserChannel implements JSONConvertible {
	
	private GraphiteGuild guild;
	private GraphiteMember owner;
	private GraphiteVoiceChannel channel;
	
	@JSONConstructor
	private GuildUserChannel() {}
	
	public GuildUserChannel(GraphiteGuild guild, GraphiteMember owner, GraphiteVoiceChannel channel) {
		this.guild = guild;
		this.owner = owner;
		this.channel = channel;
	}
	
	public void setGuild(GraphiteGuild guild) {
		this.guild = guild;
	}
	
	public GraphiteGuild getGuild() {
		return guild;
	}
	
	public GraphiteMember getOwner() {
		return owner;
	}
	
	public GraphiteVoiceChannel getChannel() {
		return channel;
	}
	
	public void delete() {
		guild.getChannelsConfig().deleteUserChannel(this);
	}
	
	public boolean isValid() {
		return channel != null && owner != null;
	}
	
	@Override
	public void preSerialize(JSONObject object) {
		object.put("guild", guild.getID());
		if(owner != null) object.put("owner", owner.getID());
		if(channel != null) object.put("channel", channel.getID());
	}
	
	@Override
	public boolean equals(Object o) {
		if(!(o instanceof GuildUserChannel)) return false;
		GuildUserChannel u = (GuildUserChannel) o;
		return channel.equals(u.channel);
	}

}

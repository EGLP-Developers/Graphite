package me.eglp.gv2.util.scripting.object;

import net.dv8tion.jda.api.entities.channel.ChannelType;

public class JSChannelType {

	protected ChannelType type;
	
	public JSChannelType(ChannelType type) {
		this.type = type;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof JSChannelType)) return false;
		return type.equals(((JSChannelType) obj).type);
	}
	
	@Override
	public String toString() {
		return "[JS Channel Type: " + type.name() + "]";
	}
	
}

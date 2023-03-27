package me.eglp.gv2.util.mention;

import net.dv8tion.jda.api.entities.channel.ChannelType;

public enum MentionType {

	USER,
	ROLE,
	TEXT_CHANNEL,
	EVERYONE,
	HERE,
	EMOTE,
	
	VOICE_CHANNEL,
	NEWS_CHANNEL,
	STAGE_CHANNEL,
	CATEGORY,
	;
	
	public static MentionType getMentionType(ChannelType channelType) {
		switch(channelType) {
			case CATEGORY:
				return CATEGORY;
			case TEXT:
				return TEXT_CHANNEL;
			case VOICE:
				return VOICE_CHANNEL;
			case GROUP:
			case GUILD_NEWS_THREAD:
			case GUILD_PRIVATE_THREAD:
			case GUILD_PUBLIC_THREAD:
			case NEWS:
				return NEWS_CHANNEL;
			case PRIVATE:
			case STAGE:
				return STAGE_CHANNEL;
			case UNKNOWN:
			default:
				throw new IllegalArgumentException("Channel type has no mention type");
		}
	}
	
}

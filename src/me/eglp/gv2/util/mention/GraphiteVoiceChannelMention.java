package me.eglp.gv2.util.mention;

import me.eglp.gv2.util.base.guild.GraphiteVoiceChannel;

public class GraphiteVoiceChannelMention extends GraphiteMention {

	private GraphiteVoiceChannel mentionedVoiceChannel;
	
	public GraphiteVoiceChannelMention(GraphiteVoiceChannel channel) {
		super(MentionType.VOICE_CHANNEL);
		this.mentionedVoiceChannel = channel;
	}
	
	public GraphiteVoiceChannel getMentionedVoiceChannel() {
		if(!isValid()) throw new IllegalStateException("Mention is invalid");
		return mentionedVoiceChannel;
	}
	
	@Override
	public boolean isAmbiguous() {
		return false;
	}
	
	@Override
	public boolean isValid() {
		return mentionedVoiceChannel != null;
	}

}

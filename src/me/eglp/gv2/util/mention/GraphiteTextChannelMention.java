package me.eglp.gv2.util.mention;

import me.eglp.gv2.util.base.guild.GraphiteTextChannel;

public class GraphiteTextChannelMention extends GraphiteMention {

	private GraphiteTextChannel mentionedTextChannel;
	
	public GraphiteTextChannelMention(GraphiteTextChannel channel) {
		super(MentionType.TEXT_CHANNEL);
		this.mentionedTextChannel = channel;
	}
	
	public GraphiteTextChannel getMentionedTextChannel() {
		if(!isValid()) throw new IllegalStateException("Mention is invalid");
		return mentionedTextChannel;
	}
	
	@Override
	public boolean isAmbiguous() {
		return false;
	}
	
	@Override
	public boolean isValid() {
		return mentionedTextChannel != null;
	}

}

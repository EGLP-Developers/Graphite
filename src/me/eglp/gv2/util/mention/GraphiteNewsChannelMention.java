package me.eglp.gv2.util.mention;

import me.eglp.gv2.util.base.guild.GraphiteNewsChannel;

public class GraphiteNewsChannelMention extends GraphiteMention{

	private GraphiteNewsChannel mentionedNewsChannel;
	
	public GraphiteNewsChannelMention(GraphiteNewsChannel channel) {
		super(MentionType.NEWS_CHANNEL);
		this.mentionedNewsChannel = channel;
	}
	
	public GraphiteNewsChannel getMentionedNewsChannel() {
		if(!isValid()) throw new IllegalStateException("Mention is invalid");
		return mentionedNewsChannel;
	}
	
	@Override
	public boolean isAmbiguous() {
		return false;
	}
	
	@Override
	public boolean isValid() {
		return mentionedNewsChannel != null;
	}

}

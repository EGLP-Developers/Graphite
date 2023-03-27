package me.eglp.gv2.util.mention;

import me.eglp.gv2.util.base.guild.GraphiteStageChannel;

public class GraphiteStageChannelMention extends GraphiteMention{

	private GraphiteStageChannel mentionedStageChannel;
	
	public GraphiteStageChannelMention(GraphiteStageChannel channel) {
		super(MentionType.STAGE_CHANNEL);
		this.mentionedStageChannel = channel;
	}
	
	public GraphiteStageChannel getMentionedStageChannel() {
		if(!isValid()) throw new IllegalStateException("Mention is invalid");
		return mentionedStageChannel;
	}
	
	@Override
	public boolean isAmbiguous() {
		return false;
	}
	
	@Override
	public boolean isValid() {
		return mentionedStageChannel != null;
	}

}

package me.eglp.gv2.util.scripting.object;

import me.eglp.gv2.util.mention.GraphiteTextChannelMention;
import me.eglp.gv2.util.scripting.ScriptExecutionException;

public class JSTextChannelMention extends JSMention {

	private GraphiteTextChannelMention mention;
	
	public JSTextChannelMention(GraphiteTextChannelMention mention) {
		super(mention);
		this.mention = mention;
	}
	
	/**
	 * Returns the text channel that was mentioned
	 * @return The text channel that was mentioned
	 * @throws ScriptExecutionException if this mention is not {@link #isValid() valid}
	 */
	public JSTextChannel getTextChannel() throws ScriptExecutionException {
		if(!isValid()) notValid();
		return new JSTextChannel(mention.getMentionedTextChannel());
	}
	
	@Override
	public String toString() {
		return "[JS Text Channel Mention]";
	}

}

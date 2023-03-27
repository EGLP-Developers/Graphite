package me.eglp.gv2.util.scripting.object;

import me.eglp.gv2.util.mention.GraphiteVoiceChannelMention;
import me.eglp.gv2.util.scripting.ScriptExecutionException;

public class JSVoiceChannelMention extends JSMention {

	private GraphiteVoiceChannelMention mention;
	
	public JSVoiceChannelMention(GraphiteVoiceChannelMention mention) {
		super(mention);
		this.mention = mention;
	}
	
	/**
	 * Returns the voice channel that was mentioned
	 * @return The voice channel that was mentioned
	 * @throws ScriptExecutionException if this mention is not {@link #isValid() valid} or if it is {@link #isAmbiguous() ambiguous}
	 */
	public JSVoiceChannel getVoiceChannel() throws ScriptExecutionException {
		return new JSVoiceChannel(mention.getMentionedVoiceChannel());
	}
	
	@Override
	public String toString() {
		return "[JS Voice Channel Mention]";
	}

}

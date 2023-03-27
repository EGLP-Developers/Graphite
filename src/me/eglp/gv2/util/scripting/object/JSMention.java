package me.eglp.gv2.util.scripting.object;

import me.eglp.gv2.util.mention.GraphiteMention;
import me.eglp.gv2.util.mention.MentionType;
import me.eglp.gv2.util.scripting.ScriptExecutionException;

public class JSMention {

	private GraphiteMention mention;

	public JSMention(GraphiteMention mention) {
		this.mention = mention;
	}
	
	/**
	 * Returns whether this mention is valid<br>
	 * An invalid mention is a mention of the correct format, but with an invalid id/name
	 * @return Whether this mention is valid
	 */
	public boolean isValid() {
		return mention.isValid();
	}
	
	/**
	 * Returns the type of this mention
	 * @return The type of this mention
	 * @see JSVars
	 */
	public JSMentionType getType() {
		return new JSMentionType(mention.getType());
	}
	
	/**
	 * Returns this mention as a user mention<br>
	 * This method will throw an error if this is not a user mention
	 * @return This mention as a user mention
	 * @throws ScriptExecutionException If this is not a user mention
	 */
	public JSUserMention asUser() {
		if(!mention.isOfType(MentionType.USER)) wrongType("user");
		return new JSUserMention(mention.asUserMention());
	}
	
	/**
	 * Returns this mention as a role mention<br>
	 * This method will throw an error if this is not a role mention
	 * @return This mention as a role mention
	 * @throws ScriptExecutionException If this is not a role mention
	 */
	public JSRoleMention asRole() {
		if(!mention.isOfType(MentionType.ROLE)) wrongType("role");
		return new JSRoleMention(mention.asRoleMention());
	}
	
	/**
	 * Returns this mention as a text channel mention<br>
	 * This method will throw an error if this is not a text channel mention
	 * @return This mention as a text channel mention
	 * @throws ScriptExecutionException If this is not a text channel mention
	 */
	public JSTextChannelMention asTextChannel() {
		if(!mention.isOfType(MentionType.TEXT_CHANNEL)) wrongType("text channel");
		return new JSTextChannelMention(mention.asTextChannelMention());
	}
	
	/**
	 * Returns this mention as a voice channel mention<br>
	 * This method will throw an error if this is not a voice channel mention
	 * @return This mention as a voice channel mention
	 * @throws ScriptExecutionException If this is not a voice channel mention
	 */
	public JSVoiceChannelMention asVoiceChannel() {
		if(!mention.isOfType(MentionType.VOICE_CHANNEL)) wrongType("voice channel");
		return new JSVoiceChannelMention(mention.asVoiceChannelMention());
	}
	
	protected void notValid() {
		throw new ScriptExecutionException("Mention is invalid");
	}
	
	private void wrongType(String requiredType) {
		throw new ScriptExecutionException("Mention needs to be of type " + requiredType);
	}
	
	@Override
	public String toString() {
		return "[JS Mention]";
	}
	
}

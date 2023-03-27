package me.eglp.gv2.util.scripting.object;

import org.mozilla.javascript.Scriptable;

import me.eglp.gv2.util.mention.GraphiteUserMention;
import me.eglp.gv2.util.scripting.GraphiteScript;
import me.eglp.gv2.util.scripting.ScriptExecutionException;

public class JSUserMention extends JSMention {

	private GraphiteUserMention mention;
	
	public JSUserMention(GraphiteUserMention mention) {
		super(mention);
		this.mention = mention;
	}
	
	/**
	 * Returns whether this mention is ambiguous<br>
	 * This is only the case when using the {@code @<username>} format and there are multiple members with the same name on the guild
	 * @return Whether this mention is ambiguous
	 */
	public boolean isAmbiguous() {
		return mention.isAmbiguous();
	}
	
	/**
	 * Returns an array of all users that could be meant by this mention<br>
	 * <br>
	 * This array will contain either no elements if the mention is not {@link #isValid() valid},<br>
	 * one element if the mention is {@link #isValid() valid} and not {@link #isAmbiguous() ambiguous} or<br>
	 * more than one element if the mention is {@link #isAmbiguous() ambiguous}
	 * @return An array of all users that could be meant by this mention<br>
	 * @see JSUser
	 */
	public Scriptable getPossibleUsers() {
		return GraphiteScript.createJSArray(mention.getPossibleUsers().stream().map(JSUser::new).toArray(JSUser[]::new));
	}
	
	/**
	 * Returns the user that was mentioned
	 * @return The user that was mentioned
	 * @throws ScriptExecutionException if this mention is not {@link #isValid() valid} or if it is {@link #isAmbiguous() ambiguous}
	 */
	public JSUser getUser() throws ScriptExecutionException {
		if(!isValid()) notValid();
		if(isAmbiguous()) throw new ScriptExecutionException("User mention is ambiguous");
		return new JSUser(mention.getMentionedUser());
	}
	
	@Override
	public String toString() {
		return "[JS User Mention]";
	}

}

package me.eglp.gv2.util.scripting.object;

import me.eglp.gv2.util.mention.GraphiteRoleMention;
import me.eglp.gv2.util.scripting.ScriptExecutionException;

public class JSRoleMention extends JSMention {

	private GraphiteRoleMention mention;
	
	public JSRoleMention(GraphiteRoleMention mention) {
		super(mention);
		this.mention = mention;
	}
	
	/**
	 * Returns the role that was mentioned
	 * @return The role that was mentioned
	 * @throws ScriptExecutionException if this mention is not {@link #isValid() valid}
	 */
	public JSRole getRole() throws ScriptExecutionException {
		if(!isValid()) notValid();
		return new JSRole(mention.getMentionedRole());
	}
	
	@Override
	public String toString() {
		return "[JS Role Mention]";
	}

}

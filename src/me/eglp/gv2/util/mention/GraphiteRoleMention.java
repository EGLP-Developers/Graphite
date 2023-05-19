package me.eglp.gv2.util.mention;

import me.eglp.gv2.guild.GraphiteRole;

public class GraphiteRoleMention extends GraphiteMention {

	private GraphiteRole mentionedRole;

	public GraphiteRoleMention(GraphiteRole role) {
		super(MentionType.ROLE);
		this.mentionedRole = role;
	}

	public GraphiteRole getMentionedRole() {
		if(!isValid()) throw new IllegalStateException("Mention is invalid");
		return mentionedRole;
	}

	@Override
	public boolean isAmbiguous() {
		return false;
	}

	@Override
	public boolean isValid() {
		return mentionedRole != null;
	}

}

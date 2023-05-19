package me.eglp.gv2.util.mention;

import java.util.Collections;
import java.util.List;

import me.eglp.gv2.user.GraphiteUser;

public class GraphiteUserMention extends GraphiteMention {

	private List<GraphiteUser> possibleUsers;

	public GraphiteUserMention(List<GraphiteUser> users) {
		super(MentionType.USER);
		this.possibleUsers = users;
	}

	public GraphiteUserMention(GraphiteUser user) {
		super(MentionType.USER);
		this.possibleUsers = user != null ? Collections.singletonList(user) : null;
	}

	public List<GraphiteUser> getPossibleUsers() {
		return possibleUsers;
	}

	public boolean isAmbiguous() {
		return possibleUsers.size() != 1;
	}

	public GraphiteUser getMentionedUser() {
		if(!isValid() || isAmbiguous()) throw new IllegalStateException("Mention is ambiguous/invalid");
		return possibleUsers.get(0);
	}

	@Override
	public boolean isValid() {
		return possibleUsers != null && !possibleUsers.isEmpty();
	}

}

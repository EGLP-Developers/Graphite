package me.eglp.gv2.util.scripting.object;

import me.eglp.gv2.util.mention.MentionType;

public class JSMentionType {

	private MentionType type;
	
	public JSMentionType(MentionType type) {
		this.type = type;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof JSMentionType)) return false;
		return type.equals(((JSMentionType) obj).type);
	}
	
	@Override
	public String toString() {
		return "[JS Mention Type: " + type.name() + "]";
	}
	
}

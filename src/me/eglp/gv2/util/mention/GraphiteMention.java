package me.eglp.gv2.util.mention;

public abstract class GraphiteMention {

	private MentionType type;
	
	public GraphiteMention(MentionType type) {
		this.type = type;
	}
	
	public boolean isOfType(MentionType type) {
		return this.type.equals(type);
	}
	
	public MentionType getType() {
		return type;
	}
	
	public GraphiteEveryoneMention asEveryoneMention() {
		return this instanceof GraphiteEveryoneMention ? (GraphiteEveryoneMention) this : null;
	}
	
	public GraphiteHereMention asHereMention() {
		return this instanceof GraphiteHereMention ? (GraphiteHereMention) this : null;
	}
	
	public GraphiteRoleMention asRoleMention() {
		return this instanceof GraphiteRoleMention ? (GraphiteRoleMention) this : null;
	}
	
	public GraphiteTextChannelMention asTextChannelMention() {
		return this instanceof GraphiteTextChannelMention ? (GraphiteTextChannelMention) this : null;
	}
	
	public GraphiteNewsChannelMention asNewsChannelMention() {
		return this instanceof GraphiteNewsChannelMention ? (GraphiteNewsChannelMention) this : null;
	}
	
	public GraphiteUserMention asUserMention() {
		return this instanceof GraphiteUserMention ? (GraphiteUserMention) this : null;
	}
	
	public GraphiteVoiceChannelMention asVoiceChannelMention() {
		return this instanceof GraphiteVoiceChannelMention ? (GraphiteVoiceChannelMention) this : null;
	}
	
	public GraphiteStageChannelMention asStageChannelMention() {
		return this instanceof GraphiteStageChannelMention ? (GraphiteStageChannelMention) this : null;
	}
	
	public GraphiteCategoryMention asCategoryMention() {
		return this instanceof GraphiteCategoryMention ? (GraphiteCategoryMention) this : null;
	}
	
	public GraphiteEmoteMention asEmoteMention() {
		return this instanceof GraphiteEmoteMention ? (GraphiteEmoteMention) this : null;
	}
	
	public abstract boolean isValid();
	
	public abstract boolean isAmbiguous();
	
}

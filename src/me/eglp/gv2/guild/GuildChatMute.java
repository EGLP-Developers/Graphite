package me.eglp.gv2.guild;

public class GuildChatMute {

	private GraphiteMember member;
	private long expiresAt;

	public GuildChatMute(GraphiteMember member, long expiresAt) {
		this.member = member;
		this.expiresAt = expiresAt;
	}

	public GuildChatMute(GraphiteMember member) {
		this(member, -1);
	}

	public GraphiteMember getMember() {
		return member;
	}

	public boolean isExpired() {
		return System.currentTimeMillis() >= getExpirationTime();
	}

	public void remove(GraphiteMember member, String reason) {
		member.getGuild().getModerationConfig().removeChatMute(this, member, reason);
	}

	public boolean isTemporary() {
		return expiresAt != -1;
	}

	public long getExpirationTime() {
		return expiresAt == -1 ? Long.MAX_VALUE : expiresAt;
	}

	public long getExpirationTimeRaw() {
		return expiresAt;
	}

}

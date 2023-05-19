package me.eglp.gv2.guild;

import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONValue;

public class GuildJail {

	private GraphiteMember member;
	private GraphiteAudioChannel channel;

	@JSONValue
	private int leaveAttempts;

	@JSONValue
	private long expiresAt;

	@JSONConstructor
	private GuildJail() {}

	public GuildJail(GraphiteMember member, GraphiteAudioChannel channel, int leaveAttempts, long expiresAt) {
		this.member = member;
		this.channel = channel;
		this.leaveAttempts = leaveAttempts;
		this.expiresAt = expiresAt;
	}

	public GuildJail(GraphiteMember member, GraphiteAudioChannel channel) {
		this(member, channel, 0, -1);
	}

	public GraphiteMember getMember() {
		return member;
	}

	public GraphiteAudioChannel getChannel() {
		return channel;
	}

	public boolean isExpired() {
		return System.currentTimeMillis() >= getExpirationTime();
	}

	public void remove(GraphiteMember moderator, String reason) {
		member.getGuild().getModerationConfig().removeJail(this, moderator, reason);
	}

	public boolean isTemporary() {
		return expiresAt != -1;
	}

	public void addLeaveAttempt() {
		member.getGuild().getModerationConfig().addLeaveAttemptToJail(this);
		this.leaveAttempts++;
	}

	public int getLeaveAttempts() {
		return leaveAttempts;
	}

	public long getExpirationTime() {
		return expiresAt == -1 ? Long.MAX_VALUE : expiresAt;
	}

	public long getExpirationTimeRaw() {
		return expiresAt;
	}

}

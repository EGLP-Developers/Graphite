package me.eglp.gv2.util.backup.data.messages;

import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONConvertible;
import me.mrletsplay.mrcore.json.converter.JSONValue;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;

public class BackupThread implements JSONConvertible {

	@JSONValue
	private String id;

	@JSONValue
	private String name;

	@JSONValue
	private String autoArchiveDuration;

	@JSONValue
	private boolean invitable;

	@JSONValue
	private boolean archived;

	@JSONValue
	private boolean locked;

	@JSONValue
	private int slowmode;

	@JSONConstructor
	private BackupThread() {}

	public BackupThread(ThreadChannel channel) {
		this.id = channel.getId();
		this.name = channel.getName();
		this.autoArchiveDuration = channel.getAutoArchiveDuration().name();
		this.invitable = channel.getType() == ChannelType.GUILD_PRIVATE_THREAD ? channel.isInvitable() : false;
		this.archived = channel.isArchived();
		this.locked = channel.isLocked();
		this.slowmode = channel.getSlowmode();
	}

	public String getID() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getAutoArchiveDuration() {
		return autoArchiveDuration;
	}

	public boolean isInvitable() {
		return invitable;
	}

	public boolean isArchived() {
		return archived;
	}

	public boolean isLocked() {
		return locked;
	}

	public int getSlowmode() {
		return slowmode;
	}

}

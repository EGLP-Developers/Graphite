package me.eglp.gv2.util.scripting.object;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.stream.Collectors;

import org.mozilla.javascript.Scriptable;

import me.eglp.gv2.util.backup.GuildBackup;
import me.eglp.gv2.util.base.guild.GraphiteGuild;
import me.eglp.gv2.util.base.guild.GraphiteMember;
import me.eglp.gv2.util.base.guild.GraphiteRole;
import me.eglp.gv2.util.base.guild.GraphiteTextChannel;
import me.eglp.gv2.util.base.guild.GraphiteVoiceChannel;
import me.eglp.gv2.util.queue.GraphiteQueue;
import me.eglp.gv2.util.queue.GraphiteTaskInfo;
import me.eglp.gv2.util.scripting.GraphiteScript;
import me.eglp.gv2.util.scripting.ScriptExecutionException;

public class JSGuild {

	protected GraphiteGuild guild;

	public JSGuild(GraphiteGuild guild) {
		this.guild = guild;
	}

	/**
	 * Returns the name of this guild
	 * @return The name of this guild
	 */
	public String getName() {
		return guild.getName();
	}

	/**
	 * Returns the Discord id of this guild
	 * @return The Discord id of this guild
	 */
	public String getID() {
		return guild.getID();
	}

	/**
	 * Creates a backup on the specified guild
	 * @return The created backup or {@code null} if the backup failed (e.g. the maximum amount of backups has been reached)
	 * @see JSPremiumLevel#getMaxBackups()
	 * @see #restoreBackup(JSBackup, boolean, JSRestoreSelector...)
	 */
	public JSBackup createBackup() {
		if(!guild.canCreateBackup()) return null;
		GuildBackup b = GuildBackup.createNew(guild, null, 0, false); // JS Backups don't have messages
		return b == null ? null : new JSBackup(b);
	}

	/**
	 * Restores a backup on the specified guild
	 * @param backup The backup to restore
	 * @param parameters What should be restored
	 * @see #createBackup(int)
	 * @see JSVars
	 */
	public void restoreBackup(JSBackup backup, JSRestoreSelector... parameters) {
		if(guild.getBackupCooldown() > 0) throw new ScriptExecutionException("Backups are on cooldown");
		GraphiteQueue q = guild.getResponsibleQueue();
		q.queueHeavy(
			guild,
			new GraphiteTaskInfo(GuildBackup.TASK_ID, "Restoring backup (Script)"),
			() -> backup.backup.restore(guild, null, EnumSet.copyOf(Arrays.stream(parameters) // JS Backups don't have messages
				.map(p -> p.type)
				.collect(Collectors.toList())))
		).exceptionally(ex -> -1L);
	}

	/**
	 * Returns the amount of time (in milliseconds) until another backup can be restored
	 * @return The amount of time until another backup can be restored
	 */
	public long getBackupCooldown() {
		return guild.getBackupCooldown();
	}

	/**
	 * Returns all of the backups this guild currently has
	 * @return All of the backups this guild currently has
	 * @see JSBackup
	 */
	public Scriptable getBackups() {
		return GraphiteScript.createJSArray(guild.getBackups().stream().map(JSBackup::new).toArray(JSBackup[]::new));
	}

	/**
	 * Returns the backup identified by the given name or {@code null} if the backup doesn't exist
	 * @param name The name of the backup
	 * @return The backup with that id
	 */
	public JSBackup getBackupByName(String name) {
		GuildBackup b = guild.getBackupByName(name);
		return b == null ? null : new JSBackup(b);
	}

	/**
	 * Returns the permission manager for this guild
	 * @return The permission manager for this guild
	 */
	public JSGuildPermissionManager getPermissionManager() {
		return new JSGuildPermissionManager(guild.getPermissionManager());
	}

	/**
	 * Returns an array of all members on this guild
	 * @return An array of all members on this guild
	 * @see JSMember
	 */
	public Scriptable getMembers() {
		return GraphiteScript.createJSArray(guild.getMembers().stream().map(JSMember::new).toArray(JSMember[]::new));
	}

	/**
	 * Returns the member instance for a user or {@code null} if the user isn't part of this guild
	 * @param user The user to get the member instance for
	 * @return The member instance
	 */
	public JSMember getMember(JSUser user) {
		GraphiteMember m = guild.getMember(user.user);
		return m == null ? null : new JSMember(m);
	}

	/**
	 * Returns an array of all roles on this guild
	 * @return An array of all roles on this guild
	 * @see JSRole
	 */
	public Scriptable getRoles() {
		return GraphiteScript.createJSArray(guild.getRoles().stream().map(JSRole::new).toArray(JSRole[]::new));
	}

	/**
	 * Returns an array of all roles on this guild which have the provided name
	 * @param name The name to search for
	 * @param ignoreCase Whether case should be ignored
	 * @return An array of all roles which have the provided name
	 * @see JSRole
	 */
	public Scriptable getRolesByName(String name, boolean ignoreCase) {
		return GraphiteScript.createJSArray(guild.getJDAGuild().getRolesByName(name, ignoreCase).stream()
				.map(guild::getRole)
				.map(JSRole::new)
				.toArray(JSRole[]::new));
	}

	/**
	 * Returns a role by its id
	 * @param id The id of the role to get
	 * @return The role with the specified id, or null if no such role exists
	 * @see JSRole#getID()
	 */
	public JSRole getRole(String id) {
		GraphiteRole r = guild.getRoleByID(id);
		return r == null ? null : new JSRole(r);
	}

	/**
	 * Returns an array of all voice channels in this guild
	 * @return An array of all voice channels in this guild
	 * @see JSVoiceChannel
	 */
	public Scriptable getVoiceChannels() {
		return GraphiteScript.createJSArray(guild.getVoiceChannels().stream().map(JSVoiceChannel::new).toArray(JSVoiceChannel[]::new));
	}

	/**
	 * Returns an array of all voice channels on this guild which have the provided name
	 * @param name The name to search for
	 * @param ignoreCase Whether case should be ignored
	 * @return An array of all voice channels which have the provided name
	 */
	public Scriptable getVoiceChannelsByName(String name, boolean ignoreCase) {
		return GraphiteScript.createJSArray(guild.getVoiceChannelsByName(name, ignoreCase).stream()
				.map(JSVoiceChannel::new)
				.toArray(JSVoiceChannel[]::new));
	}

	/**
	 * Returns a voice channel by its id
	 * @param id The id of the voice channel to get
	 * @return The voice channel with the specified id, or null if no such voice channel exists
	 * @see JSVoiceChannel#getID()
	 */
	public JSVoiceChannel getVoiceChannel(String id) {
		GraphiteVoiceChannel r = guild.getVoiceChannelByID(id);
		return r == null ? null : new JSVoiceChannel(r);
	}

	/**
	 * Returns an array of all text channels in this guild
	 * @return An array of all text channels in this guild
	 * @see JSTextChannel
	 */
	public Scriptable getTextChannels() {
		return GraphiteScript.createJSArray(guild.getTextChannels().stream().map(JSTextChannel::new).toArray(JSTextChannel[]::new));
	}

	/**
	 * Returns an array of all text channels on this guild which have the provided name
	 * @param name The name to search for
	 * @param ignoreCase Whether case should be ignored
	 * @return An array of all text channels which have the provided name
	 */
	public Scriptable getTextChannelsByName(String name, boolean ignoreCase) {
		return GraphiteScript.createJSArray(guild.getJDAGuild().getTextChannelsByName(name, ignoreCase).stream()
				.map(guild::getTextChannel)
				.map(JSTextChannel::new)
				.toArray(JSTextChannel[]::new));
	}

	/**
	 * Returns a text channel by its id
	 * @param id The id of the text channel to get
	 * @return The text channel with the specified id, or null if no such text channel exists
	 * @see JSTextChannel#getID()
	 */
	public JSTextChannel getTextChannel(String id) {
		GraphiteTextChannel r = guild.getTextChannelByID(id);
		return r == null ? null : new JSTextChannel(r);
	}

	/**
	 * Returns an array of all (normal) reports that were created on this guild
	 * @return An array of all (normal) reports that were created on this guild
	 * @see JSReport
	 */
	public Scriptable getReports() {
		return GraphiteScript.createJSArray(guild.getReportsConfig().getReports().stream().map(JSReport::new).toArray(JSReport[]::new));
	}

	@Override
	public String toString() {
		return "[JS Guild: " + guild.getID() + "]";
	}

}

package me.eglp.gv2.util.scripting.object;

import org.mozilla.javascript.Scriptable;

import me.eglp.gv2.guild.GraphiteMember;
import me.eglp.gv2.util.scripting.GraphiteScript;

public class JSMember {

	protected GraphiteMember member;

	public JSMember(GraphiteMember member) {
		this.member = member;
	}

	/**
	 * Returns the guild this member is a part of
	 * @return The guild this member is a part of
	 */
	public JSGuild getGuild() {
		return new JSGuild(member.getGuild());
	}

	/**
	 * Returns the {@link JSUser} associated with this member
	 * @return The {@link JSUser} associated with this member
	 */
	public JSUser getUser() {
		return new JSUser(member);
	}

	/**
	 * Returns an array of all roles this member has
	 * @return An array of all roles this member has
	 * @see JSRole
	 */
	public Scriptable getRoles() {
		return GraphiteScript.createJSArray(member.getRoles().stream().map(JSRole::new).toArray(JSRole[]::new));
	}

	/**
	 * Returns whether the member has the specified role on the guild
	 * @param role The role to check
	 * @return Whether the member has that role
	 */
	public boolean hasRole(JSRole role) {
		return member.getRoles().stream().anyMatch(r -> r.equals(role.role));
	}

	/**
	 * Adds a role to the member
	 * @param role The role to add
	 */
	public void addRoleToMember(JSRole role) {
		member.getGuild().addRoleToMember(member, role.role);
	}

	/**
	 * Removes a role from the member
	 * @param role The role to remove
	 */
	public void removeRole(JSRole role) {
		member.getGuild().getJDAGuild().removeRoleFromMember(member.getJDAMember(), role.role.getJDARole());
	}

	@Override
	public String toString() {
		return "[JS Member: " + getUser().getID() + "]";
	}

}

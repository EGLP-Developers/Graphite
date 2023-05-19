package me.eglp.gv2.util.scripting.object;

import me.eglp.gv2.guild.GraphiteRole;

public class JSRole {

	protected GraphiteRole role;

	public JSRole(GraphiteRole role) {
		this.role = role;
	}

	/**
	 * Returns the guild this role was created on
	 * @return The guild this role was created on
	 */
	public JSGuild getGuild() {
		return new JSGuild(role.getGuild());
	}

	/**
	 * Returns the name of this role
	 * @return The name of this role
	 */
	public String getName() {
		return role.getName();
	}

	/**
	 * Returns the raw color of this role
	 * @return The raw color of this role
	 */
	public int getColorRaw() {
		return role.getJDARole().getColorRaw();
	}

	/**
	 * Returns the id of this role
	 * @return The id of this role
	 * @see GraphiteID
	 */
	public String getID() {
		return role.getID();
	}

	@Override
	public String toString() {
		return "[JS Role: " + getID() + "]";
	}

}

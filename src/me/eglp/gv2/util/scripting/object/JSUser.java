package me.eglp.gv2.util.scripting.object;

import me.eglp.gv2.user.GraphiteUser;

public class JSUser {

	protected GraphiteUser user;

	public JSUser(GraphiteUser user) {
		this.user = user;
	}

	/**
	 * Returns this user's discord id (not to be confused with {@link JSUser#getDiscriminator() the user's discriminator})
	 * @return This user's discord id
	 */
	public String getID() {
		return user.getID();
	}

	/**
	 * Returns the username of this user
	 * @return The username of this user
	 */
	public String getName() {
		return user.getName();
	}

	/**
	 * Returns this user's discriminator (the part behind the username, e.g. MrLetsplay#<b>6865</b>
	 * @return Thus user's discriminator
	 */
	@Deprecated
	public String getDiscriminator() {
		return user.getDiscriminator();
	}

	/**
	 * Returns this user's full name (username + "#" + discriminator)
	 * @return This user's full name
	 * @see #getName()
	 * @see #getDiscriminator()
	 */
	public String getFullName() {
		return getName() + "#" + getDiscriminator();
	}

	/**
	 * Returns the private channel with this user
	 * @return the private channel with this user
	 */
	public JSMessageChannel openPrivateChannel() {
		return new JSPrivateChannel(user.openPrivateChannel());
	}

	@Override
	public String toString() {
		return "[JS User: " + getID() + "]";
	}

}

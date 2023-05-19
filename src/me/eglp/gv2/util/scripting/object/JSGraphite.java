package me.eglp.gv2.util.scripting.object;

import me.eglp.gv2.main.Graphite;

public class JSGraphite {

	/**
	 * The Graphite instance stored in the {@code graphite} variable
	 */
	public static final JSGraphite INSTANCE = new JSGraphite();

	/**
	 * Returns Graphite's username
	 * @return Graphite's username
	 */
	public String getName() {
		return Graphite.getBotInfo().getName();
	}

	@Override
	public String toString() {
		return "[JS Graphite]";
	}

}

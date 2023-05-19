package me.eglp.gv2.util.scripting.object;

import me.eglp.gv2.user.GraphitePrivateChannel;

public class JSPrivateChannel extends JSMessageChannel {

	private GraphitePrivateChannel channel;

	public JSPrivateChannel(GraphitePrivateChannel channel) {
		super(channel);
		this.channel = channel;
	}

	/**
	 * Returns the user of this private channel
	 * @return The user of this private channel
	 */
	public JSUser getUser() {
		return new JSUser(channel.getUser());
	}

	@Override
	public String toString() {
		return "[JS Private Channel]";
	}

}

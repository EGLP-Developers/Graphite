package me.eglp.gv2.util.scripting.object;

import me.eglp.gv2.guild.GraphiteTextChannel;
import me.eglp.gv2.util.scripting.ScriptExecutionException;

public class JSTextChannel extends JSMessageChannel {

	private GraphiteTextChannel channel;

	public JSTextChannel(GraphiteTextChannel channel) {
		super(channel);
		this.channel = channel;
	}

	/**
	 * Returns whether Graphite can talk in this channel
	 * @return Whether Graphite can talk in this channel
	 */
	public boolean canTalk() {
		return channel.getJDAChannel().canTalk();
	}

	/**
	 * Returns the id of this channel
	 * @return The id of this channel
	 * @see GraphiteID
	 */
	public String getID() {
		return channel.getID();
	}

	/**
	 * Returns this text channel as a discord mention
	 * @return This text channel as a discord mention
	 */
	public String getAsMention() {
		return channel.getAsMention();
	}

	/**
	 * Deletes messages from this text channel (starting from the newest one)
	 * @param amount The amount of messages to delete
	 * @throws ScriptExecutionException If {@code amount} is less than 1 or greater than 100
	 */
	public void clear(int amount) throws ScriptExecutionException {
		if(amount < 1 || amount > 100) throw new ScriptExecutionException("amount cannot be less than 1 or exceed 100");
		channel.clear(amount);
	}

	@Override
	public String toString() {
		return "[JS Text Channel: " + getID() + "]";
	}

}

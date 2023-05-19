package me.eglp.gv2.util.scripting.object;

import me.eglp.gv2.guild.GraphiteVoiceChannel;
import net.dv8tion.jda.api.entities.channel.ChannelType;

public class JSVoiceChannel {

	private GraphiteVoiceChannel channel;

	public JSVoiceChannel(GraphiteVoiceChannel channel) {
		this.channel = channel;
	}

	/**
	 * Returns the name of this channel
	 * @return The name of this channel
	 */
	public String getName() {
		return channel.getName();
	}

	/**
	 * Returns the id of this channel
	 * @return The id of this channel
	 * @see GraphiteID
	 */
	public String getID() {
		return channel.getID();
	}

	public JSChannelType getType() {
		return new JSChannelType(ChannelType.VOICE);
	}

	@Override
	public String toString() {
		return "[JS Voice Channel: " + getID() + "]";
	}

}

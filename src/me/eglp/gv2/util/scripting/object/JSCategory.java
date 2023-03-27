package me.eglp.gv2.util.scripting.object;

import org.mozilla.javascript.Scriptable;

import me.eglp.gv2.util.base.guild.GraphiteCategory;
import me.eglp.gv2.util.scripting.GraphiteScript;
import net.dv8tion.jda.api.entities.channel.ChannelType;

public class JSCategory {

	private GraphiteCategory channel;
	
	public JSCategory(GraphiteCategory channel) {
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
	 */
	public String getID() {
		return channel.getID();
	}
	
	public JSChannelType getType() {
		return new JSChannelType(ChannelType.CATEGORY);
	}
	
	/**
	 * Returns an array of all text channels in this category
	 * @return An array of all text channels in this category
	 * @see JSTextChannel
	 */
	public Scriptable getTextChannels() {
		return GraphiteScript.createJSArray(channel.getTextChannels().stream().map(JSTextChannel::new).toArray(JSTextChannel[]::new));
	}

	/**
	 * Returns an array of all voice channels in this category
	 * @return An array of all voice channels in this category
	 * @see JSVoiceChannel
	 */
	public Scriptable getVoiceChannels() {
		return GraphiteScript.createJSArray(channel.getVoiceChannels().stream().map(JSVoiceChannel::new).toArray(JSVoiceChannel[]::new));
	}
	
	@Override
	public String toString() {
		return "[JS Category: " + getID() + "]";
	}
	
}

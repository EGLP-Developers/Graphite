package me.eglp.gv2.util.scripting.object;

import java.util.function.Supplier;

import org.mozilla.javascript.Context;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.base.GraphiteMessageChannel;
import me.eglp.gv2.util.base.guild.GraphiteTextChannel;
import me.eglp.gv2.util.base.user.GraphiteUser;
import me.eglp.gv2.util.scripting.GraphiteScript;
import me.eglp.gv2.util.scripting.ScriptExecutionException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.ChannelType;

public abstract class JSMessageChannel {
	
	private static final String
		BLOCK_URL = Graphite.getMainBotInfo().getWebsite().getBaseURL() + "/block_scripts?guild=%s",
		UNBLOCK_URL = Graphite.getMainBotInfo().getWebsite().getBaseURL() + "/unblock_scripts";
	
	private static final Supplier<String> MESSAGE_APPENDIX = () -> {
			Context cx = Context.getCurrentContext();
			GraphiteScript sc = (GraphiteScript) cx.getThreadLocal("script");
			return String.format("\n\n*Sent via GraphiteScript* | [Block server](%s) - [Unblock all servers](%s)",
					String.format(BLOCK_URL, sc.getOwner().getID()),
					UNBLOCK_URL);
	};
	
	private GraphiteMessageChannel<?> channel;
	
	public JSMessageChannel(GraphiteMessageChannel<?> channel) {
		this.channel = channel;
	}
	
	/**
	 * Returns the name of this message channel
	 * @return The name of this message channel
	 */
	public String getName() {
		return channel.getName();
	}
	
	/**
	 * Checks whether the bot can write to this channel
	 * @return Whether the bot can write to this channel
	 */
	public boolean canWrite() {
		return channel instanceof GraphiteTextChannel ? ((GraphiteTextChannel) channel).canWrite() : true;
	}
	
	/**
	 * Sends a message to this channel (gray embed)
	 * @param msg The message text
	 */
	public void sendMessage(String msg) {
		sendColoredMessage(msg, Role.DEFAULT_COLOR_RAW); // Damit der message appendix funktioniert
	}
	
	/**
	 * Sends a message (embed) with the specified color
	 * @param msg The message text
	 * @param color The message color
	 */
	public void sendColoredMessage(String msg, int color) {
		if(!canWrite()) throw new ScriptExecutionException("Can't write to this channel");
		Context cx = Context.getCurrentContext();
		int mC = (int) cx.getThreadLocal("messageCount");
		if(mC >= 3) throw new ScriptExecutionException("Message limit (3) reached");
		cx.putThreadLocal("messageCount", mC + 1);
		if(channel.getJDAChannel().getType().equals(ChannelType.PRIVATE)) {
			GraphiteUser owner = (GraphiteUser) channel.getOwner();
			GraphiteScript sc = (GraphiteScript) cx.getThreadLocal("script");
			if(owner.getConfig().isGuildBlocked(sc.getOwner().asGuild())) return;
		}
		GraphiteScript.runRatelimitedAction("sendMessage", 1000L, () -> {
			channel.sendMessage(new EmbedBuilder()
					.setDescription(msg + MESSAGE_APPENDIX.get())
					.setColor(color)
					.build());
		});
	}
	
	/**
	 * Returns the type of this channel
	 * @return The type of this channel
	 * @see JSVars
	 */
	public JSChannelType getType() {
		return new JSChannelType(channel.getJDAChannel().getType());
	}
	
	@Override
	public String toString() {
		return "[JS Message Channel: " + channel.getJDAChannel().getId() + "]";
	}
	
}

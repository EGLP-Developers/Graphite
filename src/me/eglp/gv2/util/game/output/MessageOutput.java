package me.eglp.gv2.util.game.output;

import me.eglp.gv2.main.DebugCategory;
import me.eglp.gv2.main.GraphiteDebug;
import me.eglp.gv2.util.base.GraphiteMessageChannel;
import me.eglp.gv2.util.game.output.renderer.MessageGraphics;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

public class MessageOutput implements GameOutput {
	
	private GraphiteMessageChannel<?> channel;
	private Message msg;
	private boolean keepOldMessages;
	
	public MessageOutput(Message msg, boolean keepOldMessages) {
		this.msg = msg;
		this.keepOldMessages = keepOldMessages;
	}
	
	public MessageOutput(Message msg) {
		this(msg, false);
	}
	
	public MessageOutput(GraphiteMessageChannel<?> channel, boolean keepOldMessages) {
		this.channel = channel;
		this.keepOldMessages = keepOldMessages;
	}
	
	public MessageOutput(GraphiteMessageChannel<?> channel) {
		this(channel, false);
	}
	
	public Message getMessage() {
		return msg;
	}
	
	public void update(MessageEditData message, boolean resend) {
		if(msg == null || resend) {
			if(!keepOldMessages && msg != null) msg.delete().queue();
			msg = channel.sendMessageComplete(MessageCreateData.fromEditData(message));
			return;
		}
		msg.editMessage(message).queue(null, t -> {
			GraphiteDebug.log(DebugCategory.MISCELLANEOUS, "Failed to edit message", t);
		});
	}
	
	public void update(MessageGraphics graphics, boolean resend) {
		update(new MessageEditBuilder().setContent(graphics.render()).build(), resend);
	}
	
	public void update(String str, boolean resend) {
		update(new MessageEditBuilder().setContent(str).build(), resend);
	}
	
	public void update(String str) {
		update(str, false);
	}
	
	public void update(MessageGraphics graphics) {
		update(graphics, false);
	}
	
	public void update(MessageEmbed embed, boolean resend) {
		update(new MessageEditBuilder().setEmbeds(embed).build(), resend);
	}
	
	public void update(MessageEmbed embed) {
		update(embed, false);
	}
	
	public void update(MessageEditBuilder builder, boolean resend) {
		update(builder.setReplace(true).build(), resend);
	}
	
	public void update(MessageEditBuilder builder) {
		update(builder, false);
	}

	@Override
	public void remove() {
		if(msg != null) {
			msg.delete().queue(null, t -> {
				GraphiteDebug.log(DebugCategory.MISCELLANEOUS, "Failed to delete message", t);
			});
			msg = null;
		}
	}

}

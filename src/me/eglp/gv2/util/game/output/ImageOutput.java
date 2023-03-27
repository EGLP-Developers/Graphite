package me.eglp.gv2.util.game.output;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import me.eglp.gv2.main.DebugCategory;
import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.main.GraphiteDebug;
import me.eglp.gv2.util.base.GraphiteMessageChannel;
import net.dv8tion.jda.api.entities.Message;

public class ImageOutput implements GameOutput { // TODO: broken

	public static final Map<String, BufferedImage> IMAGES = new HashMap<>(); // TODO: geht das überhaupt? + image expire
	
	static {
		BufferedImage img = new BufferedImage(500, 500, BufferedImage.TYPE_3BYTE_BGR);
		Graphics2D g2d = img.createGraphics();
		g2d.setColor(Color.RED);
		g2d.fillRect(0, 0, 50, 50);
		img.flush();
		IMAGES.put("test", img);
	}
	
	private GraphiteMessageChannel<?> channel;
	private Message msg;
	private boolean keepOldMessages;
	
	public ImageOutput(Message msg, boolean keepOldMessages) {
		this.msg = msg;
		this.keepOldMessages = keepOldMessages;
	}
	
	public ImageOutput(Message msg) {
		this(msg, false);
	}
	
	public ImageOutput(GraphiteMessageChannel<?> channel, boolean keepOldMessages) {
		this.channel = channel;
		this.keepOldMessages = keepOldMessages;
	}
	
	public ImageOutput(GraphiteMessageChannel<?> channel) {
		this(channel, false);
	}
	
	public Message getMessage() {
		return msg;
	}
	
	public void update(String str, boolean resend) {
		if(msg == null || resend) {
			if(!keepOldMessages && msg != null) msg.delete().queue();
			msg = channel.sendMessageComplete(str);
			return;
		}
		msg.editMessage(str).queue(null, t -> {
			GraphiteDebug.log(DebugCategory.MISCELLANEOUS, "Failed to edit message", t);
		});
	}
	
	public void update(String str) {
		update(str, false);
	}
	
	public void update(BufferedImage image, boolean resend) {
		String id = UUID.randomUUID().toString();
		IMAGES.put(id, image);
		update(Graphite.getMainBotInfo().getWebsite().getBaseURL() + "/msg-image.php?id=" + id, resend);
	}
	
	public void update(BufferedImage image) {
		update(image, false);
	}
	
	@Override
	public void remove() {
		if(msg != null) msg.delete().queue(null, t -> {
			GraphiteDebug.log(DebugCategory.MISCELLANEOUS, "Failed to delete message", t);
		});
	}
	
}

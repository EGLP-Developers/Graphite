package me.eglp.gv2.util.stats.element;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

public interface StatisticsRenderer {
	
	public static final int EMOJI_SIZE = 28;
	public static final Font DEFAULT_FONT = Font.decode("DejaVu Sans-plain-20");
	
	public void renderElement(Graphics2D g2d, GuildStatisticsElement element, int width, int height);
	
	public static void drawLabel(Graphics2D g2d, Label label, int x, int y) { // r2d = Bounds for text/emoji
		if(label.isUnicodeEmoji() || label.isCustomEmoji()) {
			g2d.drawImage(label.getImage(), x, (int) (y - label.getBounds().getCenterY()), (int) label.getBounds().getWidth(), (int) label.getBounds().getHeight(), null);
		}else {
			g2d.setFont(DEFAULT_FONT);
			g2d.setColor(Color.WHITE);
			g2d.drawString(label.getRaw(), x, (int) (y - label.getBounds().getCenterY()));
		}
	}
	
}

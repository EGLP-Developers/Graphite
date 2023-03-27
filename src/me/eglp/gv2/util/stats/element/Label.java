package me.eglp.gv2.util.stats.element;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.regex.Matcher;

import javax.imageio.ImageIO;

import me.eglp.gv2.util.GraphiteUtil;
import me.eglp.gv2.util.emote.unicode.GraphiteUnicode;
import me.eglp.gv2.util.twemoji.Twemoji;
import me.mrletsplay.mrcore.http.HttpGet;
import me.mrletsplay.mrcore.misc.FriendlyException;

public class Label {
	
	private String raw;
	private boolean isUnicodeEmoji;
	private boolean isCustomEmoji;
	private Rectangle2D bounds;
	private String imageURL;
	private BufferedImage image;
	
	public Label(String raw, boolean isUnicodeEmoji, boolean isCustomEmoji, Rectangle2D bounds, String imageURL, BufferedImage image) {
		this.raw = raw;
		this.isUnicodeEmoji = isUnicodeEmoji;
		this.isCustomEmoji = isCustomEmoji;
		this.bounds = bounds;
		this.imageURL = imageURL;
		this.image = image;
	}

	public String getRaw() {
		return raw;
	}

	public boolean isUnicodeEmoji() {
		return isUnicodeEmoji;
	}

	public boolean isCustomEmoji() {
		return isCustomEmoji;
	}

	public Rectangle2D getBounds() {
		return bounds;
	}

	public String getImageURL() {
		return imageURL;
	}
	
	public BufferedImage getImage() {
		return image;
	}
	
	public static Label parse(Graphics2D g2d, String label, int targetWidth) {
		boolean unicode = isUnicodeEmoji(label), custom = isCustomEmoji(label);
		if(unicode || custom) {
			String url = unicode ? null : getCustomEmojiURL(label);
			
			BufferedImage img;
			try {
				img = unicode ? Twemoji.loadEmoji(label) : ImageIO.read(new ByteArrayInputStream(new HttpGet(url).execute().asRaw()));
			}catch(IllegalStateException e) { // Request unsuccessful
				try {
					img = ImageIO.read(new ByteArrayInputStream(new HttpGet("https://raw.githubusercontent.com/twitter/twemoji/master/assets/72x72/2753.png").execute().asRaw()));
				} catch (IOException e1) {
					throw new FriendlyException(e1);
				}
			}catch(IOException e) {
				throw new FriendlyException(e);
			}
			
			int height = (int) (((double) targetWidth / img.getWidth()) * img.getHeight());
			
			return new Label(label, false, true, new Rectangle(targetWidth, height), url, img);
		}else {
			label = GraphiteUtil.truncateToLength(label, 20, true);
			g2d.setFont(StatisticsRenderer.DEFAULT_FONT);
			return new Label(label, false, false, g2d.getFontMetrics().getStringBounds(label, g2d), null, null);
		}
	}
	
	public static boolean isUnicodeEmoji(String str) {
		return GraphiteUnicode.getRawCodePoints().contains(str);
	}
	
	public static boolean isCustomEmoji(String str) {
		return GraphiteUtil.CUSTOM_EMOJI_PATTERN.matcher(str).matches();
	}
	
	public static String getCustomEmojiURL(String str) {
		Matcher m = GraphiteUtil.CUSTOM_EMOJI_PATTERN.matcher(str);
		if(!m.matches()) return null;
		return "https://cdn.discordapp.com/emojis/" + m.group("id") + ".png?size=128";
	}
	
}

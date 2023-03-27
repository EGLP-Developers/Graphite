package me.eglp.gv2.util.website;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.imageio.ImageIO;

import me.mrletsplay.mrcore.misc.FriendlyException;

public class WebsiteImageCache {
	
	private Map<String, WebsiteImage> imageCache;
	
	public WebsiteImageCache() {
		this.imageCache = new HashMap<>();
	}
	
	public String addImage(BufferedImage image, long cacheTime) {
		try {
			ByteArrayOutputStream bOut = new ByteArrayOutputStream();
			ImageIO.write(image, "PNG", bOut);
			byte[] rendered = bOut.toByteArray();
			String id = UUID.randomUUID().toString().replace("-", "");
			imageCache.put(id, new WebsiteImage(id, rendered, System.currentTimeMillis() + cacheTime));
			return id;
		}catch(IOException e) {
			throw new FriendlyException(e);
		}
	}
	
	public byte[] getImage(String id) {
		WebsiteImage i = imageCache.get(id);
		if(i == null) return null;
		return i.getRenderedImage();
	}
	
	public void removeImage(String id) {
		imageCache.remove(id);
	}
	
	public void refreshCache() {
		new HashMap<>(imageCache).values().forEach(e -> {
			if(e.isExpired()) e.remove();
		});
	}

}

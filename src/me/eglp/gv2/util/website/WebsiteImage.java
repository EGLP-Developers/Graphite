package me.eglp.gv2.util.website;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.base.GraphiteTemporary;

public class WebsiteImage implements GraphiteTemporary {
	
	private String id;
	private byte[] renderedImage;
	private long expiresAt;
	
	public WebsiteImage(String id, byte[] renderedImage, long expiresAt) {
		this.id = id;
		this.renderedImage = renderedImage;
		this.expiresAt = expiresAt;
	}
	
	public byte[] getRenderedImage() {
		return renderedImage;
	}

	@Override
	public void remove() {
		Graphite.getWebsiteEndpoint().getImageCache().removeImage(id);
	}
	
	@Override
	public long getExpirationTime() {
		return expiresAt;
	}

}

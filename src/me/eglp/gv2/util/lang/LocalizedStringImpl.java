package me.eglp.gv2.util.lang;

public class LocalizedStringImpl implements LocalizedString {

	private String messagePath, fallback;
	
	public LocalizedStringImpl(String messagePath, String fallback) {
		this.messagePath = messagePath;
		this.fallback = fallback;
	}
	
	public LocalizedStringImpl(String fallback) {
		this("this.is.an.invalid.path", fallback);
	}
	
	@Override
	public String getMessagePath() {
		return messagePath;
	}

	@Override
	public String getFallback() {
		return fallback;
	}
	
}

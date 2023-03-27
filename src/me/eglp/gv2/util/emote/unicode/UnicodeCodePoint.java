package me.eglp.gv2.util.emote.unicode;

public class UnicodeCodePoint {
	
	private String unicode;
	private String description;
	
	public UnicodeCodePoint(String unicode, String description) {
		this.unicode = unicode;
		this.description = description;
	}
	
	public String getUnicode() {
		return unicode;
	}
	
	public String getDescription() {
		return description;
	}
	
}

package me.eglp.gv2.util.command.text.argument;

import java.util.Arrays;

import me.eglp.gv2.util.base.GraphiteLocalizable;
import me.eglp.gv2.util.lang.DefaultLocaleString;

public class CommandArgument {

	private String raw;
	
	public CommandArgument(String raw) {
		this.raw = raw;
	}
	
	public String getRaw() {
		return raw;
	}
	
	public MentionArgument asMention() {
		return this instanceof MentionArgument ? (MentionArgument) this : null;
	}
	
	public boolean isInt() {
		try {
			Integer.valueOf(raw);
			return true;
		} catch(NumberFormatException e) {
			return false;
		}
	}
	
	public int asInt() {
		return Integer.valueOf(raw);
	}
	
	public boolean isLong() {
		try {
			Long.valueOf(raw);
			return true;
		} catch(NumberFormatException e) {
			return false;
		}
	}
	
	public long asLong() {
		return Long.valueOf(raw);
	}
	
	public boolean isDouble() {
		try {
			Double.valueOf(raw);
			return true;
		} catch(NumberFormatException e) {
			return false;
		}
	}
	
	public double asDouble() {
		return Double.valueOf(raw);
	}
	
	public boolean isBoolean(GraphiteLocalizable localizable) {
		return Arrays.stream(DefaultLocaleString.OTHER_YES.getFor(localizable).split(",")).anyMatch(f -> f.equalsIgnoreCase(raw)) || Arrays.stream(DefaultLocaleString.OTHER_NO.getFor(localizable).split(",")).anyMatch(f -> f.equalsIgnoreCase(raw));
	}
	
	public boolean asBoolean(GraphiteLocalizable localizable) {
		if(Arrays.stream(DefaultLocaleString.OTHER_YES.getFor(localizable).split(",")).anyMatch(f -> f.equalsIgnoreCase(raw))) {
			return true;
		}else if(Arrays.stream(DefaultLocaleString.OTHER_NO.getFor(localizable).split(",")).anyMatch(f -> f.equalsIgnoreCase(raw))) {
			return false;
		}
		throw new IllegalStateException("Not a boolean");
	}
	
}

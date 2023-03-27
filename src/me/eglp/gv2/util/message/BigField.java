package me.eglp.gv2.util.message;

public class BigField {
	
	private String name;
	private String value;
	private boolean inline;
	
	public BigField(String name, String value, boolean inline) {
		this.name = name;
		this.value = value;
		this.inline = inline;
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}

	public boolean isInline() {
		return inline;
	}
	
}

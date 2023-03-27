package me.eglp.gv2.util;

public enum GraphiteIcon {

	ERROR("error-new.png"),
	LOADING_GIF("loading.gif"),
	CHECKMARK("checkmark-new.png"),
	INFORMATION("information-new.png");
	
	private final String path;
	
	private GraphiteIcon(String path) {
		this.path = path;
	}
	
	public String getPath() {
		return "https://graphite-official.com/graphite-icons/" + path;
	}

}

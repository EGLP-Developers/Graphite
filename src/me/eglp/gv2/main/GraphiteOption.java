package me.eglp.gv2.main;

public enum GraphiteOption {
	
	MYSQL_DEBUG("mysql-debug"),
	SELFCHECK("selfcheck"),
	WEBINTERFACE_DEBUG("webinterface-debug"),
	;
	
	private final String name;
	
	private GraphiteOption(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

}

package me.eglp.gv2.util.backup;

import java.util.HashMap;
import java.util.Map;

public class IDMappings {

	private Map<String, String> channelRemappings;
	
	public IDMappings() {
		this.channelRemappings = new HashMap<>();
	}
	
	public void put(String oldID, String newID) {
		channelRemappings.put(oldID, newID);
	}
	
	public String getNewID(String oldID) {
		if(oldID == null) return null;
		return channelRemappings.get(oldID);
	}
	
}

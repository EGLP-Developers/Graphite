package me.eglp.gv2.util.backup;

import java.util.HashMap;
import java.util.Map;

public class IDMappings {

	private Map<String, String> idRemappings;
	
	public IDMappings() {
		this.idRemappings = new HashMap<>();
	}
	
	public void put(String oldID, String newID) {
		idRemappings.put(oldID, newID);
	}
	
	public String getNewID(String oldID) {
		if(oldID == null) return null;
		return idRemappings.get(oldID);
	}
	
}

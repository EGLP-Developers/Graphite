package me.eglp.gv2.util.scripting.object;

import me.eglp.gv2.util.backup.RestoreSelector;

public class JSRestoreSelector {

	protected RestoreSelector type;
	
	public JSRestoreSelector(RestoreSelector type) {
		this.type = type;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof JSRestoreSelector)) return false;
		return type.equals(((JSRestoreSelector) obj).type);
	}
	
	@Override
	public String toString() {
		return "[JS Restore Selector: " + type.name() + "]";
	}
	
}

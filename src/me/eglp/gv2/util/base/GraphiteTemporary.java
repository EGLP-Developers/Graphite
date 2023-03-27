package me.eglp.gv2.util.base;

public interface GraphiteTemporary {
	
	public void remove();
	
	public long getExpirationTime();
	
	public default boolean isExpired() {
		return System.currentTimeMillis() >= getExpirationTime();
	}
	
}

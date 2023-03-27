package me.eglp.gv2.util.base;

public interface GraphiteLocalizedObject<T> {

	public T getFor(GraphiteLocalizable localized, String... params);
	
}

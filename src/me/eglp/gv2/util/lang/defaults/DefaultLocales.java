package me.eglp.gv2.util.lang.defaults;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DefaultLocales {
	
	private static Map<String, DefaultLocale> defaultLocales = new HashMap<>();
	
	static {
		registerLocale("de");
		registerLocale("tr");
	}
	
	private static void registerLocale(String localeIdentifier) {
		defaultLocales.put(localeIdentifier, new DefaultLocale(localeIdentifier));
	}
	
	public static DefaultLocale getDefaultLocale(String localeIdentifier) {
		return defaultLocales.get(localeIdentifier);
	}
	
	public static Set<String> getDefaultLocaleKeys() {
		return new HashSet<>(defaultLocales.keySet());
	}
	
}

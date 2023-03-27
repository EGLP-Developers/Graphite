package me.eglp.gv2.util.base.user;

import me.eglp.gv2.util.lang.GraphiteLocale;

public class UserFallbackLocale implements GraphiteLocale {
	
	public static final UserFallbackLocale INSTANCE = new UserFallbackLocale();

	@Override
	public String getString(String path, String fallback) {
		return fallback;
	}

}

package me.eglp.gv2.util.lang.defaults;

import me.eglp.gv2.util.lang.GraphiteLocale;
import me.mrletsplay.mrcore.config.ConfigLoader;
import me.mrletsplay.mrcore.config.CustomConfig;

public class DefaultLocale implements GraphiteLocale {
	
	private CustomConfig messageConfig;
	
	public DefaultLocale(String localeIdentifier) {
		messageConfig = ConfigLoader.loadStreamConfig(DefaultLocale.class.getResourceAsStream("/include/lang/" + localeIdentifier + ".yml"), true);
	}

	@Override
	public String getString(String path, String fallback) {
		return messageConfig.getString(path, fallback, false);
	}
	
	public CustomConfig getMessageConfig() {
		return messageConfig;
	}
	
}

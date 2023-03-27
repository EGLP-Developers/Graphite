package me.eglp.gv2.util.lang;

public interface GraphiteLocale {
	
	public static final String DEFAULT_LOCALE_KEY = "en";

	public String getString(String path, String fallback);
	
	public default String getString(LocalizedString string) {
		return getString(string.getMessagePath(), string.getFallback());
	}
	
	public default String getString(LocalizedString string, String... params) {
		String msg = getString(string);
        if(params.length%2!=0){
			throw new IllegalArgumentException("Invalid params");
		}
        for(int i = 0; i<params.length; i+=2){
        	msg = msg.replace("{"+params[i]+"}", params[i+1]);
		}
		return msg;
	}
	
}

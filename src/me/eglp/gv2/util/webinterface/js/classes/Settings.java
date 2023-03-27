package me.eglp.gv2.util.webinterface.js.classes;

import java.util.List;

import me.eglp.gv2.multiplex.bot.MultiplexBot;
import me.eglp.gv2.util.webinterface.js.JavaScriptFunction;
import me.eglp.gv2.util.webinterface.js.JavaScriptParameter;
import me.eglp.gv2.util.webinterface.js.JavaScriptValue;
import me.eglp.gv2.util.webinterface.js.WebinterfaceObject;

public class Settings implements WebinterfaceObject{
	
	@JavaScriptValue(getter = "getBot")
	private MultiplexBot bot;
	
	@JavaScriptValue(getter = "getPrefix")
	private String prefix;
	
	@JavaScriptValue(getter = "getLocale")
	private String locale;
	
	@JavaScriptValue(getter = "getNickname")
	private String nickname;
	
	@JavaScriptValue(getter = "getAvailableLocales")
	private List<String> locales;
	
	@JavaScriptValue(getter = "getEnableTextCommands")
	private boolean enableTextCommands;
	
	public Settings(MultiplexBot bot, String prefix, String locale, String nickname, List<String> locales, boolean enableTextCommands) {
		this.bot = bot;
		this.prefix = prefix;
		this.locale = locale;
		this.nickname = nickname;
		this.locales = locales;
		this.enableTextCommands = enableTextCommands;
	}
	
	@JavaScriptFunction(calling = "setPrefix", withGuild = true)
	public static void setPrefix(@JavaScriptParameter(name = "bot") String bot, @JavaScriptParameter(name = "prefix") String prefix) {};
	
	@JavaScriptFunction(calling = "setLocale", withGuild = true)
	public static void setLocale(@JavaScriptParameter(name = "bot") String bot, @JavaScriptParameter(name = "locale") String locale) {};
	
	@JavaScriptFunction(calling = "setNickname", withGuild = true)
	public static void setNickname(@JavaScriptParameter(name = "bot") String bot, @JavaScriptParameter(name = "nickname") String nickname) {};
	
	@JavaScriptFunction(calling = "getSettings", returning = "settings", withGuild = true)
	public static void getSettings(@JavaScriptParameter(name = "bot") String bot) {};
	
	@JavaScriptFunction(calling = "getAvailableZoneIds", returning = "zoneIds", withGuild = false)
	public static void getAvailableZoneIds() {};
	
	@JavaScriptFunction(calling = "setTimezone", withGuild = true)
	public static void setTimezone(@JavaScriptParameter(name = "timezone") String timezone) {};
	
	@JavaScriptFunction(calling = "getTimezone", returning = "zone", withGuild = true)
	public static void getTimezone() {};
	
	@JavaScriptFunction(calling = "setEnableTextCommands", withGuild = true)
	public static void setEnableTextCommands(@JavaScriptParameter(name = "enable") boolean enable) {};
	
	@JavaScriptFunction(calling = "getEnableTextCommands", returning = "enable", withGuild = true)
	public static void getEnableTextCommands() {};

}

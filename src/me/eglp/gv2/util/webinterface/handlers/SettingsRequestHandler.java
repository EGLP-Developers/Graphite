package me.eglp.gv2.util.webinterface.handlers;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.ArrayList;

import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.guild.config.GuildConfig;
import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.multiplex.GraphiteMultiplex;
import me.eglp.gv2.multiplex.bot.MultiplexBot;
import me.eglp.gv2.util.selfcheck.SpecialSelfcheck;
import me.eglp.gv2.util.webinterface.WebinterfaceHandler;
import me.eglp.gv2.util.webinterface.base.WebinterfaceRequestEvent;
import me.eglp.gv2.util.webinterface.base.WebinterfaceResponse;
import me.eglp.gv2.util.webinterface.js.classes.Settings;
import me.mrletsplay.mrcore.json.JSONArray;
import me.mrletsplay.mrcore.json.JSONObject;

public class SettingsRequestHandler {

	@WebinterfaceHandler(requestMethod = "getSettings", requireGuild = true, requireBot = true, requireGuildAdmin = true)
	public static WebinterfaceResponse getSettings(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		MultiplexBot b = GraphiteMultiplex.getCurrentBot();
		if(b == null) return WebinterfaceResponse.error("Invalid bot");
		JSONObject o = new JSONObject();
		Graphite.withBot(b, () -> {
			String nickname = g.getSelfMember().getJDAMember().getNickname();
			o.put("settings", new Settings(GraphiteMultiplex.getCurrentBot(), g.getConfig().getPrefix(), g.getConfig().getLocale(), nickname==null?"-":nickname, new ArrayList<>(g.getLocale().getAvailableLocales()), g.getConfig().hasTextCommands()).toWebinterfaceObject());
		});
		return WebinterfaceResponse.success(o);
	}

	@WebinterfaceHandler(requestMethod = "setPrefix", requireGuild = true, requireBot = true, requireGuildAdmin = true)
	public static WebinterfaceResponse setPrefix(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		if(!event.getRequestData().has("prefix")) {
			return WebinterfaceResponse.error("You must enter a prefix");
		}
		MultiplexBot b = GraphiteMultiplex.getCurrentBot();
		if(b == null) return WebinterfaceResponse.error("Invalid bot");
		String prefix = event.getRequestData().getString("prefix");
		if(prefix == null || !GuildConfig.PREFIX_PATTERN.matcher(prefix).matches()) {
			return WebinterfaceResponse.error("The prefix you entered is invalid (up to 16 alphanumeric characters, _-~.! allowed)");
		}
		Graphite.withBot(b, () -> g.getConfig().setPrefix(prefix));
		return WebinterfaceResponse.success();
	}

	@WebinterfaceHandler(requestMethod = "setLocale", requireGuild = true, requireBot = true, requireGuildAdmin = true)
	public static WebinterfaceResponse setLocale(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		MultiplexBot b = GraphiteMultiplex.getCurrentBot();
		if(b == null) return WebinterfaceResponse.error("Invalid bot");
		String locale = event.getRequestData().getString("locale");
		Graphite.withBot(b, () -> g.getConfig().setLocale(locale));
		return WebinterfaceResponse.success();
	}

	@WebinterfaceHandler(requestMethod = "setNickname", requireGuild = true, requireBot = true, requireGuildAdmin = true)
	public static WebinterfaceResponse setNickname(WebinterfaceRequestEvent event) {
		if(!event.getRequestData().has("nickname")) {
			return WebinterfaceResponse.error("You must enter a nickname");
		}
		MultiplexBot b = GraphiteMultiplex.getCurrentBot();
		if(b == null) return WebinterfaceResponse.error("Invalid bot");
		String nickname = event.getRequestData().getString("nickname");
		Graphite.withBot(b, () -> event.getSelectedGuild().getJDAGuild().modifyNickname(event.getSelectedGuild().getSelfMember().getJDAMember(), nickname).complete());
		return WebinterfaceResponse.success();
	}

	@SpecialSelfcheck(ignoreAccessibleToEveryone = true)
	@WebinterfaceHandler(requestMethod = "getAvailableZoneIds")
	public static WebinterfaceResponse getAvailableZoneIds(WebinterfaceRequestEvent event) {
		JSONObject d = new JSONObject();
		d.set("zoneIds", new JSONArray(ZoneId.getAvailableZoneIds()));
		return WebinterfaceResponse.success(d);
	}

	@WebinterfaceHandler(requestMethod = "setTimezone", requireGuild = true, requireGuildAdmin = true)
	public static WebinterfaceResponse setTimezone(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		if(!event.getRequestData().has("timezone")) {
			return WebinterfaceResponse.error("You must enter a timezone");
		}
		try {
			ZoneId zone = ZoneId.of(event.getRequestData().getString("timezone"));
			g.getConfig().setTimezone(zone);
		}catch(DateTimeException e) {
			return WebinterfaceResponse.error("Invalid zone");
		}
		return WebinterfaceResponse.success();
	}

	@WebinterfaceHandler(requestMethod = "getTimezone", requireGuild = true, requireGuildAdmin = true)
	public static WebinterfaceResponse getTimezone(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		JSONObject d = new JSONObject();
		d.set("zone", g.getConfig().getTimezone().getId());
		return WebinterfaceResponse.success(d);
	}

	@WebinterfaceHandler(requestMethod = "setEnableTextCommands", requireGuild = true, requireGuildAdmin = true)
	public static WebinterfaceResponse setEnableTextCommands(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		if(!event.getRequestData().has("enable")) return WebinterfaceResponse.error("No enable");
		boolean enable = event.getRequestData().getBoolean("enable");
		g.getConfig().setTextCommands(enable);
		return WebinterfaceResponse.success();
	}

	@WebinterfaceHandler(requestMethod = "getEnableTextCommands", requireGuild = true, requireGuildAdmin = true)
	public static WebinterfaceResponse getEnableTextCommands(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		JSONObject d = new JSONObject();
		d.set("enable", g.getConfig().hasTextCommands());
		return WebinterfaceResponse.success(d);
	}

}

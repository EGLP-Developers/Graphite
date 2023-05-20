package me.eglp.gv2.util.webinterface.handlers;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.util.Base64;
import java.util.List;

import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.guild.chatreport.GuildChatReport;
import me.eglp.gv2.guild.chatreport.GuildChatReportMessage;
import me.eglp.gv2.guild.config.GuildReportsConfig;
import me.eglp.gv2.util.crypto.GraphiteCrypto;
import me.eglp.gv2.util.permission.DefaultPermissions;
import me.eglp.gv2.util.webinterface.WebinterfaceHandler;
import me.eglp.gv2.util.webinterface.base.WebinterfaceRequestEvent;
import me.eglp.gv2.util.webinterface.base.WebinterfaceResponse;
import me.mrletsplay.mrcore.json.JSONArray;
import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.mrcore.json.JSONType;

public class ChatReportRequestHandler {

	@WebinterfaceHandler(requestMethod = "chatReportsEnabled", requireGuild = true, requirePermissions = DefaultPermissions.WEBINTERFACE_MODERATION)
	public static WebinterfaceResponse chatReportsEnabled(WebinterfaceRequestEvent event) {
		JSONObject o = new JSONObject();
		o.put("is-enabled", event.getSelectedGuild().getReportsConfig().getChatReportKey() != null);

		return WebinterfaceResponse.success(o);
	}

	@WebinterfaceHandler(requestMethod = "enableChatReports", requireGuild = true, requirePermissions = DefaultPermissions.WEBINTERFACE_MODERATION)
	public static WebinterfaceResponse enableChatReports(WebinterfaceRequestEvent event) {
		KeyPair key = GraphiteCrypto.generateKeyPair();

		event.getSelectedGuild().getReportsConfig().setChatReportKey(key.getPublic());

		JSONObject o = new JSONObject();
		o.put("decryption-key", Base64.getEncoder().encodeToString(key.getPrivate().getEncoded()));

		return WebinterfaceResponse.success(o);
	}

	@WebinterfaceHandler(requestMethod = "disableChatReports", requireGuild = true, requirePermissions = DefaultPermissions.WEBINTERFACE_MODERATION)
	public static WebinterfaceResponse disableChatReports(WebinterfaceRequestEvent event) {
		event.getSelectedGuild().getReportsConfig().unsetChatReportKey();
		return WebinterfaceResponse.success();
	}

	@WebinterfaceHandler(requestMethod = "getChatReports", requireGuild = true, requirePermissions = DefaultPermissions.WEBINTERFACE_MODERATION)
	public static WebinterfaceResponse getChatReports(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		List<GuildChatReport> reports = g.getReportsConfig().getChatReports();

		JSONArray arr = new JSONArray();
		for(GuildChatReport r : reports) arr.add(r.toWebinterfaceObject());

		JSONObject o = new JSONObject();
		o.put("chatreports", arr);

		return WebinterfaceResponse.success(o);
	}

	@WebinterfaceHandler(requestMethod = "getChatReportHistory", requireGuild = true, requirePermissions = DefaultPermissions.WEBINTERFACE_MODERATION)
	public static WebinterfaceResponse getChatReportHistory(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();

		String id = event.getRequestData().getString("id");
		GuildChatReport report = g.getReportsConfig().getChatReportByID(id);

		if(report == null) return WebinterfaceResponse.error("ChatReport doesn't exist");

		PrivateKey k = null;

		if(event.getRequestData().isOfType("key", JSONType.STRING)) {
			String key = event.getRequestData().getString("key");
			byte[] encoded = Base64.getDecoder().decode(key);
			k = GraphiteCrypto.decodePrivateKey(encoded);
		}

		if(k == null) {
			return WebinterfaceResponse.error("Invalid key");
		}

		if(!report.isCorrectKey(k)) return WebinterfaceResponse.error("Wrong key");

		JSONArray arr = new JSONArray();

		List<GuildChatReportMessage> gcm = report.loadChatHistory(k);
		gcm.forEach(ch -> arr.add(ch.toWebinterfaceObject()));

		JSONObject o = new JSONObject();
		o.put("chat-history", arr);

		return WebinterfaceResponse.success(o);
	}

	@WebinterfaceHandler(requestMethod = "deleteChatReport", requireGuild = true, requirePermissions = DefaultPermissions.WEBINTERFACE_MODERATION)
	public static WebinterfaceResponse deleteChatReport(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		GuildReportsConfig c = g.getReportsConfig();

		String id = event.getRequestData().getString("id");
		c.removeChatReportByID(id);

		return WebinterfaceResponse.success();
	}

}

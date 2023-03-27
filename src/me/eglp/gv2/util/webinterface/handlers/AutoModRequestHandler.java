package me.eglp.gv2.util.webinterface.handlers;

import me.eglp.gv2.multiplex.GraphiteFeature;
import me.eglp.gv2.util.base.guild.GraphiteGuild;
import me.eglp.gv2.util.base.guild.automod.AbstractAutoModSettings;
import me.eglp.gv2.util.base.guild.automod.badwords.BadWordsSettings;
import me.eglp.gv2.util.base.guild.automod.discord_invites.DiscordInvitesSettings;
import me.eglp.gv2.util.base.guild.automod.excessive_caps.ExcessiveCapsSettings;
import me.eglp.gv2.util.base.guild.automod.excessive_emoji.ExcessiveEmojiSettings;
import me.eglp.gv2.util.base.guild.automod.excessive_mentions.ExcessiveMentionsSettings;
import me.eglp.gv2.util.base.guild.automod.excessive_spoilers.ExcessiveSpoilersSettings;
import me.eglp.gv2.util.base.guild.automod.external_links.ExternalLinksSettings;
import me.eglp.gv2.util.base.guild.automod.repeated_text.RepeatedTextSettings;
import me.eglp.gv2.util.base.guild.automod.zalgo.ZalgoSettings;
import me.eglp.gv2.util.webinterface.WebinterfaceHandler;
import me.eglp.gv2.util.webinterface.base.WebinterfaceRequestEvent;
import me.eglp.gv2.util.webinterface.base.WebinterfaceResponse;
import me.eglp.gv2.util.webinterface.js.ObjectSerializer;
import me.mrletsplay.mrcore.json.JSONObject;

public class AutoModRequestHandler {
	
	@WebinterfaceHandler(requestMethod = "setAutoModSettings", requireGuild = true, requireFeatures = GraphiteFeature.MODERATION)
	public static WebinterfaceResponse setAutoModSettings(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		
		JSONObject o = event.getRequestData().getJSONObject("object");
		AbstractAutoModSettings sets = (AbstractAutoModSettings) ObjectSerializer.deserialize(o);
		g.getAutoModSettings().setSettings(sets);
		
		return WebinterfaceResponse.success();
	}
	
	@WebinterfaceHandler(requestMethod = "getBadWordsSettings", requireGuild = true, requireFeatures = GraphiteFeature.MODERATION)
	public static WebinterfaceResponse getBadWordsSettings(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		BadWordsSettings s = g.getAutoModSettings().getBadWordsSettings();
		
		JSONObject obj = new JSONObject();
		obj.put("settings", s.toWebinterfaceObject());
		return WebinterfaceResponse.success(obj);
	}
	
	@WebinterfaceHandler(requestMethod = "getExcessiveCapsSettings", requireGuild = true, requireFeatures = GraphiteFeature.MODERATION)
	public static WebinterfaceResponse getExcessiveCapsSettings(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		ExcessiveCapsSettings s = g.getAutoModSettings().getExcessiveCapsSettings();
		
		JSONObject obj = new JSONObject();
		obj.put("settings", s.toWebinterfaceObject());
		
		return WebinterfaceResponse.success(obj);
	}
	
	@WebinterfaceHandler(requestMethod = "getExcessiveEmojiSettings", requireGuild = true, requireFeatures = GraphiteFeature.MODERATION)
	public static WebinterfaceResponse getExcessiveEmojiSettings(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		ExcessiveEmojiSettings s = g.getAutoModSettings().getExcessiveEmojiSettings();
		
		JSONObject obj = new JSONObject();
		obj.put("settings", s.toWebinterfaceObject());
		return WebinterfaceResponse.success(obj);
	}
	
	@WebinterfaceHandler(requestMethod = "getExcessiveMentionsSettings", requireGuild = true, requireFeatures = GraphiteFeature.MODERATION)
	public static WebinterfaceResponse getExcessiveMentionsSettings(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		ExcessiveMentionsSettings s = g.getAutoModSettings().getExcessiveMentionsSettings();
		
		JSONObject obj = new JSONObject();
		obj.put("settings", s.toWebinterfaceObject());
		return WebinterfaceResponse.success(obj);
	}
	
	@WebinterfaceHandler(requestMethod = "getExternalLinksSettings", requireGuild = true, requireFeatures = GraphiteFeature.MODERATION)
	public static WebinterfaceResponse getExternalLinksSettings(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		ExternalLinksSettings s = g.getAutoModSettings().getExternalLinksSettings();
		
		JSONObject obj = new JSONObject();
		obj.put("settings", s.toWebinterfaceObject());
		
		return WebinterfaceResponse.success(obj);
	}
	
	@WebinterfaceHandler(requestMethod = "getExcessiveSpoilersSettings", requireGuild = true, requireFeatures = GraphiteFeature.MODERATION)
	public static WebinterfaceResponse getExcessiveSpoilersSettings(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		ExcessiveSpoilersSettings s = g.getAutoModSettings().getExcessiveSpoilersSettings();
		
		JSONObject obj = new JSONObject();
		obj.put("settings", s.toWebinterfaceObject());
		
		return WebinterfaceResponse.success(obj);
	}
	
	@WebinterfaceHandler(requestMethod = "getZalgoSettings", requireGuild = true, requireFeatures = GraphiteFeature.MODERATION)
	public static WebinterfaceResponse getZalgoSettings(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		ZalgoSettings s = g.getAutoModSettings().getZalgoSettings();
		
		JSONObject obj = new JSONObject();
		obj.put("settings", s.toWebinterfaceObject());
		
		return WebinterfaceResponse.success(obj);
	}
	
	@WebinterfaceHandler(requestMethod = "getRepeatedTextSettings", requireGuild = true, requireFeatures = GraphiteFeature.MODERATION)
	public static WebinterfaceResponse getRepeatedTextSettings(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		RepeatedTextSettings s = g.getAutoModSettings().getRepeatedTextSettings();
		
		JSONObject obj = new JSONObject();
		obj.put("settings", s.toWebinterfaceObject());
		
		return WebinterfaceResponse.success(obj);
	}
	
	@WebinterfaceHandler(requestMethod = "getDiscordInvitesSettings", requireGuild = true, requireFeatures = GraphiteFeature.MODERATION)
	public static WebinterfaceResponse getDiscordInvitesSettings(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		DiscordInvitesSettings s = g.getAutoModSettings().getDiscordInvitesSettings();
		
		JSONObject obj = new JSONObject();
		obj.put("settings", s.toWebinterfaceObject());
		return WebinterfaceResponse.success(obj);
	}
	
}

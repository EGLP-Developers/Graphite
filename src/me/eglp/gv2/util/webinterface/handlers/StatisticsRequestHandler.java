package me.eglp.gv2.util.webinterface.handlers;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import me.eglp.gv2.multiplex.GraphiteFeature;
import me.eglp.gv2.util.base.guild.GraphiteGuild;
import me.eglp.gv2.util.base.guild.GraphiteTextChannel;
import me.eglp.gv2.util.selfcheck.SpecialSelfcheck;
import me.eglp.gv2.util.stats.element.GuildStatisticsElement;
import me.eglp.gv2.util.stats.element.StatisticsElementSettings;
import me.eglp.gv2.util.stats.element.StatisticsElementType;
import me.eglp.gv2.util.stats.element.StatisticsRenderer;
import me.eglp.gv2.util.webinterface.WebinterfaceHandler;
import me.eglp.gv2.util.webinterface.base.WebinterfaceRequestEvent;
import me.eglp.gv2.util.webinterface.base.WebinterfaceResponse;
import me.eglp.gv2.util.webinterface.js.ObjectSerializer;
import me.mrletsplay.mrcore.json.JSONArray;
import me.mrletsplay.mrcore.json.JSONObject;

public class StatisticsRequestHandler {
	
	@SpecialSelfcheck(ignoreAccessibleToEveryone = true)
	@WebinterfaceHandler(requestMethod = "createDefaultSettings", requireGuild = true)
	public static WebinterfaceResponse createDefaultSettings(WebinterfaceRequestEvent event) {
		String chartType = event.getRequestData().getString("chart_type");
		StatisticsElementType t = StatisticsElementType.valueOf(chartType);
		if(t == null) return WebinterfaceResponse.error("Invalid chart type");
		
		JSONObject o = new JSONObject();
		o.put("settings", t.createDefaultSettings().toWebinterfaceObject());
		
		return WebinterfaceResponse.success(o);
	}
	
	@WebinterfaceHandler(requestMethod = "createStatisticsElement", requireGuild = true, requireFeatures = GraphiteFeature.STATISTICS)
	public static WebinterfaceResponse createStatisticsElement(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		StatisticsElementType type = (StatisticsElementType) ObjectSerializer.deserialize(event.getRequestData().getJSONObject("type"));
		StatisticsElementSettings settings = (StatisticsElementSettings) ObjectSerializer.deserialize(event.getRequestData().getJSONObject("settings"));
		
		if(!type.getSettingsType().isInstance(settings) || !settings.isValid()) return WebinterfaceResponse.error("Invalid settings");
		
		String channelID = event.getRequestData().getString("channel_id");
		GraphiteTextChannel ch = g.getTextChannelByID(channelID);
		if(ch == null) {
			return WebinterfaceResponse.error("TextChannel doesn't exist");
		}
		
		GuildStatisticsElement el = g.getStatisticsConfig().createStatisticsElement(type, settings);
		el.sendMessage(ch);
		
		JSONObject o = new JSONObject();
		o.put("statistic_element", el.toWebinterfaceObject());
		
		return WebinterfaceResponse.success(o);
	}
	
	@WebinterfaceHandler(requestMethod = "updateStatisticsElementSettings", requireGuild = true, requireFeatures = GraphiteFeature.STATISTICS)
	public static WebinterfaceResponse updateStatisticsElementSettings(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		String elementID = event.getRequestData().getString("element_id");
		GuildStatisticsElement el = g.getStatisticsConfig().getStatisticsElementByID(elementID);
		if(el == null) return WebinterfaceResponse.error("Invalid element id");
		
		StatisticsElementSettings settings = (StatisticsElementSettings) ObjectSerializer.deserialize(event.getRequestData().getJSONObject("settings"));
		if(!el.getType().getSettingsType().isInstance(settings) || !settings.isValid()) return WebinterfaceResponse.error("Invalid settings");
		
		String channelID = event.getRequestData().getString("channel_id");
		GraphiteTextChannel ch = g.getTextChannelByID(channelID);
		if(ch == null) {
			return WebinterfaceResponse.error("TextChannel doesn't exist");
		}
		
		g.getStatisticsConfig().updateStatisticsElementSettings(elementID, settings);
		String oldChannelID = g.getStatisticsConfig().getChannelID(elementID);
		if(oldChannelID == null || !oldChannelID.equals(channelID)) {
			g.getStatisticsConfig().deleteOldMessage(elementID);
			el.sendMessage(ch);
		}else {
			g.getStatisticsConfig().getStatisticsElementByID(elementID).updateMessageIfExists();
		}
		
		return WebinterfaceResponse.success();
	}
	
	@WebinterfaceHandler(requestMethod = "generateStatisticsPreview", requireGuild = true, requireFeatures = GraphiteFeature.STATISTICS)
	public static WebinterfaceResponse generateStatisticsPreview(WebinterfaceRequestEvent event) {
		StatisticsElementType type = (StatisticsElementType) ObjectSerializer.deserialize(event.getRequestData().getJSONObject("type"));
		StatisticsElementSettings settings = (StatisticsElementSettings) ObjectSerializer.deserialize(event.getRequestData().getJSONObject("settings"));
		
		if(!type.getSettingsType().isInstance(settings)) return WebinterfaceResponse.error("Invalid settings");
		
		BufferedImage img;
		if(!settings.isValid()) {
			img = new BufferedImage(720, 480, BufferedImage.TYPE_4BYTE_ABGR);
			Graphics2D g2d = img.createGraphics();
			g2d.setFont(StatisticsRenderer.DEFAULT_FONT.deriveFont(50f));
			g2d.setColor(Color.ORANGE);
			String text = "Invalid settings";
			Rectangle2D r2d = g2d.getFontMetrics().getStringBounds(text, g2d);
			g2d.drawString(text, (int) (720 / 2 - r2d.getCenterX()), (int) (480 / 2 - r2d.getCenterY()));
		}else {
			img = GuildStatisticsElement.previewElement(event.getSelectedGuild(), type, settings).renderImage(720, 480);
		}
		
		ByteArrayOutputStream bOut = new ByteArrayOutputStream();
		try {
			ImageIO.write(img, "PNG", bOut);
		} catch (IOException e) {
			return WebinterfaceResponse.error("Failed to render image");
		}
		String pngEnc = Base64.getEncoder().encodeToString(bOut.toByteArray());
		JSONObject imgO = new JSONObject();
		imgO.put("image", pngEnc);
		return WebinterfaceResponse.success(imgO);
	}
	
	@WebinterfaceHandler(requestMethod = "getStatisticElements", requireGuild = true, requireFeatures = GraphiteFeature.STATISTICS)
	public static WebinterfaceResponse getStatisticElements(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		List<GuildStatisticsElement> elements = g.getStatisticsConfig().getStatisticsElements();

		JSONObject o = new JSONObject();
		o.put("statistics", new JSONArray(elements.stream().map(r -> r.toWebinterfaceObject()).collect(Collectors.toList())));
		
		return WebinterfaceResponse.success(o);
	}
	
	@WebinterfaceHandler(requestMethod = "removeStatisticElement", requireGuild = true, requireFeatures = GraphiteFeature.STATISTICS)
	public static WebinterfaceResponse removeStatisticElement(WebinterfaceRequestEvent event) {
		GraphiteGuild g = event.getSelectedGuild();
		String elementID = event.getRequestData().getString("element_id");
		if(g.getStatisticsConfig().getStatisticsElementByID(elementID) == null) {
			return WebinterfaceResponse.error("Statistic element doesn't exist");
		}
		g.getStatisticsConfig().deleteOldMessage(elementID);
		g.getStatisticsConfig().removeStatisticsElement(elementID);
		return WebinterfaceResponse.success();
	}
	
}

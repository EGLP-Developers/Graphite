package me.eglp.gv2.util.stats.element;

import java.awt.image.BufferedImage;

import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.guild.GraphiteTextChannel;
import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.webinterface.js.JavaScriptClass;
import me.eglp.gv2.util.webinterface.js.JavaScriptFunction;
import me.eglp.gv2.util.webinterface.js.JavaScriptParameter;
import me.eglp.gv2.util.webinterface.js.JavaScriptValue;
import me.eglp.gv2.util.webinterface.js.WebinterfaceObject;
import me.mrletsplay.mrcore.json.JSONObject;
import net.dv8tion.jda.api.entities.Message;

@JavaScriptClass(name = "StatisticsElement")
public class GuildStatisticsElement implements WebinterfaceObject {

	private GraphiteGuild guild;

	@JavaScriptValue(getter = "getID")
	private String id;

	@JavaScriptValue(getter = "getType")
	private StatisticsElementType type;

	@JavaScriptValue(getter = "getSettings")
	private StatisticsElementSettings settings;

	@JavaScriptValue(getter = "isPreviewMode")
	private boolean previewMode;

	@JavaScriptValue(getter = "getChannelID", setter = "setChannelID")
	private String channelID;

	@JavaScriptValue(getter = "getMessageID")
	private String messageID;

	public GuildStatisticsElement(GraphiteGuild guild, String id, StatisticsElementType type, StatisticsElementSettings settings, String channelID, String messageID) {
		this.guild = guild;
		this.id = id;
		this.type = type;
		this.settings = settings;
		this.channelID = channelID;
		this.messageID = messageID;
	}

	private GuildStatisticsElement(GraphiteGuild guild, StatisticsElementType type, StatisticsElementSettings settings) {
		this.guild = guild;
		this.type = type;
		this.settings = settings;
		this.previewMode = true;
	}

	public GraphiteGuild getGuild() {
		return guild;
	}

	public String getID() {
		return id;
	}

	public StatisticsElementType getType() {
		return type;
	}

	public StatisticsElementSettings getSettings() {
		return settings;
	}

	public String getChannelID() {
		return channelID;
	}

	public String getMessageID() {
		return messageID;
	}

	public boolean isPreviewMode() {
		return previewMode;
	}

	public BufferedImage renderImage(int width, int height) {
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
		type.getRenderer().renderElement(img.createGraphics(), this, width, height);
		return img;
	}

	public void sendMessage(GraphiteTextChannel channel) {
		Message m = channel.sendMessageComplete(getImageLink());
		if(!previewMode) guild.getStatisticsConfig().setMessageID(id, channel.getID(), m.getId());
	}

	private String getImageLink() {
		String id = Graphite.getWebsiteEndpoint().getImageCache().addImage(renderImage(720, 480), 20 * 60 * 1000);
		return Graphite.getBotInfo().getWebsite().getBaseURL() + "/api/image?id=" + id;
	}

	public void updateMessageIfExists() {
		guild.getStatisticsConfig().updateMessageIfExists(id, () -> getImageLink());
	}

	public static GuildStatisticsElement previewElement(GraphiteGuild guild, StatisticsElementType type, StatisticsElementSettings settings) {
		return new GuildStatisticsElement(guild, type, settings);
	}

	@JavaScriptFunction(calling = "createStatisticsElement", returning = "statistic_element", withGuild = true)
	public static void createStatisticsElement(@JavaScriptParameter(name = "type") StatisticsElementType type, @JavaScriptParameter(name = "settings") JSONObject settings, @JavaScriptParameter(name = "channel_id") String channelID) {};

	@JavaScriptFunction(calling = "updateStatisticsElementSettings", withGuild = true)
	public static void updateStatisticsElementSettings(@JavaScriptParameter(name = "element_id") String elementID, @JavaScriptParameter(name = "settings") JSONObject settings, @JavaScriptParameter(name = "channel_id") String channelID) {};

	@JavaScriptFunction(calling = "generateStatisticsPreview", withGuild = true)
	public static void generateStatisticsPreview(@JavaScriptParameter(name = "type") StatisticsElementType type, @JavaScriptParameter(name = "settings") JSONObject settings) {};

	@JavaScriptFunction(calling = "getStatisticElements", returning = "statistics", withGuild = true)
	public static void getStatisticElements() {};

	@JavaScriptFunction(calling = "removeStatisticElement", withGuild = true)
	public static void removeStatisticElement(@JavaScriptParameter(name = "element_id") String elementID) {};

}

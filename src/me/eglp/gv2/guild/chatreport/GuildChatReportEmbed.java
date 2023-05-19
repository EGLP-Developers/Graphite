package me.eglp.gv2.guild.chatreport;

import java.util.List;
import java.util.stream.Collectors;

import me.eglp.gv2.util.webinterface.js.JavaScriptConstructor;
import me.eglp.gv2.util.webinterface.js.JavaScriptValue;
import me.eglp.gv2.util.webinterface.js.WebinterfaceObject;
import me.mrletsplay.mrcore.config.mapper.JSONObjectMapper;
import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.mrcore.json.converter.JSONComplexListType;
import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONConvertible;
import me.mrletsplay.mrcore.json.converter.JSONValue;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class GuildChatReportEmbed implements WebinterfaceObject, JSONConvertible{

	public static final JSONObjectMapper<GuildChatReportEmbed> MAPPER = JSONObjectMapper.create(GuildChatReportEmbed.class);

	@JSONValue
	@JavaScriptValue(getter = "getTitle")
	private String title;

	@JSONValue
	@JavaScriptValue(getter = "getDescription")
	private String description;

	@JSONValue
	@JavaScriptValue(getter = "getAuthorName")
	private String authorName;

	@JSONValue
	@JavaScriptValue(getter = "getAuthorURL")
	private String authorUrl;

	@JSONValue
	@JavaScriptValue(getter = "getAuthorAvatarURL")
	private String authorAvatarUrl;

	@JSONValue
	@JavaScriptValue(getter = "getFooterText")
	private String footerText;

	@JSONValue
	@JavaScriptValue(getter = "getFooterIconURL")
	private String footerIconUrl;

	@JSONValue
	@JavaScriptValue(getter = "getImageURL")
	private String imageUrl;

	@JSONValue
	@JavaScriptValue(getter = "getThumbnail")
	private String thumbnail;

	@JSONValue
	@JavaScriptValue(getter = "getColor")
	private int color;

	@JSONValue
	@JavaScriptValue(getter = "getTimestamp")
	private long timestamp;

	@JSONValue
	@JSONComplexListType(GuildChatReportEmbedField.class)
	@JavaScriptValue(getter = "getFields")
	private List<GuildChatReportEmbedField> fields;

	@JSONConstructor
	@JavaScriptConstructor
	private GuildChatReportEmbed(){}

	public GuildChatReportEmbed(MessageEmbed e) {
		this.title = e.getTitle();
		this.description = e.getDescription();
		this.authorName = e.getAuthor() == null ? null : e.getAuthor().getName();
		this.authorUrl = e.getAuthor() == null ? null : e.getAuthor().getUrl();
		this.authorAvatarUrl = e.getAuthor() == null ? null : e.getAuthor().getProxyIconUrl();
		this.footerText = e.getFooter() == null ? null : e.getFooter().getText();
		this.footerIconUrl = e.getFooter() == null ? null : e.getFooter().getProxyIconUrl();
		this.imageUrl = e.getImage() == null ? null : e.getImage().getUrl();
		this.thumbnail = e.getThumbnail() == null ? null : e.getThumbnail().getProxyUrl();
		this.color = e.getColor() == null ? -1 : e.getColor().getRGB();
		this.timestamp = e.getTimestamp() == null ? 0L : e.getTimestamp().toEpochSecond();
		this.fields = e.getFields().stream().map(f -> new GuildChatReportEmbedField(f)).collect(Collectors.toList());
	}

	public GuildChatReportEmbed(String title, String description, String authorName, String authorUrl, String authorAvatarUrl, String footerText, String footerIconUrl, String imageUrl, String thumbnail, int color, long timestamp, List<GuildChatReportEmbedField> fields) {
		this.title = title;
		this.description = description;
		this.authorName = authorName;
		this.authorUrl = authorUrl;
		this.authorAvatarUrl = authorAvatarUrl;
		this.footerText = footerText;
		this.footerIconUrl = footerIconUrl;
		this.imageUrl = imageUrl;
		this.thumbnail = thumbnail;
		this.color = color;
		this.timestamp = timestamp;
		this.fields = fields;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public String getAuthorName() {
		return authorName;
	}

	public String authorAvatarURL() {
		return authorAvatarUrl;
	}

	public String getAuthorUrl() {
		return authorUrl;
	}

	public String getFooterText() {
		return footerText;
	}

	public String getFooterIconUrl() {
		return footerIconUrl;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public String getThumbnail() {
		return thumbnail;
	}

	public int getColor() {
		return color;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public List<GuildChatReportEmbedField> getFields() {
		return fields;
	}

	@Override
	public void preSerialize(JSONObject object) {}

	@Override
	public void preDeserialize(JSONObject object) {}

}

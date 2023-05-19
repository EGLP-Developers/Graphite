package me.eglp.gv2.guild.chatreport;

import java.util.List;
import java.util.stream.Collectors;

import me.eglp.gv2.util.webinterface.js.JavaScriptConstructor;
import me.eglp.gv2.util.webinterface.js.JavaScriptValue;
import me.eglp.gv2.util.webinterface.js.WebinterfaceObject;
import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.mrcore.json.JSONType;
import me.mrletsplay.mrcore.json.converter.JSONComplexListType;
import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONConvertible;
import me.mrletsplay.mrcore.json.converter.JSONListType;
import me.mrletsplay.mrcore.json.converter.JSONValue;
import net.dv8tion.jda.api.entities.Message;

public class GuildChatReportMessage implements WebinterfaceObject, JSONConvertible{

	@JSONValue
	@JavaScriptValue(getter = "getAttachedFiles")
	private String authorID;

	@JSONValue
	@JavaScriptValue(getter = "getAuthorName")
	private String authorName;

	@JSONValue
	@JavaScriptValue(getter = "getAuthorIsBot")
	private boolean authorIsBot;

	@JSONValue
	@JavaScriptValue(getter = "getAuthorAvatarURL")
	private String authorAvatarURL;

	@JSONValue
	@JavaScriptValue(getter = "getContent")
	private String content;

	@JSONValue
	@JSONListType(JSONType.STRING)
	@JavaScriptValue(getter = "getAttachedFiles")
	private List<String> attachedFiles;

	@JSONValue
	@JavaScriptValue(getter = "getEmbeds")
	@JSONComplexListType(GuildChatReportEmbed.class)
	private List<GuildChatReportEmbed> embeds;

	@JSONConstructor
	@JavaScriptConstructor
	private GuildChatReportMessage() {}

	public GuildChatReportMessage(String authorID, String authorName, boolean authorIsBot, String authorAvatarURL, String content, List<String> attachedFiles, List<GuildChatReportEmbed> embeds) {
		this.authorID = authorID;
		this.authorName = authorName;
		this.authorIsBot = authorIsBot;
		this.authorAvatarURL = authorAvatarURL;
		this.content = content;
		this.attachedFiles = attachedFiles;
		this.embeds = embeds;
	}

	public GuildChatReportMessage(Message m) {
		this.authorID = m.getAuthor().getId();
		this.authorName = m.getAuthor().getName();
		this.authorIsBot = m.getAuthor().isBot();
		this.authorAvatarURL = m.getAuthor().getEffectiveAvatarUrl();
		this.content = m.getContentDisplay();
		this.attachedFiles = m.getAttachments().stream().map(a -> a.getProxyUrl()).collect(Collectors.toList());
		this.embeds = m.getEmbeds().stream().map(e -> new GuildChatReportEmbed(e)).collect(Collectors.toList());
	}

	public String getAuthorID() {
		return authorID;
	}

	public String getAuthorName() {
		return authorName;
	}

	public boolean getAuthorIsBot() {
		return authorIsBot;
	}

	public String getAuthorAvatarURL() {
		return authorAvatarURL;
	}

	public String getContent() {
		return content;
	}

	public List<String> getAttachedFiles() {
		return attachedFiles;
	}

	public List<GuildChatReportEmbed> getEmbeds() {
		return embeds;
	}

	@Override
	public void preSerialize(JSONObject object) {}

	@Override
	public void preDeserialize(JSONObject object) {}

}

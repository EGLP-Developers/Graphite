package me.eglp.gv2.util.base.guild.chatreport;

import me.eglp.gv2.util.webinterface.js.JavaScriptConstructor;
import me.eglp.gv2.util.webinterface.js.JavaScriptValue;
import me.eglp.gv2.util.webinterface.js.WebinterfaceObject;
import me.mrletsplay.mrcore.config.mapper.JSONObjectMapper;
import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONConvertible;
import me.mrletsplay.mrcore.json.converter.JSONValue;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class GuildChatReportEmbedField implements WebinterfaceObject, JSONConvertible{
	
	public static final JSONObjectMapper<GuildChatReportEmbedField> MAPPER = JSONObjectMapper.create(GuildChatReportEmbedField.class);

	@JSONValue
	@JavaScriptValue(getter = "getName")
	private String name;
	
	@JSONValue
	@JavaScriptValue(getter = "getValue")
	private String value;
	
	@JSONValue
	@JavaScriptValue(getter = "isInline")
	private boolean isInline;
	
	@JSONConstructor
	@JavaScriptConstructor
	private GuildChatReportEmbedField() {}
	
	public GuildChatReportEmbedField(String name, String value, boolean inline) {
		this.name = name;
		this.value = value;
		this.isInline = inline;
	}
	
	public GuildChatReportEmbedField(MessageEmbed.Field f) {
		this.name = f.getName();
		this.value = f.getValue();
		this.isInline = f.isInline();
	}
	
	public String getName() {
		return name;
	}
	
	public String getValue() {
		return value;
	}
	
	public boolean isInline() {
		return isInline;
	}
	
	@Override
	public void preSerialize(JSONObject object) {}

	@Override
	public void preDeserialize(JSONObject object) {}

}

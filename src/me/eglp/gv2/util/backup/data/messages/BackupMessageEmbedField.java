package me.eglp.gv2.util.backup.data.messages;

import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONConvertible;
import me.mrletsplay.mrcore.json.converter.JSONValue;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class BackupMessageEmbedField implements JSONConvertible {
	
	@JSONValue
	private String name, value;
	
	@JSONValue
	private boolean inline;
	
	@JSONConstructor
	private BackupMessageEmbedField() {}

	public BackupMessageEmbedField(MessageEmbed.Field field) {
		this.name = field.getName();
		this.value = field.getValue();
		this.inline = field.isInline();
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}

	public boolean isInline() {
		return inline;
	}
	
}

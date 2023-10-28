package me.eglp.gv2.util.backup.data.emoji;

import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.util.backup.IDMappings;
import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONConvertible;
import me.mrletsplay.mrcore.json.converter.JSONValue;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.Emoji.Type;
import net.dv8tion.jda.internal.entities.emoji.UnicodeEmojiImpl;

public class BackupEmojiRef implements JSONConvertible {

	@JSONValue
	private boolean isCustom;

	@JSONValue
	private String value;

	@JSONConstructor
	private BackupEmojiRef() {}

	public BackupEmojiRef(Emoji emoji) {
		this.isCustom = emoji.getType() == Type.CUSTOM;
		this.value = isCustom ? ((CustomEmoji) emoji).getId() : emoji.getName();
	}

	public boolean isCustom() {
		return isCustom;
	}

	public String getValue() {
		return value;
	}

	public Emoji get(GraphiteGuild guild, IDMappings mappings) {
		Emoji e;
		if(isCustom) {
			e = guild.getJDAGuild().getEmojiById(mappings.getNewID(value)); // FIXME: if emoji id was not remapped
		}else {
			e = new UnicodeEmojiImpl(value);
		}
		return e;
	}

}

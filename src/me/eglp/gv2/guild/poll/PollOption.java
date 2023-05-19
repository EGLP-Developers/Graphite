package me.eglp.gv2.guild.poll;

import me.eglp.gv2.util.webinterface.js.WebinterfaceObject;
import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONConvertible;
import me.mrletsplay.mrcore.json.converter.JSONValue;
import net.dv8tion.jda.api.entities.emoji.Emoji;

public class PollOption implements JSONConvertible, WebinterfaceObject {

	@JSONValue
	private String id;

	private Emoji emoji;

	@JSONValue
	private String name;

	@JSONConstructor
	public PollOption() {}

	public PollOption(String id, Emoji emoji, String name) {
		this.id = id;
		this.emoji = emoji;
		this.name = name;
	}

	public String getID() {
		return id;
	}

	public Emoji getEmoji() {
		return emoji;
	}

	public String getName() {
		return name;
	}

	@Override
	public void preSerializeWI(JSONObject object) {
		object.put("optionID", getID());
		object.put("optionName", getName());
	}

	@Override
	public void preSerialize(JSONObject object) {
		object.put("emoji", emoji.getName());
	}

	@Override
	public void preDeserialize(JSONObject object) {
		this.emoji = Emoji.fromUnicode(object.getString("emoji"));
	}

}

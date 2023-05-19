package me.eglp.gv2.guild.automod.badwords;

import java.util.ArrayList;
import java.util.List;

import me.eglp.gv2.guild.automod.AbstractAutoModSettings;
import me.eglp.gv2.util.webinterface.js.JavaScriptClass;
import me.eglp.gv2.util.webinterface.js.JavaScriptConstructor;
import me.eglp.gv2.util.webinterface.js.JavaScriptFunction;
import me.eglp.gv2.util.webinterface.js.JavaScriptValue;
import me.eglp.gv2.util.webinterface.js.WebinterfaceObject;
import me.mrletsplay.mrcore.json.JSONType;
import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONListType;
import me.mrletsplay.mrcore.json.converter.JSONValue;

@JavaScriptClass
public class BadWordsSettings extends AbstractAutoModSettings implements WebinterfaceObject {

	public static final String TYPE = "bad_words";

	@JSONValue
	@JSONListType(JSONType.STRING)
	@JavaScriptValue(getter = "getBadWords", setter = "setBadWords")
	private List<String> badWords;

	@JSONValue
	@JavaScriptValue(getter = "isSubwordMatches", setter = "setSubwordMatches")
	private boolean subwordMatches;

	@JSONValue
	@JavaScriptValue(getter = "isNormalizeText", setter = "setNormalizeText")
	private boolean normalizeText;

	@JSONConstructor
	@JavaScriptConstructor
	public BadWordsSettings() {
		super(TYPE, "Bad Words");
		this.badWords = new ArrayList<>();
		this.subwordMatches = false;
		this.normalizeText = true;
	}

	@Override
	public String getWarnReason() {
		return "Bad word usage";
	}

	public void setBadWords(List<String> badWords) {
		this.badWords = badWords;
	}

	public List<String> getBadWords() {
		return badWords;
	}

	public void setSubwordMatches(boolean subwordMatches) {
		this.subwordMatches = subwordMatches;
	}

	public boolean isSubwordMatches() {
		return subwordMatches;
	}

	public void setNormalizeText(boolean normalizeText) {
		this.normalizeText = normalizeText;
	}

	public boolean isNormalizeText() {
		return normalizeText;
	}

	@JavaScriptFunction(calling = "getBadWordsSettings", returning = "settings", withGuild = true)
	private static void getBadWordsSettings() {}

}

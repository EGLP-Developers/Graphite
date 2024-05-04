package me.eglp.gv2.util.game.impl.rpg.object;

import me.mrletsplay.mrcore.json.converter.JSONPrimitiveStringConvertible;

public enum RPGObjectCategory implements JSONPrimitiveStringConvertible {

	LANDSCAPE("It's a normal {name} that's part of the scenery"),
	OBJECT("It's a pretty boring {name}"),
	QUEST_ITEM("It's a quest item"),
	FOOD("It's edible"),
	WEAPON("It's deadly"),
	;

	private final String defaultItemDescription;

	private RPGObjectCategory(String defaultItemDescription) {
		this.defaultItemDescription = defaultItemDescription;
	}

	public String getDefaultItemDescription() {
		return defaultItemDescription;
	}

	@Override
	public String toJSONPrimitive() {
		return name();
	}

	public static RPGObjectCategory decodePrimitive(String value) {
		return valueOf(value);
	}

}

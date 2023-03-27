package me.eglp.gv2.util.game.impl.rpg.object;

import me.eglp.gv2.util.game.impl.rpg.RPGPlayer;
import me.eglp.gv2.util.lang.LocalizedMessage;
import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONConvertible;
import me.mrletsplay.mrcore.json.converter.JSONValue;

public class RPGObject implements JSONConvertible {

	@JSONValue
	private RPGObjectType type;
	
	@JSONConstructor
	private RPGObject() {}
	
	public RPGObject(RPGObjectType type) {
		this.type = type;
	}
	
	public String getName() {
		return type.getItemName();
	}
	
	public String getDescription() {
		return "*" + getName() + "*\n" + LocalizedMessage.formatMessage(type.getItemDescription() != null ? type.getItemDescription() : type.getCategory().getDefaultItemDescription(), "name", "*" + getName() + "*");
	}
	
	public RPGObjectType getType() {
		return type;
	}
	
	public boolean onPickup(RPGPlayer player) {
		return true;
	}
	
	public boolean onUsed(RPGPlayer player) {
		player.send("You don't know what to use the *" + getName() + "* for");
		return false;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof RPGObject)) return false;
		RPGObject rpg = (RPGObject) obj;
		return rpg.getType().equals(type);
	}
	
}

package me.eglp.gv2.util.game.impl.rpg.object;

import me.eglp.gv2.util.game.impl.rpg.RPGPlayer;
import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONParameter;
import me.mrletsplay.mrcore.json.converter.JSONValue;

public class RPGQuestObject extends RPGObject {

	@JSONValue
	private String userID, username;
	
	@JSONConstructor
	private RPGQuestObject() {
		super(null);
	}

	public RPGQuestObject(@JSONParameter("type") RPGObjectType type) {
		super(type);
	}
	
	public void setOwner(RPGPlayer player) {
		this.userID = player.getUserID();
		this.username = player.getUser().getName();
	}
	
	public String getUserID() {
		return userID;
	}
	
	@Override
	public String getName() {
		return super.getName() + " (" + username + ")";
	}
	
	@Override
	public boolean onPickup(RPGPlayer player) {
		return player.getUserID().equals(userID);
	}
	
}

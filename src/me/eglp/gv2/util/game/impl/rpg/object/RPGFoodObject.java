package me.eglp.gv2.util.game.impl.rpg.object;

import me.eglp.gv2.util.game.impl.rpg.RPGPlayer;
import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONValue;

public class RPGFoodObject extends RPGObject {

	@JSONValue
	private int regenerationAmount;
	
	@JSONConstructor
	private RPGFoodObject() {
		super(null);
	}
	
	public RPGFoodObject(RPGObjectType type, int regenerationAmount) {
		super(type);
		this.regenerationAmount = regenerationAmount;
	}
	
	public boolean onUsed(RPGPlayer player) {
		player.addHealth(regenerationAmount);
		player.send("You consume the *" + getName() + "*, instantly recovering *" + regenerationAmount + "* HP");
		return true;
	}
	
}

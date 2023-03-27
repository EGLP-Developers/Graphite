package me.eglp.gv2.util.game.impl.rpg.enemy;

import me.eglp.gv2.util.game.impl.rpg.RPGPlayer;
import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONValue;

public class RPGQuestEnemy extends RPGEnemy {

	@JSONValue
	private String userID, username;
	
	@JSONConstructor
	private RPGQuestEnemy() {}
	
	public RPGQuestEnemy(RPGEnemyType type, int x, int y) {
		super(type, x, y);
	}
	
	public void setOwner(RPGPlayer player) {
		this.userID = player.getUserID();
		this.username = player.getUser().getName();
	}
	
	public int onAttack(RPGPlayer player) {
		if(!player.getUserID().equals(userID)) return -1;
		return super.onAttack(player);
	}
	
	@Override
	public String getName() {
		return super.getName() + " (" + username + ")";
	}
	
	public boolean canBeAttacked(RPGPlayer player) {
		return player.getUserID().equals(userID);
	}
	
	public void onAttacked(RPGPlayer player, int damage) {
		reduceHealth(player, damage);
	}
	
}

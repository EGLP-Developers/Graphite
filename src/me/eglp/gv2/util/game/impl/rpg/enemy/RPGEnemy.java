package me.eglp.gv2.util.game.impl.rpg.enemy;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import me.eglp.gv2.util.game.impl.rpg.RPG;
import me.eglp.gv2.util.game.impl.rpg.RPGLocation;
import me.eglp.gv2.util.game.impl.rpg.RPGPlayer;
import me.eglp.gv2.util.game.impl.rpg.object.RPGObjectType;
import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONConvertible;
import me.mrletsplay.mrcore.json.converter.JSONValue;

public class RPGEnemy implements JSONConvertible {

	@JSONValue
	private String uid;
	
	@JSONValue
	private RPGEnemyType type;
	
	@JSONValue
	private int x, y, health;
	
	@JSONConstructor
	protected RPGEnemy() {}
	
	public RPGEnemy(RPGEnemyType type, int x, int y) {
		this.uid = UUID.randomUUID().toString();
		this.type = type;
		this.x = x;
		this.y = y;
		this.health = type.getMaxHealth();
	}
	
	public String getUID() {
		return uid;
	}
	
	public RPGEnemyType getType() {
		return type;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public boolean isDead() {
		return health == 0;
	}
	
	public void addHealth(int amount) {
		health = Math.min(health + amount, type.getMaxHealth());
	}
	
	public void reduceHealth(RPGPlayer player, int amount) {
		health = Math.max(health - amount, 0);
		if(health == 0) {
			List<RPGObjectType> drops = type.getDrops();
			RPGLocation l = RPG.INSTANCE.getMap().getLocation(x, y);
			player.send("The *" + getName() + "* faints, dropping" + (drops.isEmpty() ? " nothing" : (": " + drops.stream().map(RPGObjectType::getItemName).collect(Collectors.joining(", ")))));
			l.removeEnemy(this);
			drops.forEach(d -> l.addObject(d.createObject()));
		}
	}
	
	public int getHealth() {
		return health;
	}
	
	public String getName() {
		return type.getEnemyName();
	}
	
	public int onAttack(RPGPlayer player) {
		int ad = type.getMinAttackDamage();
		int diff = type.getMaxAttackDamage() - type.getMinAttackDamage();
		if(diff != 0) ad += RPG.INSTANCE.getRandom().nextInt(diff);
		player.reduceHealth(ad);
		return ad;
	}
	
	public boolean canBeAttacked(RPGPlayer player) {
		return true;
	}
	
	public void onAttacked(RPGPlayer player, int damage) {
		reduceHealth(player, damage);
	}
	
}

package me.eglp.gv2.util.game.impl.rpg.quest;

import me.eglp.gv2.util.game.impl.rpg.RPG;
import me.eglp.gv2.util.game.impl.rpg.RPGLocation;
import me.eglp.gv2.util.game.impl.rpg.RPGMap;
import me.eglp.gv2.util.game.impl.rpg.RPGPlayer;
import me.eglp.gv2.util.game.impl.rpg.enemy.RPGEnemyType;
import me.eglp.gv2.util.game.impl.rpg.enemy.RPGQuestEnemy;
import me.eglp.gv2.util.game.impl.rpg.npc.RPGNPC;
import me.eglp.gv2.util.game.impl.rpg.object.RPGObject;
import me.eglp.gv2.util.game.impl.rpg.object.RPGObjectCategory;
import me.eglp.gv2.util.game.impl.rpg.object.RPGObjectType;
import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONValue;

public class RPGKillEnemyQuest implements RPGQuest {

	private RPGNPC npc;
	
	@JSONValue
	private String enemyUID, enemyName;
	
	@JSONValue
	private int enemyX, enemyY;
	
	@JSONConstructor
	private RPGKillEnemyQuest() {}
	
	public RPGKillEnemyQuest(RPGNPC npc, RPGPlayer player, RPGEnemyType type) {
		this.npc = npc;
		int[] sp = RPGMap.getRandomSpotNear(npc.getX(), npc.getY(), 5);
		this.enemyX = sp[0];
		this.enemyY = sp[1];
		RPGLocation l = RPG.INSTANCE.getMap().getLocation(enemyX, enemyY);
		RPGQuestEnemy qo = (RPGQuestEnemy) type.createEnemy(enemyX, enemyY);
		qo.setOwner(player);
		l.addEnemy(qo);
		this.enemyUID = qo.getUID();
		this.enemyName = qo.getType().getEnemyName();
	}
	
	@Override
	public void setNPC(RPGNPC npc) {
		this.npc = npc;
	}
	
	@Override
	public String getNPCText() {
		return "There seems to be a *" + enemyName + "* causing trouble somewhere around (" + enemyX + "/" + enemyY + "). Could you please kill it for me?";
	}
	
	@Override
	public String getDescription() {
		return "Kill the *" + enemyName + "* at (" + enemyX + "/" + enemyY + ") and report back to *" + npc.getName() + "* (" + npc.getX() + "/" + npc.getY() + ")";
	}

	@Override
	public void questStarted(RPGPlayer player) {
		if(!player.hasObjectCategory(RPGObjectCategory.WEAPON)) {
			player.send("*" + npc.getName() +"*\nI see you don't have a sword. Take mine, i don't really need it anyway");
			RPGObject sw = RPGObjectType.RUSTY_SWORD.createObject();
			if(player.isInventoryFull()) {
				player.getLocation().addObject(sw);
			}else {
				player.getInventory().add(sw);
			}
		}
	}
	
	@Override
	public boolean checkQuestFinished(RPGPlayer player) {
		return RPG.INSTANCE.getMap().getLocation(enemyX, enemyY).getEnemyByUID(enemyUID) == null;
	}

	@Override
	public void questFinished(RPGPlayer player) {
		player.send("*" + npc.getName() + "*\nThanks for killing the *" + enemyName + "*. Here's 100$ for your efforts!");
		player.addMoney(100);
	}
	
}

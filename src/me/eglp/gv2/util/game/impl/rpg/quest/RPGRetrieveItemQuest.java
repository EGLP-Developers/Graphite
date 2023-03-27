package me.eglp.gv2.util.game.impl.rpg.quest;

import me.eglp.gv2.util.game.impl.rpg.RPG;
import me.eglp.gv2.util.game.impl.rpg.RPGLocation;
import me.eglp.gv2.util.game.impl.rpg.RPGMap;
import me.eglp.gv2.util.game.impl.rpg.RPGPlayer;
import me.eglp.gv2.util.game.impl.rpg.npc.RPGNPC;
import me.eglp.gv2.util.game.impl.rpg.object.RPGObject;
import me.eglp.gv2.util.game.impl.rpg.object.RPGObjectType;
import me.eglp.gv2.util.game.impl.rpg.object.RPGQuestObject;
import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONValue;

public class RPGRetrieveItemQuest implements RPGQuest {

	private RPGNPC npc;

	@JSONValue
	private RPGObjectType type;
	
	@JSONValue
	private int itemX, itemY;
	
	@JSONConstructor
	private RPGRetrieveItemQuest() {}
	
	public RPGRetrieveItemQuest(RPGNPC npc, RPGPlayer player, RPGObjectType type) {
		this.npc = npc;
		this.type = type;
		int[] pos = RPGMap.getRandomSpotNear(npc.getX(), npc.getY(), 5);
		this.itemX = pos[0];
		this.itemY = pos[1];
		RPGLocation l = RPG.INSTANCE.getMap().getLocation(itemX, itemY);
		RPGQuestObject qo = (RPGQuestObject) type.createObject();
		qo.setOwner(player);
		l.addObject(qo);
	}
	
	@Override
	public void setNPC(RPGNPC npc) {
		this.npc = npc;
	}
	
	@Override
	public String getNPCText() {
		return "I seem to have lost my " + type.getItemName() + " somewhere around (" + itemX + "/" + itemY + "). Could you please find it for me?";
	}
	
	@Override
	public String getDescription() {
		return "Find the *" + type.getItemName() + "* at (" + itemX + "/" + itemY + ") and bring it to *" + npc.getName() + "* (" + npc.getX() + "/" + npc.getY() + ")";
	}

	@Override
	public void questStarted(RPGPlayer player) {
		
	}
	
	@Override
	public boolean checkQuestFinished(RPGPlayer player) {
		RPGObject o = player.getInventory().stream().filter(i -> i.getType().equals(type)).findFirst().orElse(null);
		if(o == null) return false;
		player.getInventory().remove(o);
		return true;
	}

	@Override
	public void questFinished(RPGPlayer player) {
		player.send("*" + npc.getName() + "*\nThanks for brining me my *" + type.getItemName() + "* back. Here's 100$ for your efforts!");
		player.addMoney(100);
	}
	
}

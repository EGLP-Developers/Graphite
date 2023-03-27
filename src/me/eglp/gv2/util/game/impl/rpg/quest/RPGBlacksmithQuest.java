package me.eglp.gv2.util.game.impl.rpg.quest;

import me.eglp.gv2.util.game.impl.rpg.RPGPlayer;
import me.eglp.gv2.util.game.impl.rpg.npc.RPGNPC;
import me.eglp.gv2.util.game.impl.rpg.object.RPGObject;
import me.eglp.gv2.util.game.impl.rpg.object.RPGObjectType;
import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONValue;

public class RPGBlacksmithQuest implements RPGQuest {

	private RPGNPC npc;

	@JSONValue
	private RPGObjectType type, result;
	
	@JSONValue
	private int moneyAmount;
	
	@JSONConstructor
	private RPGBlacksmithQuest() {}
	
	public RPGBlacksmithQuest(RPGNPC npc, RPGPlayer player, RPGObjectType type, RPGObjectType result, int moneyAmount) {
		this.npc = npc;
		this.type = type;
		this.result = result;
		this.moneyAmount = moneyAmount;
	}
	
	@Override
	public void setNPC(RPGNPC npc) {
		this.npc = npc;
	}
	
	@Override
	public String getNPCText() {
		return "Here, I'll give you my old *" + type.getItemName() + "* but in order for me to repair it, I'll need some compensation. Let's say " + moneyAmount + "$, is that fine? If so please bring both of these things back to me";
	}
	
	@Override
	public String getDescription() {
		return "Bring the *" + type.getItemName() + "* along with " + moneyAmount + "$ to " + npc.getName() + "* (" + npc.getX() + "/" + npc.getY() + ")";
	}

	@Override
	public void questStarted(RPGPlayer player) {
		player.getInventory().add(type.createObject());
	}
	
	@Override
	public boolean checkQuestFinished(RPGPlayer player) {
		RPGObject o = player.getInventory().stream().filter(i -> i.getType().equals(type)).findFirst().orElse(null);
		if(o == null || player.getMoney() < moneyAmount) return false;
		player.getInventory().remove(o);
		player.setMoney(player.getMoney() - moneyAmount);
		return true;
	}

	@Override
	public void questFinished(RPGPlayer player) {
		RPGObject o = result.createObject();
		player.send("*" + npc.getName() + "*\nSo you've decided to accept my offer? Just wait a second, i'll be finished shortly...\n(a few minutes later) Okay, here you go, enjoy your *" + o.getName() + "*!");
		player.getInventory().add(o);
	}
	
}

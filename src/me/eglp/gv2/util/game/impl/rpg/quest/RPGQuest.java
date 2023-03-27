package me.eglp.gv2.util.game.impl.rpg.quest;

import me.eglp.gv2.util.game.impl.rpg.RPGPlayer;
import me.eglp.gv2.util.game.impl.rpg.npc.RPGNPC;
import me.mrletsplay.mrcore.json.converter.JSONConvertible;

public interface RPGQuest extends JSONConvertible {
	
	public void setNPC(RPGNPC npc);
	
	public String getNPCText();
	
	public String getDescription();
	
	public void questStarted(RPGPlayer player);
	
	public boolean checkQuestFinished(RPGPlayer player);
	
	public void questFinished(RPGPlayer player);

}

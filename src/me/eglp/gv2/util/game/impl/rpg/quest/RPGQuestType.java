package me.eglp.gv2.util.game.impl.rpg.quest;

import java.util.function.BiFunction;

import me.eglp.gv2.util.game.impl.rpg.RPGPlayer;
import me.eglp.gv2.util.game.impl.rpg.enemy.RPGEnemyType;
import me.eglp.gv2.util.game.impl.rpg.npc.RPGNPC;
import me.eglp.gv2.util.game.impl.rpg.object.RPGObjectType;
import me.mrletsplay.mrcore.json.converter.JSONPrimitiveStringConvertible;

public enum RPGQuestType implements JSONPrimitiveStringConvertible {

	RETRIEVE_MAGIC_STONE((npc, p) -> new RPGRetrieveItemQuest(npc, p, RPGObjectType.MAGIC_STONE)),
	KILL_GREATER_BEAR((npc, p) -> new RPGKillEnemyQuest(npc, p, RPGEnemyType.QUEST_BEAR)),
	BLACKSMITH_SWORD((npc, p) -> new RPGBlacksmithQuest(npc, p, RPGObjectType.DULL_IRON_SWORD, RPGObjectType.IRON_SWORD, 100)),
	;
	
	private final BiFunction<RPGNPC, RPGPlayer, ? extends RPGQuest> questCreator;
	
	private RPGQuestType(BiFunction<RPGNPC, RPGPlayer, ? extends RPGQuest> questCreator) {
		this.questCreator = questCreator;
	}
	
	public RPGQuest createQuest(RPGNPC npc, RPGPlayer player) {
		return questCreator.apply(npc, player);
	}
	
	@Override
	public String toJSONPrimitive() {
		return name();
	}
	
	public static RPGQuestType decodePrimitive(Object value) {
		return valueOf((String) value);
	}
	
}

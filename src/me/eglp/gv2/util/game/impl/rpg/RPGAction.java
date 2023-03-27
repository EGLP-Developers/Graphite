package me.eglp.gv2.util.game.impl.rpg;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum RPGAction {
	
	MOVE("move", "walk", "explore", "wander", "go"),
	INSPECT("inspect", "look at", "examine"),
	INTERACT("interact with", "interact", "use"),
	TALK_TO("talk to", "speak to"),
	TAKE("take", "pick up", "collect", "get"),
	LOOK_AROUND("look around", "la"),
	MAP("map"),
	EQUIP("equip", "wield"),
	DEQUIP("dequip", "unequip", "unwield"),
	INVENTORY("inventory", "inv"),
	QUESTS("quests", "tasks"),
	DROP("drop"),
	HELP("help", "?"),
	ATTACK("attack", "hit", "punch", "kick"),
	SAY("say"),
	;
	
	private List<String> triggers;
	
	private RPGAction(String... triggers) {
		this.triggers = Arrays.asList(triggers);
	}
	
	public List<String> getTriggers() {
		return triggers;
	}
	
	public String getStartingTrigger(String msg) {
		return triggers.stream().filter(t -> msg.startsWith(t)).sorted(Collections.reverseOrder()).findFirst().orElse(null);
	}

}

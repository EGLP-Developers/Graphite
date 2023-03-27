package me.eglp.gv2.util.game.impl.rpg.npc;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import me.eglp.gv2.util.game.impl.rpg.RPG;
import me.eglp.gv2.util.game.impl.rpg.RPGPlayer;
import me.eglp.gv2.util.game.impl.rpg.dialog.BooleanCallbackDialog;
import me.eglp.gv2.util.game.impl.rpg.quest.RPGQuest;
import me.eglp.gv2.util.game.impl.rpg.quest.RPGQuestType;
import me.mrletsplay.mrcore.json.converter.JSONPrimitiveStringConvertible;
import me.mrletsplay.mrcore.misc.Probability;
import me.mrletsplay.mrcore.misc.Probability.ProbabilityElement;

public enum RPGNPCType implements JSONPrimitiveStringConvertible {

	QUEST_VILLAGER("Villager (!)", null,
			new ProbabilityElement<>(RPGQuestType.RETRIEVE_MAGIC_STONE, 50),
			new ProbabilityElement<>(RPGQuestType.KILL_GREATER_BEAR, 50)),
	VILLAGER("Villager", (npc, p) -> {
		p.send("*" + npc.getName() + "*\nHi, nice to meet you");
	}),
	BLACKSMITH("Blacksmith", null,
			new ProbabilityElement<>(RPGQuestType.BLACKSMITH_SWORD, 100)),
	;
	
	private final String title;
	private final BiConsumer<RPGNPC, RPGPlayer> onInteract;
	private final BiFunction<RPGNPCType, int[], ? extends RPGNPC> npcCreator;
	private final List<ProbabilityElement<RPGQuestType>> dropProbabilities;
	
	@SafeVarargs
	private RPGNPCType(String title, BiConsumer<RPGNPC, RPGPlayer> onInteract, BiFunction<RPGNPCType, int[], ? extends RPGNPC> npcCreator, ProbabilityElement<RPGQuestType>... dropProbabilities) {
		this.title = title;
		this.onInteract = onInteract;
		this.npcCreator = npcCreator;
		this.dropProbabilities = Arrays.asList(dropProbabilities);
	}
	
	@SafeVarargs
	private RPGNPCType(String title, BiConsumer<RPGNPC, RPGPlayer> onInteract, ProbabilityElement<RPGQuestType>... dropProbabilities) {
		this(title, onInteract, null, dropProbabilities);
	}
	
	public RPGNPC createNPC(int x, int y) {
		return npcCreator != null ? npcCreator.apply(this, new int[] {x, y}) : new RPGNPC(this, x, y);
	}
	
	public void onInteract(RPGNPC npc, RPGPlayer player) {
		if(onInteract != null) {
			onInteract.accept(npc, player);
			return;
		}

		RPGQuest q = npc.getQuest(player);
		if(q == null) {
			RPGQuest qs = npc.generateNewQuest(player);
			player.send("*" + npc.getName() + "*\n" + qs.getNPCText());
			player.setDialogAction(new BooleanCallbackDialog(player, s -> {
				if(s) {
					player.send("You accepted the quest");
					npc.setQuest(player, qs);
					player.getActiveQuests().add(qs);
					qs.questStarted(player);
				}else {
					player.send("You declined the quest");
				}
			}, "Accept", "Decline"));
			return;
		}
		if(q.checkQuestFinished(player)) {
			q.questFinished(player);
			npc.removeQuest(player);
			player.getActiveQuests().remove(q);
			return;
		}
		player.send("*" + npc.getName() + "*\n" + q.getNPCText());
	}
	
	public String getTitle() {
		return title;
	}
	
	public RPGQuest generateNewQuest(RPGNPC npc, RPGPlayer player) {
		return Probability.choose(dropProbabilities, RPG.INSTANCE.getRandom()).getElement().createQuest(npc, player);
	}
	
	@Override
	public String toJSONPrimitive() {
		return name();
	}
	
	public static RPGNPCType decodePrimitive(Object value) {
		return valueOf((String) value);
	}
	
}

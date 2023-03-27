package me.eglp.gv2.util.game.impl.rpg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import me.eglp.gv2.util.emote.JDAEmote;
import me.eglp.gv2.util.game.impl.rpg.enemy.RPGEnemy;
import me.eglp.gv2.util.game.impl.rpg.enemy.RPGEnemyType;
import me.eglp.gv2.util.game.impl.rpg.npc.RPGNPC;
import me.eglp.gv2.util.game.impl.rpg.npc.RPGNPCType;
import me.eglp.gv2.util.game.impl.rpg.object.RPGObject;
import me.eglp.gv2.util.game.impl.rpg.object.RPGObjectType;
import me.mrletsplay.mrcore.json.converter.JSONComplexListType;
import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONConvertible;
import me.mrletsplay.mrcore.json.converter.JSONValue;
import me.mrletsplay.mrcore.misc.Probability;
import me.mrletsplay.mrcore.misc.Probability.ProbabilityElement;

public class RPGLocation implements JSONConvertible {
	
	private static final List<ProbabilityElement<RPGLocationType>> PROBABILITIES = Arrays.asList(
			new ProbabilityElement<>(RPGLocationType.FOREST, 25),
			new ProbabilityElement<>(RPGLocationType.VILLAGE, 25),
			new ProbabilityElement<>(RPGLocationType.PLAINS, 50),
			new ProbabilityElement<>(RPGLocationType.MOUNTAINS, 25),
			new ProbabilityElement<>(RPGLocationType.BEACH, 25),
			new ProbabilityElement<>(RPGLocationType.DESERT, 25)
		);
	
	@JSONValue
	private int x, y;
	
	@JSONValue
	private RPGLocationType type;
	
	@JSONValue
	@JSONComplexListType(RPGObject.class)
	private List<RPGObject> objects;
	
	@JSONValue
	@JSONComplexListType(RPGNPC.class)
	private List<RPGNPC> npcs;
	
	@JSONValue
	@JSONComplexListType(RPGEnemy.class)
	private List<RPGEnemy> enemies;
	
	@JSONConstructor
	private RPGLocation() {}
	
	public RPGLocation(int x, int y, RPGLocationType t) {
		this.x = x;
		this.y = y;
		this.type = t;
		this.objects = new ArrayList<>();
		this.npcs = new ArrayList<>();
		this.enemies = new ArrayList<>();
	}
	
	public RPGObject getObject(String name) {
		return find(objects, name, RPGObject::getName);
	}
	
	public static <T> T find(List<T> list, String name, Function<T, String> nmFnc) {
		return list.stream()
				.filter(o -> nmFnc.apply(o).toLowerCase().startsWith(name.toLowerCase()))
				.sorted(Comparator
						.comparingInt((T o) -> matchingRegionLength(name, nmFnc.apply(o)))
						.reversed())
				.findFirst().orElse(null);
	}
	
	private static int matchingRegionLength(String a, String b) {
		int i = 0;
		while(i < a.length() && i < b.length() && Character.toLowerCase(a.charAt(i)) == Character.toLowerCase(b.charAt(i))) i++;
		return i;
	}
	
	public void addObject(RPGObject o) {
		objects.add(o);
	}
	
	public void removeObject(RPGObject o) {
		objects.remove(o);
	}
	
	public List<RPGObject> getObjects() {
		return objects;
	}
	
	public RPGNPC getNPC(String name) {
		return find(npcs, name, RPGNPC::getName);
	}
	
	public List<RPGNPC> getNPCs() {
		return npcs;
	}
	
	public List<RPGPlayer> getPlayers() {
		return RPG.INSTANCE.getPlayers().stream().filter(p -> p.getX() == x && p.getY() == y).collect(Collectors.toList());
	}
	
	public RPGEnemy getEnemy(String name) {
		return find(enemies, name, RPGEnemy::getName);
	}
	
	public RPGEnemy getEnemyByUID(String uid) {
		return enemies.stream().filter(o -> o.getUID().equalsIgnoreCase(uid)).findFirst().orElse(null);
	}
	
	public void addEnemy(RPGEnemy o) {
		enemies.add(o);
	}
	
	public void removeEnemy(RPGEnemy o) {
		enemies.remove(o);
	}
	
	public List<RPGEnemy> getEnemies() {
		return enemies;
	}
	
	public RPGLocationType getType() {
		return type;
	}
	
	public JDAEmote getLocationIcon(RPGPlayer player) {
		if(npcs.stream().anyMatch(n -> n.getQuest(player) != null)) return JDAEmote.EXCLAMATION;
		if(npcs.stream().anyMatch(n -> n.getType().equals(RPGNPCType.QUEST_VILLAGER))) return JDAEmote.SMALL_ORANGE_DIAMOND;
		JDAEmote e = JDAEmote.WHITE_LARGE_SQUARE;
		switch(type) {
			case FOREST: e = JDAEmote.EVERGREEN_TREE; break;
			case VILLAGE: e = JDAEmote.HOMES; break;
			case PLAINS: e = JDAEmote.DECIDUOUS_TREE; break;
			case MOUNTAINS: e = JDAEmote.MOUNTAIN_SNOW; break;
			case BEACH: e = JDAEmote.BEACH; break;
			case DESERT: e = JDAEmote.DESERT; break;
		}
		return e;
	}
	
	public static RPGLocation generateNew(int x, int y) {
		RPGLocationType t = Probability.chooseValue(PROBABILITIES, RPG.INSTANCE.getRandom());
		RPGLocation l = new RPGLocation(x, y, t);
		switch(t) {
			case FOREST:
			{
				l.objects.add(RPGObjectType.STONE.createObject());
				break;
			}
			case VILLAGE:
			{
				if(RPG.INSTANCE.getRandom().nextBoolean()) l.npcs.add(RPGNPCType.QUEST_VILLAGER.createNPC(x, y));
				l.npcs.add(RPGNPCType.BLACKSMITH.createNPC(x, y));
				for(int i = 0; i < RPG.INSTANCE.getRandom().nextInt(5) + 1; i++) {
					l.npcs.add(RPGNPCType.VILLAGER.createNPC(x, y));
				}
				break;
			}
			case PLAINS:
			{
				
				break;
			}
			case MOUNTAINS:
			{
				
				break;
			}
			case BEACH:
			{
				
				break;
			}
			case DESERT:
			{
				
				break;
			}
			default:
				break;
		}
		RPGEnemyType e = Probability.chooseValue(t.getEnemyProbabilities(), RPG.INSTANCE.getRandom());
		if(e != null) l.addEnemy(e.createEnemy(x, y));
		return l;
	}
	
}

package me.eglp.gv2.util.game.impl.rpg;

import java.util.Arrays;
import java.util.List;

import me.eglp.gv2.util.game.impl.rpg.enemy.RPGEnemyType;
import me.mrletsplay.mrcore.json.converter.JSONPrimitiveStringConvertible;
import me.mrletsplay.mrcore.misc.Probability.ProbabilityElement;

public enum RPGLocationType implements JSONPrimitiveStringConvertible {

	FOREST("Forest", "You are standing in a forest near ({x}/{y})",
			new ProbabilityElement<>(RPGEnemyType.BEAR, 100)),
	VILLAGE("Village", "You are standing in a village near ({x}/{y})"),
	PLAINS("Plains", "You are standing in a plain field near ({x}/{y})"),
	MOUNTAINS("Mountains", "You are standing in the mountains near ({x}/{y})"),
	BEACH("Beach", "You are standing on a beach near ({x}/{y})",
			new ProbabilityElement<>(RPGEnemyType.CRAB, 100)),
	DESERT("Desert", "You are standing in a desert near ({x}/{y})",
			new ProbabilityElement<>(RPGEnemyType.SCORPION, 100)),
	;

	private final String name, locationDescription;
	private List<ProbabilityElement<RPGEnemyType>> enemyProbabilities;

	@SafeVarargs
	private RPGLocationType(String name, String locationDescription, ProbabilityElement<RPGEnemyType>... enemyProbabilities) {
		this.name = name;
		this.locationDescription = locationDescription;
		this.enemyProbabilities = Arrays.asList(enemyProbabilities);
	}

	public String getName() {
		return name;
	}

	public String getLocationDescription() {
		return locationDescription;
	}

	public List<ProbabilityElement<RPGEnemyType>> getEnemyProbabilities() {
		return enemyProbabilities;
	}

	@Override
	public String toJSONPrimitive() {
		return name();
	}

	public static RPGLocationType decodePrimitive(String value) {
		return valueOf(value);
	}

}

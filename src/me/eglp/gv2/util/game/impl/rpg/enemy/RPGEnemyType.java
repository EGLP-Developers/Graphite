package me.eglp.gv2.util.game.impl.rpg.enemy;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

import me.eglp.gv2.util.game.impl.rpg.RPG;
import me.eglp.gv2.util.game.impl.rpg.object.RPGObjectType;
import me.mrletsplay.mrcore.json.converter.JSONPrimitiveStringConvertible;
import me.mrletsplay.mrcore.misc.Probability;
import me.mrletsplay.mrcore.misc.Probability.ProbabilityElement;

public enum RPGEnemyType implements JSONPrimitiveStringConvertible {

	BEAR("Bear", 40, 2, 5,
			new ProbabilityElement<>(RPGObjectType.MEAT, 100),
			new ProbabilityElement<>(RPGObjectType.MEAT, 50),
			new ProbabilityElement<>(RPGObjectType.MEAT, 25)),
	QUEST_BEAR("Great Bear", 50, 5, 10, (t, xy) -> new RPGQuestEnemy(t, xy[0], xy[1]),
			new ProbabilityElement<>(RPGObjectType.MEAT, 100),
			new ProbabilityElement<>(RPGObjectType.MEAT, 100),
			new ProbabilityElement<>(RPGObjectType.MEAT, 75)),
	SCORPION("Scorpion", 20, 3, 5,
			new ProbabilityElement<>(RPGObjectType.STONE, 40)),
	CRAB("Crab", 20, 3, 5,
			new ProbabilityElement<>(RPGObjectType.CRAB_RAVE_CD, 1)),
	WOLF("Wolf", 30, 2, 5,
			new ProbabilityElement<>(RPGObjectType.MEAT, 100),
			new ProbabilityElement<>(RPGObjectType.MEAT, 25)),
	;

	private final String enemyName;
	private final int maxHealth, minAttackDamage, maxAttackDamage;
	private final BiFunction<RPGEnemyType, int[], ? extends RPGEnemy> enemyCreator;
	private final List<ProbabilityElement<RPGObjectType>> dropProbabilities;

	@SafeVarargs
	private RPGEnemyType(String enemyName, int maxHealth, int minAD, int maxAD, BiFunction<RPGEnemyType, int[], ? extends RPGEnemy> enemyCreator, ProbabilityElement<RPGObjectType>... dropProbabilities) {
		this.enemyName = enemyName;
		this.maxHealth = maxHealth;
		this.minAttackDamage = minAD;
		this.maxAttackDamage = maxAD;
		this.enemyCreator = enemyCreator;
		this.dropProbabilities = Arrays.asList(dropProbabilities);
	}

	@SafeVarargs
	private RPGEnemyType(String enemyName, int maxHealth, int minAD, int maxAD, ProbabilityElement<RPGObjectType>... dropProbabilities) {
		this(enemyName, maxHealth, minAD, maxAD, null, dropProbabilities);
	}

	public String getEnemyName() {
		return enemyName;
	}

	public int getMaxHealth() {
		return maxHealth;
	}

	public int getMinAttackDamage() {
		return minAttackDamage;
	}

	public int getMaxAttackDamage() {
		return maxAttackDamage;
	}

	public RPGEnemy createEnemy(int x, int y) {
		return enemyCreator != null ? enemyCreator.apply(this, new int[] {x, y}) : new RPGEnemy(this, x, y);
	}

	public List<RPGObjectType> getDrops() {
		return Probability.chooseMultipleValues(dropProbabilities, 100, RPG.INSTANCE.getRandom());
	}

	@Override
	public String toJSONPrimitive() {
		return name();
	}

	public static RPGEnemyType decodePrimitive(String value) {
		return valueOf(value);
	}

}

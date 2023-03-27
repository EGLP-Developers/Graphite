package me.eglp.gv2.util.game.impl.rpg.object;

import java.util.function.Function;

import me.eglp.gv2.util.game.impl.rpg.RPG;
import me.mrletsplay.mrcore.json.converter.JSONPrimitiveStringConvertible;

public enum RPGObjectType implements JSONPrimitiveStringConvertible {

	STONE(RPGObjectCategory.LANDSCAPE, "Stone", 3, 5),
	MAGIC_STONE(RPGObjectCategory.QUEST_ITEM, "Magic Stone", "Looks like a normal Stone on the outside, but it seems to be emitting some kind of magic aura around it.\nThe words \"Property of Mr. Testerinsky\" are enscribed on the bottom", 3, 8, RPGQuestObject::new),
	MEAT(RPGObjectCategory.FOOD, "Meat", "A bit raw, but it's still better than nothing", 2, 5, t -> new RPGFoodObject(t, 10)),
	COOKED_MEAT(RPGObjectCategory.FOOD, "Cooked Meat", "Tasty!", 2, 5, t -> new RPGFoodObject(t, 25)),
	CRAB_RAVE_CD(RPGObjectCategory.OBJECT, "Crab Rave CD", "On the cover it says: \"Visit https://www.youtube.com/watch?v=LDU_Txk06tM for the original\"", 2, 5),
	RUSTY_SWORD(RPGObjectCategory.WEAPON, "Rusty Sword", "A bit rusty, but it can still deal a good hit", 5, 10),
	DULL_IRON_SWORD(RPGObjectCategory.WEAPON, "Dull Iron Sword", "Seems too dull to be of any use in combat, try bringing it to a blacksmith", 3, 7),
	IRON_SWORD(RPGObjectCategory.WEAPON, "Iron Sword", "A sharpened iron sword that can deal quite a bit of damage", 10, 15),
	;
	
	private final RPGObjectCategory category;
	private final String itemName, itemDescription;
	private final int minAttackDamage, maxAttackDamage;
	private final Function<RPGObjectType, ? extends RPGObject> itemCreator;
	
	private RPGObjectType(RPGObjectCategory category, String itemName, String itemDescription, int minAD, int maxAD, Function<RPGObjectType, ? extends RPGObject> itemCreator) {
		this.category = category;
		this.itemName = itemName;
		this.itemDescription = itemDescription;
		this.minAttackDamage = minAD;
		this.maxAttackDamage = maxAD;
		this.itemCreator = itemCreator;
	}
	
	private RPGObjectType(RPGObjectCategory category, String itemName, String itemDescription, int minAD, int maxAD) {
		this(category, itemName, itemDescription, minAD, maxAD, null);
	}
	
	private RPGObjectType(RPGObjectCategory category, String itemName, int minAD, int maxAD) {
		this(category, itemName, null, minAD, maxAD);
	}
	
	public RPGObjectCategory getCategory() {
		return category;
	}
	
	public String getItemName() {
		return itemName;
	}
	
	public String getItemDescription() {
		return itemDescription;
	}
	
	public int getAttackDamage() {
		int ad = minAttackDamage;
		int diff = maxAttackDamage - minAttackDamage;
		if(diff != 0) ad += RPG.INSTANCE.getRandom().nextInt(diff);
		return ad;
	}
	
	public int getMinAttackDamage() {
		return minAttackDamage;
	}
	
	public int getMaxAttackDamage() {
		return maxAttackDamage;
	}
	
	public RPGObject createObject() {
		return itemCreator != null ? itemCreator.apply(this) : new RPGObject(this);
	}

	@Override
	public String toJSONPrimitive() {
		return name();
	}
	
	public static RPGObjectType decodePrimitive(Object value) {
		return valueOf((String) value);
	}
	
}

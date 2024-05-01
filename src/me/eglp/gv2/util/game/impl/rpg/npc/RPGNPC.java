package me.eglp.gv2.util.game.impl.rpg.npc;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import me.eglp.gv2.util.game.impl.rpg.RPG;
import me.eglp.gv2.util.game.impl.rpg.RPGPlayer;
import me.eglp.gv2.util.game.impl.rpg.quest.RPGQuest;
import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONConverter;
import me.mrletsplay.mrcore.json.converter.JSONConvertible;
import me.mrletsplay.mrcore.json.converter.JSONValue;

public class RPGNPC implements JSONConvertible {

	public static final String[]
			FIRST_NAMES = {
						"Adam", "Geoffrey", "Gilbert", "Henry", "Hugh", "John", "Nicholas", "Peter", "Ralf", "Richard", "Robert", "Roger", "Simon","Thomas", "Walter", "William",
						"Agnes", "Alice", "Avice", "Beatrice", "Cecily", "Emma", "Isabella", "Joan", "Juliana", "Margery", "Matilda", "Rohesia"
					},
			LAST_NAMES = {
						"Ashdown", "Baker", "Bennet", "Bigge", "Brickenden", "Brooker", "Browne", "Carpenter", "Cheeseman", "Clarke", "Cooper", "Fletcher", "Foreman", "Godfrey", "Gregory", "Hughes", "Mannering", "Nash", "Payne", "Rolfe", "Taylor", "Walter", "Ward", "Webb", "Wood"
					};

	@JSONValue
	private RPGNPCType type;

	@JSONValue
	private int x, y;

	@JSONValue
	private String name;

	private Map<String, RPGQuest> quests;

	@JSONConstructor
	private RPGNPC() {}

	public RPGNPC(RPGNPCType type, int x, int y) {
		this.type = type;
		this.x = x;
		this.y = y;
		Random r = RPG.INSTANCE.getRandom();
		this.name = FIRST_NAMES[r.nextInt(FIRST_NAMES.length)] + " " + LAST_NAMES[r.nextInt(LAST_NAMES.length)];
		this.quests = new HashMap<>();
	}

	public RPGNPCType getType() {
		return type;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public String getName() {
		return name + " the " + type.getTitle();
	}

	public void setQuest(RPGPlayer player, RPGQuest quest) {
		quests.put(player.getUserID(), quest);
	}

	public void removeQuest(RPGPlayer player) {
		quests.remove(player.getUserID());
	}

	public RPGQuest getQuest(RPGPlayer player) {
		return quests.get(player.getUserID());
	}

	public void onInteract(RPGPlayer player) {
		type.onInteract(this, player);
	}

	@Override
	public void preSerialize(JSONObject object) {
		JSONObject qs = new JSONObject();
		for(Map.Entry<String, RPGQuest> q : quests.entrySet()) {
			qs.put(q.getKey(), q.getValue().toJSON());
		}
		object.put("quests", qs);
	}

	@Override
	public void preDeserialize(JSONObject object) {
		this.quests = new HashMap<>();
		JSONObject qs = object.getJSONObject("quests");
		for(String k : qs.keys()) {
			RPGQuest q = JSONConverter.decodeObject(qs.getJSONObject(k), RPGQuest.class);
			q.setNPC(this);
			quests.put(k, q);
		}
	}

	public RPGQuest generateNewQuest(RPGPlayer rpgPlayer) {
		return type.generateNewQuest(this, rpgPlayer);
	}

}

package me.eglp.gv2.util.game.impl.rpg;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONConverter;
import me.mrletsplay.mrcore.json.converter.JSONConvertible;

public class RPGMap implements JSONConvertible {

	public static final int MAP_SIZE = 256;

	private Map<Integer, RPGLocation> locations;

	@JSONConstructor
	public RPGMap() {
		this.locations = new HashMap<>();
	}

	public RPGLocation getLocation(int x, int y) {
		if(isOOB(x, y)) return null;
		int p = xyp(x, y);
		if(!locations.containsKey(p)) locations.put(p, RPGLocation.generateNew(x, y));
		return locations.get(p);
	}

	public int xyp(int x, int y) {
		return y * MAP_SIZE + x;
	}

	public boolean isOOB(int x, int y) {
		return x < 0 || y < 0 || x >= MAP_SIZE || y >= MAP_SIZE;
	}

	public Map<Integer, RPGLocation> getLocations() {
		return locations;
	}

	public static int[] getRandomSpotNear(int x, int y, int r) {
		Random rn = RPG.INSTANCE.getRandom();
		return new int[] {constr(x + rn.nextInt(2 * r) - r, 0, MAP_SIZE), constr(y + rn.nextInt(2 * r) - r, 0, MAP_SIZE)};
	}

	private static int constr(int v, int min, int max) {
		return Math.max(Math.min(v, max), min);
	}

	@Override
	public void preSerialize(JSONObject object) {
		JSONObject locs = new JSONObject();
		for(Map.Entry<Integer, RPGLocation> en : new HashMap<>(locations).entrySet()) {
			locs.put(""+en.getKey(), en.getValue().toJSON());
		}
		object.put("locations", locs);
	}

	@Override
	public void preDeserialize(JSONObject object) {
		JSONObject locs = object.getJSONObject("locations");
		for(String k : locs.keys()) {
			locations.put(Integer.parseInt(k), JSONConverter.decodeObject(locs.getJSONObject(k), RPGLocation.class));
		}
	}

}

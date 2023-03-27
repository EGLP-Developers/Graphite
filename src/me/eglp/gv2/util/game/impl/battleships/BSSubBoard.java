package me.eglp.gv2.util.game.impl.battleships;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class BSSubBoard {
	
	public static final int
		OCEAN = 0,
		SHIP = 1,
		MISSED_SHOT = 2,
		HIT_SHOT = 3,
		SUNKEN_SHIP = 4;
		

	private int width, height;
	private int[] primary;
	private Map<ShipType, int[]> shipPositions;
	
	public BSSubBoard(int w, int h) {
		this.width = w;
		this.height = h;
		this.primary = new int[w * h];
		this.shipPositions = new HashMap<>();
	}
	
	public void resetBoats() {
		this.shipPositions = new HashMap<>();
	}
	
	public ShipType getNextShipType() {
		return Arrays.stream(ShipType.values()).filter(t -> !shipPositions.containsKey(t)).findFirst().orElse(null);
	}
	
	public boolean hasFinishedPlacingShips() {
		return shipPositions.size() == ShipType.values().length;
	}
	
	public boolean placeShip(ShipType type, int x, int y, int rot) {
		if(isOOB(x, y) || (rot == 0 ? isOOB(x + type.getSize() - 1, y) : isOOB(x, y + type.getSize() - 1))) {
			return false;
		}
		for(int i = 0; i < type.getSize(); i++) {
			if(rot == 0) {
				if(get(x + i, y) != OCEAN) return false;
			}else {
				if(get(x, y + i) != OCEAN) return false;
			}
		}
		shipPositions.put(type, new int[] {x, y, rot});
		for(int i = 0; i < type.getSize(); i++) {
			if(rot == 0) {
				set(x + i, y, SHIP);
			}else {
				set(x, y + i, SHIP);
			}
		}
		return true;
	}
	
	public boolean fireAt(int x, int y) {
		if(get(x, y) == HIT_SHOT || get(x, y) == MISSED_SHOT || get(x, y) == SUNKEN_SHIP) return false;
		if(get(x, y) == SHIP) {
			set(x, y, HIT_SHOT);
			updShips();
			return true;
		}
		set(x, y, MISSED_SHOT);
		return true;
	}
	
	public int[] getOpponentTrackingBoard() {
		int[] br = new int[width * height];
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				if(get(x, y) != SHIP) br[y * width + x] = get(x, y);
			}
		}
		return br;
	}
	
	private boolean updShips() {
		for(ShipType ship : shipPositions.keySet()) {
			if(!hasShipSunk(ship)) continue;
			int[] p = shipPositions.get(ship);
			int x = p[0], y = p[1];
			for(int i = 0; i < ship.getSize(); i++) {
				if(p[2] == 0) {
					set(x + i, y, SUNKEN_SHIP);
				}else {
					set(x, y + i, SUNKEN_SHIP);
				}
			}
		}
		return false;
	}
	
	private boolean hasShipSunk(ShipType ship) {
		int[] p = shipPositions.get(ship);
		int x = p[0], y = p[1];
		for(int i = 0; i < ship.getSize(); i++) {
			if(p[2] == 0) {
				if(get(x + i, y) == SHIP) return false;
			}else {
				if(get(x, y + i) == SHIP) return false;
			}
		}
		return true;
	}
	
	public boolean hasLost() {
		return Arrays.stream(ShipType.values()).allMatch(this::hasShipSunk);
	}
	
	private boolean isOOB(int x, int y) {
		return x < 0 || y < 0 || x >= width || y >= height;
	}
	
	public void set(int x, int y, int v) {
		primary[y * width + x] = v;
	}
	
	public int get(int x, int y) {
		return primary[y * width + x];
	}
	
}

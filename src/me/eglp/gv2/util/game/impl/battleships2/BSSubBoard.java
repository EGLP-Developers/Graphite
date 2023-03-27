package me.eglp.gv2.util.game.impl.battleships2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
	private List<PlacedShip> ships;
	
	public BSSubBoard(int w, int h) {
		this.width = w;
		this.height = h;
		this.primary = new int[w * h];
		this.ships = new ArrayList<>();
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
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
	public boolean canPlaceShip(ShipType type, int x, int y, Direction dir) {
		return dir.getPoints(x, y, type).stream()
				.allMatch(p -> !isOOB(p.getX(), p.getY()) && get(p.getX(), p.getY()) == OCEAN);
	}
	
	public boolean placeShip(ShipType type, int x, int y, Direction dir) {
		if(!canPlaceShip(type, x, y, dir)) return false;
		ships.add(new PlacedShip(type, x, y, dir));
		dir.getPoints(x, y, type).forEach(p -> set(p.getX(), p.getY(), SHIP));
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
	
	private void updShips() {
		for(PlacedShip s : ships) {
			if(!hasShipSunk(s)) continue;
			s.getPoints().forEach(p -> set(p.getX(), p.getY(), SUNKEN_SHIP));
		}
	}
	
	private boolean hasShipSunk(PlacedShip s) {
		return s.getPoints().stream().allMatch(p -> get(p.getX(), p.getY()) == HIT_SHOT || get(p.getX(), p.getY()) == SUNKEN_SHIP);
	}
	
	public boolean hasLost() {
		return ships.stream()
				.allMatch(s -> hasShipSunk(s));
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

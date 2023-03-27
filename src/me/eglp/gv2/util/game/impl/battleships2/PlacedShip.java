package me.eglp.gv2.util.game.impl.battleships2;

import java.util.List;

public class PlacedShip {
	
	private ShipType type;
	private int x, y;
	private Direction direction;
	
	public PlacedShip(ShipType type, int x, int y, Direction direction) {
		this.type = type;
		this.x = x;
		this.y = y;
		this.direction = direction;
	}

	public ShipType getType() {
		return type;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public Direction getDirection() {
		return direction;
	}
	
	public List<Point> getPoints() {
		return direction.getPoints(x, y, type);
	}
	
}

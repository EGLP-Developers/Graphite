package me.eglp.gv2.util.game.impl.battleships2;

import java.util.ArrayList;
import java.util.List;

public enum Direction {
	
	RIGHT,
	DOWN,
	LEFT,
	UP;
	
	public List<Point> getPoints(int startX, int startY, ShipType type) {
		int endX = startX, endY = startX;
		switch(this) {
			case DOWN:
				endY += type.getSize() - 1;
				break;
			case LEFT:
				endX -= type.getSize() - 1;
				break;
			case RIGHT:
				endX += type.getSize() - 1;
				break;
			case UP:
				endY -= type.getSize() - 1;
				break;
		}
		
		List<Point> points = new ArrayList<>();
		if(startX != endX) {
			for(int pX = Math.min(startX, endX); pX <= Math.max(startX, endX); pX++) {
				points.add(new Point(pX, startY));
			}
		}else if(startY != endY) {
			for(int pY = Math.min(startY, endY); pY <= Math.max(startY, endY); pY++) {
				points.add(new Point(startX, pY));
			}
		}
		return points;
	}

}

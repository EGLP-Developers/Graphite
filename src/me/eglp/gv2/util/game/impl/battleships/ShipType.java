package me.eglp.gv2.util.game.impl.battleships;

public enum ShipType {

	CARRIER("Carrier", 5),
	BATTLESHIP("Battleship", 4),
	CRUISER("Cruiser", 3),
	SUBMARINE("Submarine", 3),
	DESTROYER("Destroyer", 2);
	
	private String friendlyName;
	private final int size;
	
	private ShipType(String friendlyName, int size) {
		this.friendlyName = friendlyName;
		this.size = size;
	}
	
	public String getFriendlyName() {
		return friendlyName;
	}
	
	public int getSize() {
		return size;
	}
	
}

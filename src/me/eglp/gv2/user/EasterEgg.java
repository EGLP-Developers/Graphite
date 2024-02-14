package me.eglp.gv2.user;

public enum EasterEgg {

	SMILE("Because I'm happy", "Change your nickname to 'Smile'", 100),
	MUSIC_VOLUME_EARRAPE("UNTZ UNTZ UNTZ", "Enable earrape mode", 100),
	COIN_FLIP_SIDE("FLIP FLOP on the side", "The coin landed on its side", 1000),
	;

	private final String name, description;
	private final int money;

	private EasterEgg(String name, String description, int money) {
		this.name = name;
		this.description = description;
		this.money = money;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public int getMoney() {
		return money;
	}

}

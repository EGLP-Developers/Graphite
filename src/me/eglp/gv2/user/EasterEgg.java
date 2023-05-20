package me.eglp.gv2.user;

public enum EasterEgg {

	SMILE("Because I'm happy", "Change your nickname to 'Smile'"),
	MUSIC_VOLUME_EARRAPE("UNTZ UNTZ UNTZ", "Enable earrape mode"),
	COIN_FLIP_SIDE("FLIP FLOP on the side", "The coin landed on its side"),
	;

	private String name, description;

	private EasterEgg(String name, String description) {
		this.name = name;
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

}

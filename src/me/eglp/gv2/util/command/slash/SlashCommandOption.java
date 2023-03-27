package me.eglp.gv2.util.command.slash;

public class SlashCommandOption {
	
	private SlashCommandOptionType type;
	private String name;
	
	private SlashCommandOption(SlashCommandOptionType type, String name) {
		this.type = type;
		this.name = name;
	}

	public SlashCommandOptionType getType() {
		return type;
	}
	
	public String getName() {
		return name;
	}
	
}

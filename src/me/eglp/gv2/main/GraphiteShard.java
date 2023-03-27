package me.eglp.gv2.main;

import net.dv8tion.jda.api.JDA;

public class GraphiteShard {

	private int id;
	private JDA jda;
	
	public GraphiteShard(int id, JDA jda) {
		this.id = id;
		this.jda = jda;
	}
	
	public int getID() {
		return id;
	}
	
	public JDA getJDA() {
		return jda;
	}

	public String getStatus() {
		return jda.getStatus().toString();
	}
	
	public long getPing() {
		return jda.getGatewayPing();
	}
	
}

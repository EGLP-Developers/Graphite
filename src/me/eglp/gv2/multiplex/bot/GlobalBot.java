package me.eglp.gv2.multiplex.bot;

import java.util.List;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.main.GraphiteShard;
import me.eglp.gv2.multiplex.MultiplexCache;
import me.mrletsplay.mrcore.misc.FriendlyException;

public class GlobalBot extends MultiplexBot {

	public static final GlobalBot INSTANCE = new GlobalBot();

	public GlobalBot() {
		this.botInfo = new GlobalBotInfo();
		this.cache = new MultiplexCache(this);
	}
	
	@Override
	public List<GraphiteShard> getShards() {
		return Graphite.getGlobalShards();
	}
	
	@Override
	public String getID() {
		throw new FriendlyException("Bot is global bot and doesn't have an ID");
	}
	
}

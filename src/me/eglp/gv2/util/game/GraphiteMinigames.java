package me.eglp.gv2.util.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.eglp.gv2.user.GraphiteUser;

public class GraphiteMinigames {

	private GraphiteMinigameStats stats;
	private Map<GraphiteUser, MinigameInstance> games;
	private Map<GraphiteMinigame, List<MultiPlayerMinigameInstance>> sharedMinigames;

	public GraphiteMinigames() {
		this.stats = new GraphiteMinigameStats();
		this.games = new HashMap<>();
		this.sharedMinigames = new HashMap<>();

		for(GraphiteMinigame m : GraphiteMinigame.values()) {
			if(!m.isMultiplayer() || m.isGlobal()) continue;
			sharedMinigames.put(m, new ArrayList<>());
		}
	}

	public void setGame(GraphiteUser user, MinigameInstance game) {
		if(game == null) {
			MinigameInstance i = getGame(user);
			games.remove(user);
			if(i != null) {
				i.onUserLeave(user);
			}
			return;
		}
		games.put(user, game);
	}

	public void leaveGame(GraphiteUser user) {
		setGame(user, null);
	}

	public MinigameInstance getGame(GraphiteUser user) {
		return games.get(user);
	}

	public boolean isInGame(GraphiteUser user) {
		return games.containsKey(user);
	}

	public void shareMinigame(MultiPlayerMinigameInstance game) {
		if(sharedMinigames.get(game.getGame()).contains(game)) return;
		sharedMinigames.get(game.getGame()).add(game);
	}

	public void unshareMinigame(MultiPlayerMinigameInstance game) {
		sharedMinigames.get(game.getGame()).remove(game);
	}

	public List<MultiPlayerMinigameInstance> getSharedMinigames(GraphiteMinigame game) {
		List<MultiPlayerMinigameInstance> mgs = sharedMinigames.get(game);
		return mgs == null ? Collections.emptyList() : mgs;
	}

	public MultiPlayerMinigameInstance getSharedMinigame(GraphiteMinigame game, GraphiteUser user) {
		List<MultiPlayerMinigameInstance> insts = sharedMinigames.get(game);
		if(insts == null || insts.isEmpty()) return null;
		return insts.stream()
				.filter(g -> g.isJoinable())
				.findFirst().orElse(null);
	}

	public GraphiteMinigameStats getStats() {
		return stats;
	}

}

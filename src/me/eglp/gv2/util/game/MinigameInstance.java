package me.eglp.gv2.util.game;

import java.util.List;
import java.util.Objects;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.base.user.GraphiteUser;
import me.eglp.gv2.util.game.output.GameOutput;
import me.eglp.gv2.util.input.GraphiteInput;

public interface MinigameInstance {
	
	public List<GraphiteInput> getActiveInputs();
	
	public List<GameOutput> getActiveOutputs();
	
	public GraphiteMinigame getGame();
	
	public void addUser(GraphiteUser user);
	
	public List<GraphiteUser> getPlayingUsers();
	
	public void onUserLeave(GraphiteUser user);
	
	public default void userWon(GraphiteUser user, String subCategory, GraphiteMinigameMoney winnerMoney) {
		userWon(user, subCategory, winnerMoney.getMoney());
	}
	
	public default void userWon(GraphiteUser user, String subCategory, int winnerMoney) {
		Graphite.getMinigames().getStats().addUserWin(getGame(), user);
		Graphite.getEconomy().addMoney(user, winnerMoney);
	}
	
	public default void userLost(GraphiteUser user, String subCategory) {
		Graphite.getMinigames().getStats().addUserLoss(getGame(), user);
	}
	
	public default void stop() {
		stop(true);
	}
	
	public default void stop(boolean removeOutputs) {
		getActiveInputs().stream().filter(Objects::nonNull).forEach(GraphiteInput::remove);
		if(removeOutputs) getActiveOutputs().stream().filter(Objects::nonNull).forEach(GameOutput::remove);
		getPlayingUsers().stream().filter(Objects::nonNull).forEach(p -> Graphite.getMinigames().leaveGame(p));
	}
	
}

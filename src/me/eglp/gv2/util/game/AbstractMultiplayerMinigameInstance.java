package me.eglp.gv2.util.game;

import me.eglp.gv2.util.lang.DefaultMessage;

public abstract class AbstractMultiplayerMinigameInstance implements MultiPlayerMinigameInstance {
	
	private boolean running;
	
	public AbstractMultiplayerMinigameInstance() {
		this.running = true;
	}
	
	public void setRunning(boolean running) {
		this.running = running;
	}
	
	public boolean isRunning() {
		return running;
	}
	
	public void endGame() {
		endGame(null, (String[]) null);
	}
	
	public synchronized void endGame(DefaultMessage message, String... params) {
		if(!running) return;
		running = false;
		if(message != null) {
			getPlayingUsers().stream()
				.filter(p -> p != null)
				.forEach(p -> p.openPrivateChannel().sendMessage(message, params));
		}
		stop();
	}
	
	@Override
	public void stop(boolean removeOutputs) {
		running = false;
		MultiPlayerMinigameInstance.super.stop(removeOutputs);
	}

}

package me.eglp.gv2.util.game.impl.rpg.dialog;

import java.util.function.Consumer;

import me.eglp.gv2.util.game.impl.rpg.RPGPlayer;

public class CallbackDialog implements DialogAction {

	private String[] options;
	private Consumer<Integer> callback;
	
	public CallbackDialog(RPGPlayer player, Consumer<Integer> callback, String... options) {
		this.options = options;
		this.callback = callback;
		send(player);
	}

	@Override
	public String[] getOptions() {
		return options;
	}

	@Override
	public void respond(int index) {
		callback.accept(index);
	}

}

package me.eglp.gv2.util.game.impl.rpg.dialog;

import java.util.function.Consumer;

import me.eglp.gv2.util.game.impl.rpg.RPGPlayer;

public class BooleanCallbackDialog extends CallbackDialog {

	public BooleanCallbackDialog(RPGPlayer player, Consumer<Boolean> callback, String yesOption, String noOption) {
		super(player, i -> callback.accept(i == 0), yesOption, noOption);
	}

}

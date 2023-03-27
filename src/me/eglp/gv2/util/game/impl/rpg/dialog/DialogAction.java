package me.eglp.gv2.util.game.impl.rpg.dialog;

import java.util.Arrays;

import me.eglp.gv2.util.game.impl.rpg.RPGLocation;
import me.eglp.gv2.util.game.impl.rpg.RPGPlayer;

public interface DialogAction {
	
	public default void send(RPGPlayer player) {
		String[] ops = getOptions();
		if(ops.length == 0) return;
		String str = "";
		for(int i = 0; i < ops.length; i++) {
			str += (i + 1) + ") " + ops[i] + "\n";
		}
		player.send(str.substring(0, str.length() - 1));
	}

	public String[] getOptions();
	
	public default int getIndex(String response) {
		String[] ops = getOptions();
		try {
			int i = Integer.parseInt(response) - 1;
			if(i >= 0 && i < ops.length) return i;
		}catch(NumberFormatException ignored) {}
		String r = RPGLocation.find(Arrays.asList(ops), response, s -> s);
		if(r == null) return -1;
		return Arrays.binarySearch(ops, r);
	}
	
	public void respond(int index);
	
}

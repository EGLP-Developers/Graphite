package me.eglp.gv2.util.game.output.renderer;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import me.eglp.gv2.util.emote.JDAEmote;

public class IntMappingGetterRenderer implements MessageRenderer {

	private int width, height;
	private BiFunction<Integer, Integer, Integer> getter;
	private Map<Integer, String> mappings;
	
	public IntMappingGetterRenderer(int w, int h, BiFunction<Integer, Integer, Integer> getter) {
		this.width = w;
		this.height = h;
		this.getter = getter;
		this.mappings = new HashMap<>();
	}
	
	public BiFunction<Integer, Integer, Integer> getGetter() {
		return getter;
	}
	
	public void addMapping(int v, String symbol) {
		mappings.put(v, symbol);
	}
	
	public void addMapping(int v, JDAEmote emote) {
		addMapping(v, emote.getUnicode());
	}

	@Override
	public void render(MessageGraphics graphics, int x, int y) {
		for(int iX = 0; iX < width; iX++) {
			for(int iY = 0; iY < height; iY++) {
				int v = getter.apply(iX, iY);
				graphics.setSymbol(mappings.get(v));
				graphics.point(x + iX, y + iY);
			}
		}
	}

}

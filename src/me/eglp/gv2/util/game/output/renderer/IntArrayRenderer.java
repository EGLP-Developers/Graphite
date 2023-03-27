package me.eglp.gv2.util.game.output.renderer;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import me.eglp.gv2.util.emote.JDAEmote;

public class IntArrayRenderer implements MessageRenderer {

	private int width, height;
	private Supplier<int[]> supplier;
	private Map<Integer, String> mappings;
	
	public IntArrayRenderer(int w, int h, Supplier<int[]> supplier) {
		this.width = w;
		this.height = h;
		this.supplier = supplier;
		this.mappings = new HashMap<>();
	}
	
	public Supplier<int[]> getSupplier() {
		return supplier;
	}
	
	public void addMapping(int v, String symbol) {
		mappings.put(v, symbol);
	}
	
	public void addMapping(int v, JDAEmote emote) {
		addMapping(v, emote.getUnicode());
	}

	@Override
	public void render(MessageGraphics graphics, int x, int y) {
		int[] arr = supplier.get();
		for(int iX = 0; iX < width; iX++) {
			for(int iY = 0; iY < height; iY++) {
				int v = arr[iY * width + iX];
				graphics.setSymbol(mappings.get(v));
				graphics.point(x + iX, y + iY);
			}
		}
	}
	
}

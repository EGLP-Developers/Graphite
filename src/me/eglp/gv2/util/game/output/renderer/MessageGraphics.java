package me.eglp.gv2.util.game.output.renderer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import me.eglp.gv2.util.emote.JDAEmote;

public class MessageGraphics {

	private String symbol;
	private Map<Integer, String> points;
	
	public MessageGraphics() {
		this.points = new HashMap<>();
		this.symbol = JDAEmote.WHITE_LARGE_SQUARE.getUnicode();
	}
	
	public void setSymbol(String unicode) {
		this.symbol = unicode;
	}
	
	public void setSymbol(JDAEmote emote) {
		setSymbol(emote.getUnicode());
	}
	
	public void point(int x, int y) {
		points.put(xyp(x, y), symbol);
	}
	
	public void fill(int x, int y, int w, int h) {
		for(int nx = x; nx < x + w; nx++) {
			for(int ny = y; ny < y + h; ny++) {
				point(nx, ny);
			}
		}
	}
	
	public void draw(int x, int y, MessageGraphics graphics) {
		String s = symbol;
		for(Map.Entry<Integer, String> pt : graphics.points.entrySet()) {
			int[] p = pxy(pt.getKey());
			setSymbol(pt.getValue());
			point(p[0] + x, p[1] + y);
		}
		setSymbol(s);
	}
	
	public String render() {
		return render(true);
	}
	
	public String render(boolean fillEmpty) {
		if(points.isEmpty()) return "";
		int
			smX = points.keySet().stream().mapToInt(p -> pxy(p)[0]).min().getAsInt(),
			smY = points.keySet().stream().mapToInt(p -> pxy(p)[1]).min().getAsInt(),
			lgX = points.keySet().stream().mapToInt(p -> pxy(p)[0]).max().getAsInt(),
			lgY = points.keySet().stream().mapToInt(p -> pxy(p)[1]).max().getAsInt();
		String[][] rd = new String[lgY - smY + 1][lgX - smX + 1];
		for(Map.Entry<Integer, String> pt : points.entrySet()) {
			int[] p = pxy(pt.getKey());
			rd[p[1] - smY][p[0] - smX] = pt.getValue();
		}
		return Arrays.stream(rd)
				.map(l -> Arrays.stream(l)
						.map(v -> v == null ? (fillEmpty ? JDAEmote.BLACK_LARGE_SQUARE.getUnicode() : "") : v)
						.collect(Collectors.joining()))
				.collect(Collectors.joining("\n"));
	}
	
	private int xyp(int x, int y) {
		return ((int) (y << 16) | Short.toUnsignedInt((short) x));
	}
	
	private int[] pxy(int p) {
		return new int[] {(short) (p & 0xFFFF), (short) (p >> 16)};
	}
	
}

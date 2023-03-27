package me.eglp.gv2.util.game.impl.connectfour;

import java.util.Arrays;

public class C4Board {
	
	private int[] board;
	private int width, height;
	
	public C4Board() {
		this.width = 7;
		this.height = 6;
		this.board = new int[width * height];
	}
	
	public boolean drop(int x, int p) {
		for(int y = height - 1; y >= 0; y--) {
			if(get(x, y) == 0) {
				set(x, y, p);
				return true;
			}
		}
		return false;
	}
	
	public int getWinner() {
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				int v = get(x, y);
				if(v == 0) continue;
				if(hasWonAt(x, y, v)) return v;
			}
		}
		return Arrays.stream(board).allMatch(i -> i != 0) ? -1 : 0;
	}
	
	private boolean hasWonAt(int x, int y, int v) {
		boolean wh = true, wv = true, wdd = true, wdu = true;
		for(int dx = 1; dx <= 3; dx++) {
			if(isOOB(x + dx, y) || get(x + dx, y) != v) {
				wh = false;
				break;
			}
		}
		if(wh) return true;
		for(int dy = 1; dy <= 3; dy++) {
			if(isOOB(x, y + dy) || get(x, y + dy) != v) {
				wv = false;
				break;
			}
		}
		if(wv) return true;
		for(int d = 1; d <= 3; d++) {
			if(isOOB(x + d, y + d) || get(x + d, y + d) != v) {
				wdd = false;
				break;
			}
		}
		if(wdd) return true;
		for(int d = 1; d <= 3; d++) {
			if(isOOB(x + d, y - d) || get(x + d, y - d) != v) {
				wdu = false;
				break;
			}
		}
		return wdu;
	}
	
	public int[] getBoard() {
		return board;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	private boolean isOOB(int x, int y) {
		return x < 0 || y < 0 || x >= width || y >= height;
	}
	
	public void set(int x, int y, int v) {
		board[y * width + x] = v;
	}
	
	public int get(int x, int y) {
		return board[y * width + x];
	}
	
}

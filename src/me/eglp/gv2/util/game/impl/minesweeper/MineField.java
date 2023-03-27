package me.eglp.gv2.util.game.impl.minesweeper;

import java.util.Random;

public class MineField {
	
	public static final int
		UNREVEALED = -1,
		MINE = -2,
		FLAG = -3,
		MINE_EXPLODED = -4;
	
	private int size, numMines;
	private boolean[] mines, revealed, flags;
	private boolean lost;
	private boolean b;
	
	public MineField(int size, int numMines) {
		this.size = size;
		this.numMines = numMines;
		this.mines = new boolean[size * size];
		this.revealed = new boolean[size * size];
		this.flags = new boolean[size * size];
	}
	
	private void genField(int sx, int sy) {
		b = true;
		Random r = new Random();
		for(int i = 0; i < numMines; i++) {
			int x = r.nextInt(size), y = r.nextInt(size);
			if(get(mines, x, y) || (Math.abs(sx - x) <= 1 && Math.abs(sy - y) <= 1)) {
				i--;
				continue;
			}
			set(mines, x, y, true);
		}
	}
	
	public void flag(int x, int y) {
		set(flags, x, y, !get(flags, x, y));
	}
	
	public void reveal(int x, int y) {
		if(!b) genField(x, y);
		set(revealed, x, y, true);
		if(get(mines, x, y)) {
			lost = true;
			return;
		}
		int[][] f = genNumField(true);
		int nFl = 0;
		for(int rx = -1; rx <= 1; rx++) {
			for(int ry = -1; ry <= 1; ry++) {
				if(x == 0 && y == 0) continue;
				int nx = x + rx, ny = y + ry;
				if(isOOB(nx, ny)) continue;
				if(get(flags, nx, ny)) nFl++;
			}
		}
		if(f[x][y] >= 0 && nFl >= f[x][y]) {
			for(int rx = -1; rx <= 1; rx++) {
				for(int ry = -1; ry <= 1; ry++) {
					if(x == 0 && y == 0) continue;
					int nx = x + rx, ny = y + ry;
					if(isOOB(nx, ny)) continue;
					if(!get(flags, nx, ny) && !get(revealed, nx, ny)) reveal(nx, ny);
				}
			}
		}
	}
	
	public int getSize() {
		return size;
	}
	
	public int getNumMines() {
		return numMines;
	}
	
	public boolean hasLost() {
		return lost;
	}
	
	public boolean hasWon() {
		if(hasLost()) return false;
		int n = 0;
		for(int i = 0; i < revealed.length; i++) {
			if(revealed[i]) n++;
		}
		return n == revealed.length - numMines;
	}
	
	public int[][] genNumField(boolean revealAll) {
		int[][] f = new int[size][size];
		
		for(int x = 0; x < size; x++) {
			for(int y = 0; y < size; y++) {
				if(!revealAll && get(flags, x, y)) {
					f[x][y] = FLAG;
					continue;
				}
				if(!revealAll && !get(revealed, x, y)) {
					f[x][y] = UNREVEALED;
					continue;
				}
				if(get(mines, x, y)) {
					f[x][y] = get(revealed, x, y) ? MINE_EXPLODED : MINE;
					continue;
				}
				int n = 0;
				for(int rx = -1; rx <= 1; rx++) {
					for(int ry = -1; ry <= 1; ry++) {
						if(x == 0 && y == 0) continue;
						if(isOOB(x + rx, y + ry)) continue;
						if(get(mines, x + rx, y + ry)) n++;
					}
				}
				f[x][y] = n;
			}
		}
		return f;
	}
	
	private boolean isOOB(int x, int y) {
		return x < 0 || y < 0 || x >= size || y >= size;
	}
	
	private void set(boolean[] f, int x, int y, boolean b) {
		f[y * size + x] = b;
	}
	
	private boolean get(boolean[] f, int x, int y) {
		return f[y * size + x];
	}
	
}

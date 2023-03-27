package me.eglp.gv2.util.game.impl.tictactoe;

import java.util.Arrays;
import java.util.stream.IntStream;

public class TTTBoard {
	
	public static final int
		EMPTY = 0,
		X = 1,
		O = 2;
	
	private int[] board;
	private int size;
	
	public TTTBoard(int size) {
		this.board = new int[size * size];
		this.size = size;
	}
	
	public int checkWinner() {
		// Columns
		for(int x = 0; x < size; x++) {
			final int fX = x;
			int firstV = get(x, 0);
			if(firstV == EMPTY) continue;
			boolean hasWonColumn = IntStream.range(1, size).allMatch(y -> get(fX, y) == firstV);
			if(hasWonColumn) return firstV;
		}
		
		// Rows
		for(int y = 0; y < size; y++) {
			final int fY = y;
			int firstV = get(0, y);
			if(firstV == EMPTY) continue;
			boolean hasWonRow = IntStream.range(1, size).allMatch(x -> get(x, fY) == firstV);
			if(hasWonRow) return firstV;
		}
		
		// Diagonal 1
		int firstVDiagonal1 = get(0, 0);
		if(firstVDiagonal1 != EMPTY) {
			boolean wonDiagonal1 = IntStream.range(1, size).allMatch(v -> get(v, v) == firstVDiagonal1);
			if(wonDiagonal1) return firstVDiagonal1;
		}
		
		// Diagonal 2
		int firstVDiagonal2 = get(size - 1, 0);
		if(firstVDiagonal2 != EMPTY) {
			boolean wonDiagonal2 = IntStream.range(1, size).allMatch(v -> get(size - v - 1, v) == firstVDiagonal2);
			if(wonDiagonal2) return firstVDiagonal2;
		}
		
		if(Arrays.stream(board).allMatch(i -> i != EMPTY)) return -1;
		
		return EMPTY;
	}
	
	public void set(int field, int v) {
		board[field] = v;
	}
	
	public int get(int field) {
		return board[field];
	}
	
	public void set(int x, int y, int v) {
		board[y * size + x] = v;
	}
	
	public int get(int x, int y) {
		return board[y * size + x];
	}
	
}

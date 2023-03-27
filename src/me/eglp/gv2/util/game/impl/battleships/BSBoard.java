package me.eglp.gv2.util.game.impl.battleships;

public class BSBoard {
	
	private BSSubBoard player1, player2;
	private int width, height;
	
	public BSBoard() {
		this.width = 10;
		this.height = 10;
		this.player1 = new BSSubBoard(width, height);
		this.player2 = new BSSubBoard(width, height);
	}
	
	public int getWinner() {
		if(player1.hasLost()) return 2;
		if(player2.hasLost()) return 1;
		return 0;
	}
	
	public BSSubBoard getPlayer1() {
		return player1;
	}
	
	public BSSubBoard getPlayer2() {
		return player2;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
}

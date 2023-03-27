package me.eglp.gv2.util.game;

public enum GraphiteMinigameMoney {
	
	BATTLESHIPS(2),
	TICTACTOE(2),
	MINESWEEPER(1),
	CONNECTFOUR(2),
	ROCK_PAPER_SCISSORS(2),
	POKER(2);
	
	private int money;
	
	private GraphiteMinigameMoney(int money) {
		this.money = money;
	}
	
	public int getMoney() {
		return money;
	}

}

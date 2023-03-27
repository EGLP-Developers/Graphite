package me.eglp.gv2.util.game.output.renderer;

public interface MessageRenderer {

	public void render(MessageGraphics graphics, int x, int y);

	public default void render(MessageGraphics graphics) {
		render(graphics, 0, 0);
	}
	
}

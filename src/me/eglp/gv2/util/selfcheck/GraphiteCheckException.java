package me.eglp.gv2.util.selfcheck;

public class GraphiteCheckException extends RuntimeException {

	private static final long serialVersionUID = 684115043911795104L;

	public GraphiteCheckException() {
		super();
	}

	public GraphiteCheckException(String message) {
		super(message);
	}

	public GraphiteCheckException(Throwable cause) {
		super(cause);
	}

	public GraphiteCheckException(String message, Throwable cause) {
		super(message, cause);
	}
	
}

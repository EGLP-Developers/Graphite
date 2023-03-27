package me.eglp.gv2.util.scripting;

public class ScriptTimeoutError extends Error {

	private static final long serialVersionUID = 2253387095340794802L;

	public ScriptTimeoutError() {
		super();
	}

	public ScriptTimeoutError(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ScriptTimeoutError(String message, Throwable cause) {
		super(message, cause);
	}

	public ScriptTimeoutError(String message) {
		super(message);
	}

	public ScriptTimeoutError(Throwable cause) {
		super(cause);
	}
	
}

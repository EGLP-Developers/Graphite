package me.eglp.gv2.util.scripting;

import me.eglp.gv2.util.scripting.object.JSCommandInvokedEvent;
import me.mrletsplay.mrcore.misc.FriendlyException;

/**
 * Represents an exception thrown by the script either due to invalid code or through automatic error messages (such as by methods like {@link JSCommandInvokedEvent#requireArgs(int)})<br>
 * When an exception of this type is thrown, it will automatically be translated into a message of type "Error in script: &lt;message&gt;"
 */
public class ScriptExecutionException extends FriendlyException {

	private static final long serialVersionUID = 9094274133895330933L;

	public ScriptExecutionException(String reason, Throwable cause) {
		super(reason, cause);
	}

	public ScriptExecutionException(String reason) {
		super(reason);
	}

	public ScriptExecutionException(Throwable cause) {
		super(cause);
	}
	
}

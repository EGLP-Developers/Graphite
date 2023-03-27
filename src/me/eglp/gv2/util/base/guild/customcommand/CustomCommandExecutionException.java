package me.eglp.gv2.util.base.guild.customcommand;

import me.mrletsplay.mrcore.misc.FriendlyException;

public class CustomCommandExecutionException extends FriendlyException {

	private static final long serialVersionUID = 2727795250517804918L;
	
	private CustomCommandInvokedEvent event;

	public CustomCommandExecutionException(CustomCommandInvokedEvent e, String reason, Throwable cause) {
		super(reason, cause);
		this.event = e;
	}

	public CustomCommandExecutionException(CustomCommandInvokedEvent e, String reason) {
		super(reason);
		this.event = e;
	}

	public CustomCommandExecutionException(CustomCommandInvokedEvent e, Throwable cause) {
		super(cause);
		this.event = e;
	}
	
	public CustomCommandInvokedEvent getEvent() {
		return event;
	}
	
}

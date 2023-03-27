package me.eglp.gv2.multiplex;

import me.mrletsplay.mrcore.misc.FriendlyException;

public class MultiplexException extends FriendlyException {
	
	private static final long serialVersionUID = 546392302900243246L;

	public MultiplexException(Throwable cause) {
		super(cause);
	}
	
	public MultiplexException(String reason) {
		super(reason);
	}
	
	public MultiplexException(String reason, Throwable cause) {
		super(reason, cause);
	}

}

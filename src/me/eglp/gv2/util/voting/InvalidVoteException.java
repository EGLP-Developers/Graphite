package me.eglp.gv2.util.voting;

import me.mrletsplay.mrcore.misc.FriendlyException;

public class InvalidVoteException extends FriendlyException {
	
	private static final long serialVersionUID = -4869093901200145329L;

	public InvalidVoteException(String message) {
		super(message);
	}

}

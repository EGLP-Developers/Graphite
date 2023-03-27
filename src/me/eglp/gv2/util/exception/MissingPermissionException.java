package me.eglp.gv2.util.exception;

import java.util.Arrays;
import java.util.stream.Collectors;

import me.mrletsplay.mrcore.misc.FriendlyException;
import net.dv8tion.jda.api.Permission;

public class MissingPermissionException extends FriendlyException {
	
	private static final long serialVersionUID = 2033020518788926889L;
	
	private Permission[] permissions;

	public MissingPermissionException(Permission... permissions) {
		super("Missing permission(s): " + Arrays.stream(permissions).map(Permission::getName).collect(Collectors.joining(", ")));
		this.permissions = permissions;
	}
	
	public Permission[] getPermissions() {
		return permissions;
	}

}

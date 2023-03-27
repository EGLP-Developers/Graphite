package me.eglp.gv2.util.permission;

public class Permission {
	
	private String permission;
	
	public Permission(String permission) {
		this.permission = permission;
	}
	
	public String getPermission() {
		return permission;
	}
	
	public boolean includes(Permission other) {
		return includes(other.getPermission());
	}
	
	public boolean includes(String permission) {
		String[] tPerm = this.permission.split("\\.");
		String[] oPerm = permission.split("\\.");
		
		if(oPerm.length < tPerm.length) return false;
		for(int i = 0; i < oPerm.length; i++) {
			if(i >= tPerm.length) return false;
			if(tPerm[i].equals("*")) return true;
			if(!tPerm[i].equalsIgnoreCase(oPerm[i])) return false;
		}
		return true;
	}
	
	@Override
	public String toString() {
		return permission;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof Permission)) return false;
		return ((Permission) obj).permission.equalsIgnoreCase(permission);
	}
	
	@Override
	public int hashCode() {
		return permission.hashCode();
	}

}

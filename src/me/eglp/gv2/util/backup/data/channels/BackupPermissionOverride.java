package me.eglp.gv2.util.backup.data.channels;

import me.eglp.gv2.main.Graphite;
import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONConvertible;
import me.mrletsplay.mrcore.json.converter.JSONPrimitiveStringConvertible;
import me.mrletsplay.mrcore.json.converter.JSONValue;
import net.dv8tion.jda.api.entities.PermissionOverride;

public class BackupPermissionOverride implements JSONConvertible {

	@JSONValue
	private Type type;

	@JSONValue
	private String id;

	@JSONValue
	private long
		allowed,
		denied;

	@JSONConstructor
	private BackupPermissionOverride() {}

	public BackupPermissionOverride(PermissionOverride jdaOverride) {
		if(jdaOverride.isMemberOverride()) {
			this.type = Type.MEMBER;
			this.id = jdaOverride.getId();
		}else {
			this.type = Type.ROLE;
			this.id = Graphite.getGuild(jdaOverride.getGuild()).getRole(jdaOverride.getRole()).getID();
		}

		this.allowed = jdaOverride.getAllowedRaw();
		this.denied = jdaOverride.getDeniedRaw();
	}

	public Type getType() {
		return type;
	}

	public String getID() {
		return id;
	}

	public long getAllowed() {
		return allowed;
	}

	public long getDenied() {
		return denied;
	}

	public static enum Type implements JSONPrimitiveStringConvertible {

		MEMBER,
		ROLE;

		@Override
		public String toJSONPrimitive() {
			return name();
		}

		public static Type decodePrimitive(String value) {
			return valueOf(value);
		}

	}

}

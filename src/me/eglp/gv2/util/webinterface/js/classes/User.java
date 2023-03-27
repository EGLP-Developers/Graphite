package me.eglp.gv2.util.webinterface.js.classes;

import me.eglp.gv2.util.base.user.GraphiteUser;
import me.eglp.gv2.util.webinterface.base.GraphiteWebinterfaceUser;
import me.eglp.gv2.util.webinterface.js.JavaScriptFunction;
import me.eglp.gv2.util.webinterface.js.JavaScriptParameter;
import me.eglp.gv2.util.webinterface.js.JavaScriptValue;
import me.eglp.gv2.util.webinterface.js.WebinterfaceObject;

public class User implements WebinterfaceObject {
	
	@JavaScriptValue(getter = "getID")
	private String id;
	
	@JavaScriptValue(getter = "getName")
	private String name;
	
	@JavaScriptValue(getter = "getFullName")
	private String fullName;
	
	@JavaScriptValue(getter = "getDiscriminator")
	private String discriminator;
	
	@JavaScriptValue(getter = "getAvatarURL")
	private String avatarUrl;
	
	@JavaScriptValue(getter = "getAvatarID")
	private String avatarID;
	
	@JavaScriptValue(getter = "isAdmin")
	private boolean admin;
	
	@JavaScriptValue(getter = "isOwner")
	private boolean owner;
	
	public User(String id, String name, String discriminator, String avatarUrl, String avatarID, boolean admin, boolean owner) {
		this.id = id;
		this.name = name;
		this.fullName = name + "#" + discriminator;
		this.discriminator = discriminator;
		this.avatarUrl = avatarUrl;
		this.avatarID = avatarID;
		this.admin = admin;
		this.owner = owner;
	}
	
	public User(GraphiteWebinterfaceUser u) {
		GraphiteUser usr = u.getDiscordUser();
		
		this.id = usr.getID();
		this.name = usr.getName();
		this.fullName = usr.getName() + "#" + usr.getDiscriminator();
		this.discriminator = usr.getDiscriminator();
		this.avatarUrl = usr.getJDAUser().getEffectiveAvatarUrl();
		this.avatarID = usr.getJDAUser().getAvatarId();
		this.admin = u.isAdmin();
	}
	
	@JavaScriptFunction(calling = "getSelfUser", returning = "user", withGuild = true)
	public static void getSelfUser() {};
	
	@JavaScriptFunction(calling = "isGuildAdmin", returning = "isAdmin", withGuild = true)
	public static void isGuildAdmin() {};
	
	@JavaScriptFunction(calling = "isGuildOwner", returning = "isOwner", withGuild = true)
	public static void isGuildOwner() {};
	
	@JavaScriptFunction(calling = "getUserByID", returning = "user", withGuild = true)
	public static void getUserByID(@JavaScriptParameter(name = "id") long id) {};
	
	@JavaScriptFunction(calling = "blockScripts", withGuild = false)
	public static void blockScripts(@JavaScriptParameter(name = "guild") String guild) {};
	
	@JavaScriptFunction(calling = "unblockAllScripts", withGuild = false)
	public static void unblockAllScripts() {};
	
}

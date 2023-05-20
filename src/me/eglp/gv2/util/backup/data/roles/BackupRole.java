package me.eglp.gv2.util.backup.data.roles;

import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.guild.GraphiteMember;
import me.eglp.gv2.guild.GraphiteRole;
import me.eglp.gv2.main.DebugCategory;
import me.eglp.gv2.main.GraphiteDebug;
import me.eglp.gv2.util.backup.IDMappings;
import me.eglp.gv2.util.lang.DefaultLocaleString;
import me.eglp.gv2.util.webinterface.js.JavaScriptValue;
import me.eglp.gv2.util.webinterface.js.WebinterfaceObject;
import me.mrletsplay.mrcore.http.HttpRequest;
import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONConvertible;
import me.mrletsplay.mrcore.json.converter.JSONValue;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.RoleIcon;
import net.dv8tion.jda.api.managers.RoleManager;
import net.dv8tion.jda.api.requests.restaction.RoleAction;

public class BackupRole implements JSONConvertible, WebinterfaceObject {

	@JavaScriptValue(getter = "getID")
	@JSONValue
	private String id;

	@JavaScriptValue(getter = "getName")
	@JSONValue
	private String name;

	@JSONValue
	private boolean
		isPublic,
		hoisted,
		mentionable;

	@JSONValue
	private long permissions;

	@JavaScriptValue(getter = "getColor")
	@JSONValue
	private int color;

	@JavaScriptValue(getter = "getIconEmoji")
	@JSONValue
	private String iconEmoji;

	@JavaScriptValue(getter = "getIconImage")
	@JSONValue
	private String iconImage;

	@JSONConstructor
	private BackupRole() {}

	public BackupRole(GraphiteRole role) {
		this.id = role.getID();
		this.name = role.getName();
		this.isPublic = role.isPublicRole();
		this.hoisted = role.getJDARole().isHoisted();
		this.mentionable = role.getJDARole().isMentionable();
		this.permissions = role.getJDARole().getPermissionsRaw();
		this.color = role.getJDARole().getColorRaw();

		RoleIcon icon = role.getJDARole().getIcon();
		if(icon != null) {
			this.iconEmoji = icon.getEmoji();
			try {
				String url = icon.getIconUrl();
				if(url != null) {
					byte[] bytes = HttpRequest.createGet(url).execute().asRaw();
					this.iconImage = Base64.getEncoder().encodeToString(bytes);
				}
			}catch(Exception ignored) {}
		}
	}

	public String getID() {
		return id;
	}

	public String getName() {
		return name;
	}

	public boolean isPublic() {
		return isPublic;
	}

	public boolean isHoisted() {
		return hoisted;
	}

	public boolean isMentionable() {
		return mentionable;
	}

	public long getPermissions() {
		return permissions;
	}

	public int getColor() {
		return color;
	}

	public String getIconEmoji() {
		return iconEmoji;
	}

	public String getIconImage() {
		return iconImage;
	}

	public boolean restore(GraphiteGuild guild, IDMappings mappings) {
		if(isPublic) {
			Role r = guild.getJDAGuild().getPublicRole();
			if(!guild.getSelfMember().getMember().canInteract(r)) return true;
			RoleManager m = r.getManager();
			m.setName(name);
			m.setHoisted(hoisted);
			m.setMentionable(mentionable);
			m.setPermissions(permissions);
			m.setColor(color);
			m.queue();
		}else {
			RoleAction r = guild.getJDAGuild().createRole();
			r.setName(name);
			r.setHoisted(hoisted);
			r.setMentionable(mentionable);
			r.setPermissions(permissions);
			r.setColor(color);

			if(iconEmoji != null) {
				r.setIcon(iconEmoji);
			}else if(iconImage != null){
				r.setIcon(Icon.from(Base64.getDecoder().decode(iconImage)));
			}

			CompletableFuture<Role> createRole = r.timeout(10, TimeUnit.SECONDS).submit();
			Role role;
			try {
				role = createRole.get();
			} catch (InterruptedException | ExecutionException e) {
				if(e.getCause() instanceof TimeoutException) {
					GraphiteMember owner = guild.getMember(guild.getJDAGuild().getOwner());
					EmbedBuilder eb = new EmbedBuilder();
					eb.setTitle(DefaultLocaleString.ERROR_HIT_ROLE_RATELIMIT_TITLE.getFor(owner));
					eb.setDescription(DefaultLocaleString.ERROR_HIT_ROLE_RATELIMIT_CONTENT.getFor(owner));
					eb.setFooter(DefaultLocaleString.ERROR_HIT_ROLE_RATELIMIT_FOOTER.getFor(owner));
					owner.openPrivateChannel().sendMessage(eb.build());
					return false;
				}

				GraphiteDebug.log(DebugCategory.BACKUP, e);
				return false;
			}

			if(role != null) { // Check null because of 250 role limit
				mappings.put(id, role.getId());
			}

		}

		return true;
	}

}

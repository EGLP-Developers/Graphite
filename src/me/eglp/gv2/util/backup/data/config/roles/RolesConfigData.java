package me.eglp.gv2.util.backup.data.config.roles;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import me.eglp.gv2.guild.GraphiteGuild;
import me.eglp.gv2.guild.config.GuildRolesConfig;
import me.eglp.gv2.util.backup.IDMappings;
import me.eglp.gv2.util.backup.RestoreSelector;
import me.mrletsplay.mrcore.json.JSONType;
import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONConvertible;
import me.mrletsplay.mrcore.json.converter.JSONListType;
import me.mrletsplay.mrcore.json.converter.JSONValue;

public class RolesConfigData implements JSONConvertible {

	@JSONValue
	@JSONListType(JSONType.STRING)
	private List<String> accessibleRoles;

	@JSONValue
	@JSONListType(JSONType.STRING)
	private List<String> autoRoles;

	@JSONValue
	@JSONListType(JSONType.STRING)
	private List<String> botRoles;

	@JSONValue
	@JSONListType(JSONType.STRING)
	private List<String> moderatorRoles;

	@JSONConstructor
	private RolesConfigData() {}

	public RolesConfigData(GraphiteGuild guild) {
		GuildRolesConfig c = guild.getRolesConfig();
		this.accessibleRoles = c.getAccessibleRoles().stream()
				.map(r -> r.getID())
				.collect(Collectors.toList());
		this.autoRoles = c.getAutoRoles().stream()
				.map(r -> r.getID())
				.collect(Collectors.toList());
		this.botRoles = c.getBotRoles().stream()
				.map(r -> r.getID())
				.collect(Collectors.toList());
		this.moderatorRoles = c.getModeratorRoles().stream()
				.map(r -> r.getID())
				.collect(Collectors.toList());
	}

	public void restore(GraphiteGuild guild, EnumSet<RestoreSelector> selectors, IDMappings mappings) {
		GuildRolesConfig c = guild.getRolesConfig();

		if(RestoreSelector.ROLE_MANAGEMENT.appliesTo(selectors)) {
			c.setAccessibleRoles(accessibleRoles.stream()
					.map(mappings::getNewID)
					.map(guild::getRoleByID)
					.filter(Objects::nonNull)
					.collect(Collectors.toList()));

			c.setAutoRoles(autoRoles.stream()
					.map(guild::getRoleByID)
					.filter(Objects::nonNull)
					.collect(Collectors.toList()));

			c.setBotRoles(botRoles.stream()
					.map(guild::getRoleByID)
					.filter(Objects::nonNull)
					.collect(Collectors.toList()));
		}

		if(RestoreSelector.MODERATION_AUTOMOD.appliesTo(selectors)) {
			c.setModeratorRoles(moderatorRoles.stream()
					.map(mappings::getNewID)
					.map(guild::getRoleByID)
					.filter(Objects::nonNull)
					.collect(Collectors.toList()));
		}
	}

}

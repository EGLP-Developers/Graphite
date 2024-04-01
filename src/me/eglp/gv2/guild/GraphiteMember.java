package me.eglp.gv2.guild;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.user.GraphiteUser;
import me.eglp.gv2.util.permission.MemberPermissions;
import me.eglp.gv2.util.webinterface.js.JavaScriptClass;
import me.eglp.gv2.util.webinterface.js.JavaScriptFunction;
import me.eglp.gv2.util.webinterface.js.JavaScriptGetter;
import me.eglp.gv2.util.webinterface.js.WebinterfaceObject;
import me.mrletsplay.mrcore.json.JSONObject;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.UserSnowflake;

@JavaScriptClass(name = "Member")
public class GraphiteMember extends GraphiteUser implements WebinterfaceObject {

	private String memberID;
	private Member member;
	private GraphiteGuild guild;

	public GraphiteMember(Member member, GraphiteGuild guild) {
		super(member.getUser());
		memberID = member.getId();
		this.member = member;
		this.guild = guild;
	}

	public Member getMember() {
		return member;
	}

	public GraphiteUser getUser() {
		return Graphite.getUser(getMember().getUser());
	}

	public GraphiteGuild getGuild() {
		return guild;
	}

	@Override
	@JavaScriptGetter(name = "getID", returning = "memberID")
	public String getID() {
		return memberID;
	}

	@Override
	@JavaScriptGetter(name = "getName", returning = "memberName")
	public String getName() {
		return getJDAUser().getName();
	}

	@Override
	@Deprecated
	@JavaScriptGetter(name = "getDiscriminator", returning = "memberDiscriminator")
	public String getDiscriminator() {
		return getJDAUser().getDiscriminator();
	}

	public MemberPermissions getMemberPermissions() {
		return guild.getPermissionManager().getPermissions(this);
	}

	public boolean isOwner() {
		return getMember().isOwner();
	}

	public List<GraphiteRole> getRoles() {
		return getMember().getRoles().stream().map(r -> guild.getRole(r)).collect(Collectors.toList());
	}

	public GraphiteAudioChannel getCurrentAudioChannel() {
		if(getMember().getVoiceState() == null || !getMember().getVoiceState().inAudioChannel()) return null;
		return Graphite.getAudioChannel(getMember().getVoiceState().getChannel());
	}

	public boolean canInteract(GraphiteMember other) {
		return getMember().canInteract(other.getMember());
	}

	public boolean canInteract(GraphiteRole other) {
		return getMember().canInteract(other.getJDARole());
	}

	public boolean isBanned() {
		return guild.isBanned(this.getID());
	}

	public void unban() {
		guild.getJDAGuild().unban(UserSnowflake.fromId(this.getID())).complete();
	}

	public boolean isGuildMuted() {
		GuildVoiceState s = getMember().getVoiceState();
		if(s == null) return false;
		return s.isGuildMuted();
	}

	public boolean isMuted() {
		GuildVoiceState s = getMember().getVoiceState();
		if(s == null) return false;
		return s.isMuted();
	}

	public void unmute() {
		getMember().mute(false).queue();
	}

	public EnumSet<Permission> getPermissions(GraphiteGuildChannel channel) {
		return getMember().getPermissions(channel.getJDAChannel());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(memberID);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		GraphiteMember other = (GraphiteMember) obj;
		return Objects.equals(memberID, other.memberID);
	}

	@Override
	public void preSerializeWI(JSONObject object) {
		object.put("memberID", getID());
		object.put("memberName", getName());
		object.put("memberDiscriminator", getDiscriminator());
	}

	@JavaScriptFunction(calling = "getMembers", returning = "members", withGuild = true)
	public static void getMembers() {};

}

package me.eglp.gv2.util.base.guild;

import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.base.user.GraphiteUser;
import me.eglp.gv2.util.jdaobject.JDAObject;
import me.eglp.gv2.util.permission.MemberPermissions;
import me.eglp.gv2.util.webinterface.js.JavaScriptClass;
import me.eglp.gv2.util.webinterface.js.JavaScriptFunction;
import me.eglp.gv2.util.webinterface.js.JavaScriptGetter;
import me.eglp.gv2.util.webinterface.js.WebinterfaceObject;
import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.mrcore.misc.FriendlyException;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.UserSnowflake;

@JavaScriptClass(name = "Member")
public class GraphiteMember extends GraphiteUser implements WebinterfaceObject {

	private String memberID;
	private JDAObject<Member> jdaMember;
	private GraphiteGuild guild;
	
	public GraphiteMember(Member member, GraphiteGuild guild) {
		super(member.getUser());
		memberID = member.getId();
		String gID = member.getGuild().getId();
		this.jdaMember = new JDAObject<>(jda -> jda.getGuildById(gID) == null ? null : jda.getGuildById(gID).retrieveMemberById(memberID).complete(), o -> o.getId().equals(memberID) && o.getGuild().getId().equals(gID));
		this.guild = guild;
	}
	
	@Override
	public boolean isAvailable() {
		return super.isAvailable() && jdaMember.isAvailable();
	}
	
	public JDAObject<Member> getJDAMemberObject() {
		return jdaMember;
	}
	
	public Member getJDAMember() {
		return jdaMember.get();
	}
	
	public GraphiteUser getUser() {
		return Graphite.getUser(getJDAMember().getUser());
	}
	
	public GraphiteGuild getGuild() {
		return guild;
	}
	
	@JavaScriptGetter(name = "getID", returning = "memberID")
	public String getID() {
		return memberID;
	}
	
	@JavaScriptGetter(name = "getName", returning = "memberName")
	public String getName() {
		return getUser().getName();
	}
	
	@JavaScriptGetter(name = "getDiscriminator", returning = "memberDiscriminator")
	public String getDiscriminator() {
		return getUser().getDiscriminator();
	}
	
	public MemberPermissions getMemberPermissions() {
		return guild.getPermissionManager().getPermissions(this);
	}
	
	public boolean isOwner() {
		return getJDAMember().isOwner();
	}
	
	public List<GraphiteRole> getRoles() {
		return getJDAMember().getRoles().stream().map(r -> guild.getRole(r)).collect(Collectors.toList());
	}
	
	public GraphiteAudioChannel getCurrentAudioChannel() {
		if(!isAvailable()) throw new FriendlyException("Member is not available (in this context)");
		if(getJDAMember().getVoiceState() == null || !getJDAMember().getVoiceState().inAudioChannel()) return null;
		return Graphite.getAudioChannel(getJDAMember().getVoiceState().getChannel());
	}
	
	public boolean canInteract(GraphiteMember other) {
		return getJDAMember().canInteract(other.getJDAMember());
	}
	
	public boolean canInteract(GraphiteRole other) {
		return getJDAMember().canInteract(other.getJDARole());
	}
	
	public boolean isBanned() {
		return guild.isBanned(this.getID());
	}
	
	public void unban() {
		guild.getJDAGuild().unban(UserSnowflake.fromId(this.getID())).complete();
	}
	
	public boolean isGuildMuted() {
		GuildVoiceState s = getJDAMember().getVoiceState();
		if(s == null) return false;
		return s.isGuildMuted();
	}
	
	public boolean isMuted() {
		GuildVoiceState s = getJDAMember().getVoiceState();
		if(s == null) return false;
		return s.isMuted();
	}
	
	public void unmute() {
		getJDAMember().mute(false).queue();
	}
	
	public EnumSet<Permission> getPermissions(GraphiteGuildChannel channel) {
		return getJDAMember().getPermissions(channel.getJDAChannel());
	}
	
	@Override
	public boolean equals(Object o) {
		if(!(o instanceof GraphiteMember)) return false;
		return super.equals(o) && guild.getID().equals(((GraphiteMember) o).getGuild().getID());
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

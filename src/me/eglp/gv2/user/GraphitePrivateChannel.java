package me.eglp.gv2.user;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.base.GraphiteMessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public class GraphitePrivateChannel implements GraphiteMessageChannel<GraphiteUser> {

	private GraphiteUser user;
	private PrivateChannel channel;

	public GraphitePrivateChannel(PrivateChannel jdaChannel) {
		this.user = Graphite.getUser(jdaChannel.getUser());
		this.channel = user.getJDAUser().openPrivateChannel().complete();
	}

	@Override
	public PrivateChannel getJDAChannel() {
		return channel;
	}

	public GraphiteUser getUser() {
		return user;
	}

	@Override
	public GraphiteUser getOwner() {
		return user;
	}

	@Override
	public void sendMessage(MessageEmbed message) {
		sendMessage(message, t -> {}); // PrivateChannels don't need to print errors as there's no alternative anyway
	}

	@Override
	public void sendMessage(MessageCreateData message) {
		sendMessage(message, t -> {});
	}

	@Override
	public void sendMessage(String message, String... params) {
		sendMessage(message, t -> {}, params);
	}

	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof GraphitePrivateChannel)) return false;
		return user.equals(((GraphitePrivateChannel) obj).getUser());
	}

}

package me.eglp.gv2.user;

import java.util.Objects;

import me.eglp.gv2.main.DebugCategory;
import me.eglp.gv2.main.GraphiteDebug;
import me.eglp.gv2.util.base.GraphiteLocalizable;
import me.eglp.gv2.util.base.GraphiteOwning;
import me.eglp.gv2.util.command.CommandSender;
import me.eglp.gv2.util.lang.GraphiteLocale;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.ErrorResponse;

public class GraphiteUser implements CommandSender, GraphiteOwning, GraphiteLocalizable{

	private String userID;
	private User user;
	private UserConfig config;

	public GraphiteUser(User user) {
		userID = user.getId();
		this.user = user;
		this.config = new UserConfig(this);
	}

	public User getJDAUser() {
		return user;
	}

	public UserConfig getConfig() {
		return config;
	}

	/**
	 * Returns the user's username, or if the discriminator is not <code>0000</code>, the username with the discriminator appended to it (e.g. <code>user#1234</code>)
	 * @return The user's full username
	 */
	public String getFullName() {
		String disc = getDiscriminator();
		if(disc.equals("0000")) return getName();
		return getName() + "#" + disc;
	}

	public String getName() {
		return getJDAUser().getName();
	}

	@Deprecated
	public String getDiscriminator() {
		return getJDAUser().getDiscriminator();
	}

	public String getAsMention() {
		return getJDAUser().getAsMention();
	}

	@Override
	public String getID() {
		return userID;
	}

	public boolean isBot() {
		return getJDAUser().isBot();
	}

	public GraphitePrivateChannel openPrivateChannel() {
		try {
			return new GraphitePrivateChannel(getJDAUser().openPrivateChannel().complete());
		}catch(Exception e) {
			if(e instanceof ErrorResponseException) {
				ErrorResponse r = ((ErrorResponseException) e).getErrorResponse();
				if(r == ErrorResponse.CANNOT_SEND_TO_USER) return null;
			}

			GraphiteDebug.log(DebugCategory.JDA, e);
			return null;
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(userID);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GraphiteUser other = (GraphiteUser) obj;
		return Objects.equals(userID, other.userID);
	}

	@Override
	public GraphiteLocale getLocale() {
		return UserFallbackLocale.INSTANCE;
	}

}

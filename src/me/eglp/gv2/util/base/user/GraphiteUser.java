package me.eglp.gv2.util.base.user;

import java.util.List;
import java.util.stream.Collectors;

import me.eglp.gv2.main.DebugCategory;
import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.main.GraphiteDebug;
import me.eglp.gv2.util.base.GraphiteLocalizable;
import me.eglp.gv2.util.base.GraphiteOwning;
import me.eglp.gv2.util.command.CommandSender;
import me.eglp.gv2.util.jdaobject.JDAObject;
import me.eglp.gv2.util.lang.GraphiteLocale;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.ErrorResponse;

public class GraphiteUser implements CommandSender, GraphiteOwning, GraphiteLocalizable {

	private String userID;
	private JDAObject<User> jdaUser;
	private UserConfig config;

	public GraphiteUser(User user) {
		userID = user.getId();
		this.jdaUser = new JDAObject<>(jda -> jda.getUserById(userID), jda -> retrieveUser(jda, userID), o -> o.getId().equals(userID));
		this.config = new UserConfig(this);
	}

	private User retrieveUser(JDA jda, String userID) {
		try {
			return jda.retrieveUserById(userID).complete();
		}catch(ErrorResponseException e) {
			if(e.getErrorResponse() == ErrorResponse.UNKNOWN_USER) return null;
			throw e;
		}
	}

	public boolean isAvailable() {
		return jdaUser.isAvailable();
	}

	public JDAObject<User> getJDAUserObject() {
		return jdaUser;
	}

	public User getJDAUser() {
		return jdaUser.get();
	}

	public UserConfig getConfig() {
		return config;
	}

	public String getName() {
		return getJDAUser().getName();
	}

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
	public boolean equals(Object o) {
		if(!(o instanceof GraphiteUser)) return false;
		return getID().equals(((GraphiteUser) o).getID());
	}

	@Override
	public GraphiteLocale getLocale() {
		return UserFallbackLocale.INSTANCE;
	}

}

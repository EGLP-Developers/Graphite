package me.eglp.gv2.multiplex;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.multiplex.bot.MultiplexBot;
import me.eglp.gv2.user.GraphiteUser;
import me.mrletsplay.mrcore.misc.LookupList;
import me.mrletsplay.mrcore.misc.SingleLookupList;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.ErrorResponse;

public class MultiplexCache {

	private static List<GraphiteUser> allCachedUsers = new ArrayList<>();

	private MultiplexBot bot;

	private LookupList<String, GraphiteUser> cachedUsers;

	public MultiplexCache(MultiplexBot bot) {
		this.bot = bot;
		this.cachedUsers = new SingleLookupList<>(GraphiteUser::getID);
	}

	public GraphiteUser getUser(String id) {
		if(id == null) return null;

		GraphiteUser u = cachedUsers.lookup(id);
		if(u != null) return u;

		return getUser(getJDAUser(id));
	}

	public synchronized GraphiteUser getUser(User user) {
		if(user == null) return null;
//		if(bot.getShards().stream().noneMatch(s -> s.getJDA().equals(user.getJDA()))) return null;

		GraphiteUser u = cachedUsers.stream().filter(c -> c.getJDAUserObject().isSameObject(user)).findFirst().orElse(null);
		if(u == null) {
			u = newGraphiteUser(user);
			cachedUsers.add(u);
		}
		return u;
	}

	public User getJDAUser(String id) {
		if(!Graphite.isValidSnowflake(id)) return null;

		return bot.getShards().stream()
				.map(s -> s.getJDA().retrieveUserById(id).onErrorMap(ErrorResponse.UNKNOWN_USER::test, e -> null).complete())
				.filter(Objects::nonNull)
				.findFirst().orElse(null);
	}

	private static synchronized GraphiteUser newGraphiteUser(User user) {
		GraphiteUser u = allCachedUsers.stream()
				.filter(o -> o.getJDAUserObject().isSameObject(user))
				.findFirst().orElse(null);

		if(u == null) {
			u = new GraphiteUser(user);
			allCachedUsers.add(u);
		}

		return u;
	}

}

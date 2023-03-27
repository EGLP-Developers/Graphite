package me.eglp.gv2.util.jdaobject;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import me.eglp.gv2.multiplex.GraphiteMultiplex;
import me.eglp.gv2.multiplex.MultiplexException;
import me.eglp.gv2.multiplex.bot.MultiplexBot;
import net.dv8tion.jda.api.JDA;

public class JDAObject<T> {
	
	private static GlobalJDAObjectCache globalCache = new GlobalJDAObjectCache();
	
	private Function<JDA, T>
		getter, // Light action to just load a value from cache if it is cached
		retrieveFunction; // Heavy action, used when the getter returns null
	
	private Predicate<T> jdaEqualsMethod;
	
	public JDAObject(Function<JDA, T> getter, Function<JDA, T> retrieveFunction, Predicate<T> jdaEqualsMethod) {
		this.getter = getter;
		this.retrieveFunction = retrieveFunction;
		this.jdaEqualsMethod = jdaEqualsMethod == null ? other -> get().equals(other) : jdaEqualsMethod;
	}
	
	public JDAObject(Function<JDA, T> getter, Predicate<T> jdaEqualsMethod) {
		this(getter, null, jdaEqualsMethod);
	}
	
	private T load() throws MultiplexException {
		MultiplexBot bot = GraphiteMultiplex.getCurrentBot();
		
		if(bot == null) throw new MultiplexException("load() called outside of any Multiplex scope on thread " + Thread.currentThread().getName());
		T t = bot.getShards().stream() // Then try getting the cached value using all bot JDAs (due to DMs always being on shard 0, some users may be unavailable)
					.map(s -> getter.apply(s.getJDA()))
					.filter(e -> e != null)
					.findFirst().orElse(null);
		
		if(t == null) {
			if(retrieveFunction == null) throw new MultiplexException("Object not available in Multiplex context of bot " + bot.getIdentifier() + " and no retrieve function is defined");
			
			t = bot.getShards().stream() // If the value isn't cached, try retrieving it from any of the shards
					.map(s -> retrieveFunction.apply(s.getJDA()))
					.filter(e -> e != null)
					.findFirst().orElse(null);
		}
		
		if(t == null) throw new MultiplexException("Object not available in Multiplex context of bot " + bot.getIdentifier());
		globalCache.putCachedObject(this, bot, t);
		return t;
	}
	
	@SuppressWarnings("unchecked")
	public T get() throws MultiplexException {
		T t = (T) globalCache.getCachedObject(this, GraphiteMultiplex.getCurrentBot());
		if(t == null) t = load(); // Update cached object if bot has changed
		return t;
	}
	
	public boolean isAvailable() {
		try {
			get();
			return true;
		}catch(MultiplexException e) {
			return false;
		}
	}
	
	public boolean isSameObject(T other) {
		return jdaEqualsMethod.test(other);
	}
	
	public JDAObjectCache getCurrentCache() {
		return globalCache.getObjectCache(this);
	}
	
	public static void clearCurrentCache() {
		globalCache.clearCurrentCache();
	}
	
	public static class JDAObjectCache {
		
		private Map<MultiplexBot, Object> cache;
		
		public JDAObjectCache() {
			this.cache = new HashMap<>();
		}
		
		public Map<MultiplexBot, Object> getCache() {
			return cache;
		}
		
		public Object get(MultiplexBot bot) {
			return cache.get(bot);
		}
		
		public void put(MultiplexBot bot, Object value) {
			cache.put(bot, value);
		}
		
	}
	
	public static class GlobalJDAObjectCache {
		
		private ThreadLocal<Map<JDAObject<?>, JDAObjectCache>> globalCache;
		
		public GlobalJDAObjectCache() {
			this.globalCache = ThreadLocal.withInitial(HashMap::new);
		}
		
		public JDAObjectCache getObjectCache(JDAObject<?> jdaObject) {
			JDAObjectCache c = globalCache.get().get(jdaObject);
			if(c == null) {
				c = new JDAObjectCache();
				globalCache.get().put(jdaObject, c);
			}
			return c;
		}
		
		public Object getCachedObject(JDAObject<?> jdaObject, MultiplexBot bot) {
			return getObjectCache(jdaObject).get(bot);
		}
		
		public void putCachedObject(JDAObject<?> jdaObject, MultiplexBot bot, Object value) {
			getObjectCache(jdaObject).put(bot, value);
		}
		
		public void clearCurrentCache() {
			globalCache.remove();
		}
	}
	
}

package me.eglp.gv2.util.base.guild.config;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import me.eglp.gv2.util.base.guild.GraphiteGuildChannel;
import me.mrletsplay.mrcore.misc.FriendlyException;
import net.dv8tion.jda.api.entities.channel.ChannelType;

public interface IGuildConfig {
	
	public default void discardChannel(GraphiteGuildChannel channel) {
		for(Method m : getClass().getDeclaredMethods()) {
			ChannelRemoveListener l = m.getAnnotation(ChannelRemoveListener.class);
			if(l == null) continue;
			if(m.getParameterTypes().length != 1 || !GraphiteGuildChannel.class.isAssignableFrom(m.getParameterTypes()[0])) {
				throw new FriendlyException("Invalid method signature for @ChannelRemoveListener method " + m.getName() + " of class " + getClass());
			}
			
			ChannelType[] types = l.channelTypes();
			if((types.length == 0 || Arrays.asList(types).contains(channel.getJDAChannel().getType()))
				&& m.getParameterTypes()[0].isInstance(channel)) {
				try {
					m.setAccessible(true);
					m.invoke(this, channel);
				} catch (IllegalAccessException | InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		}
	}

}

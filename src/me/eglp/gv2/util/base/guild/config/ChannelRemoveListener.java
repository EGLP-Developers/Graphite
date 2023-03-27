package me.eglp.gv2.util.base.guild.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.dv8tion.jda.api.entities.channel.ChannelType;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ChannelRemoveListener {
	
	public ChannelType[] channelTypes() default {};

}

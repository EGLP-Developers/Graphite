package me.eglp.gv2.util.webinterface;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import me.eglp.gv2.multiplex.GraphiteFeature;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface WebinterfaceHandler {

	public String requestMethod();
	
	public boolean requireBot() default false;
	
	public boolean requireGuild() default false;
	
	public boolean requireGuildAdmin() default false;
	
	public GraphiteFeature[] requireFeatures() default {};
	
}

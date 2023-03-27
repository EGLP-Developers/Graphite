package me.eglp.gv2.util.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SpecialExecute {

	public boolean allowServer() default true;
	
	public boolean allowPrivate() default false;
	
	public boolean allowInGame() default false;
	
	public boolean bypassQueue() default false;
	
}

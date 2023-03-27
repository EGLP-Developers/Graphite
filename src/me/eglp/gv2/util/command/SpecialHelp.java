package me.eglp.gv2.util.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SpecialHelp {

	public boolean hideSelf() default false;
	
	public boolean hideSubCommands() default false;
	
}

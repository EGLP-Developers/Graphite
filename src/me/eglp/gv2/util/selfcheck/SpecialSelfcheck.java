package me.eglp.gv2.util.selfcheck;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SpecialSelfcheck {

	public boolean needsPermission() default true;
	
	public boolean ignoreAccessibleToEveryone() default false;
	
}

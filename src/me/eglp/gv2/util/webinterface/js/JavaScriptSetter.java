package me.eglp.gv2.util.webinterface.js;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface JavaScriptSetter {

	public String name() default "";
	
	public String setting();
	
}

package me.eglp.gv2.util.webinterface;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface WebinterfaceHandler {

	public String requestMethod();

	public boolean requireGuild() default false;

	public boolean requireGuildAdmin() default false;

	public String[] requirePermissions() default {};

}

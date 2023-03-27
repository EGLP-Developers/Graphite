package me.eglp.gv2.util.mysql;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(SQLTables.class)
public @interface SQLTable {
	
	String name();
	
	String[] columns();
	
	String charset() default "utf8mb4";
	
	String collation() default "utf8mb4_general_ci";
	
	String guildReference() default "";

}

package top.srintelligence.wallpaper_generator.lookup_kernel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Untested {
    String value() default "THIS CODE HAS NOT BEEN TESTED!!!";
}
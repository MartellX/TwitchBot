package command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CommandAnnotation {
    String name() ;
    String[] names() default {};
    int id() default -1;
    int delay() default -1;
    CommandType type() default CommandType.OTHER;
    String[] permissions() default {};
    boolean isPaused() default false;
    boolean isDisabled() default false;

}

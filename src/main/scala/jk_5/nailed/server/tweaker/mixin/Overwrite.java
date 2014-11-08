package jk_5.nailed.server.tweaker.mixin;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Used to indicate a Mixin class member which should overwrite a method in the target class
 */
@Target(METHOD)
@Retention(RUNTIME)
public @interface Overwrite {

}

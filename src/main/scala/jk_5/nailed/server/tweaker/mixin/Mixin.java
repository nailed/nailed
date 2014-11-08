package jk_5.nailed.server.tweaker.mixin;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * Decorator for mixin classes
 */
@Target(TYPE)
@Retention(CLASS)
public @interface Mixin {

    /**
     * Target class(es) for this mixin
     */
    public Class<?>[] value();

    /**
     * Priority for the mixin, relative to other mixins targetting the same classes
     */
    public int priority() default 1000;
}

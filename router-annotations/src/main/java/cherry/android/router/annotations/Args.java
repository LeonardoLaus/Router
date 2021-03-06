package cherry.android.router.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Administrator on 2017/6/8.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
@Documented
public @interface Args {
    String name() default "";

    boolean nonNull() default false;
}

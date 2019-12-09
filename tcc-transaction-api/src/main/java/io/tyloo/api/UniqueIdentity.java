package io.tyloo.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/*
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 15:06 2019/12/4
 *
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface UniqueIdentity {
}

package io.tyloo.api.Annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/*
 *
 *  唯一标识
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 15:06 2019/12/4
 *
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface UniqueIdentity {
}

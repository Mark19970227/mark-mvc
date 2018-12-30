package com.mark.annotation;

import java.lang.annotation.*;

/**
 * 名称:request参数注解
 * Created with IntelliJ IDEA.
 * User: IT666_Gj
 */
@Target(ElementType.PARAMETER) // 作用范围
@Retention(RetentionPolicy.RUNTIME) // 系统运行时,通过反射获取信息
@Documented // javadoc
public @interface AnnMarkRequestParam {

    // 表示字符可以为null
    String value() default "";
}

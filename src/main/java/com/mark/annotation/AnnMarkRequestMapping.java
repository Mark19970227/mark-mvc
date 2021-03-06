package com.mark.annotation;

import java.lang.annotation.*;

/**
 * 名称:注入注解
 * Created with IntelliJ IDEA.
 * User: IT666_Gj
 */
@Target({ElementType.TYPE,ElementType.METHOD}) // 作用范围
@Retention(RetentionPolicy.RUNTIME) // 系统运行时,通过反射获取信息
@Documented // javadoc
public @interface AnnMarkRequestMapping {

    // 表示字符可以为null
    String value() default "";
}

package com.ecomove.common.validation;


import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target({})
@Retention(RetentionPolicy.RUNTIME)
public @interface I18nField {
    String vi() default "";
    String en() default "";
}
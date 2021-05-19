package com.devexperts.switchboard.integrations.test.example;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})
public @interface TestCase {
    String summary() default "";

    String severity() default "";

    String[] components() default {};

    String[] labels() default {};

    String tradingType() default "";
}

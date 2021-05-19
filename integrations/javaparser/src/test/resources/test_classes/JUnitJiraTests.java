package com.devexperts.switchboard.integrations.test.example;

import org.junit.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@TestCase(severity = "Functional", components = "Component1", tradingType = "None")
public class JUnitJiraTests {

    @Test
    @TestCase(
            summary = "This is some test summary",
            labels = {"One", "Two", "Three"}
    )
    public void test1() {
        // STEP: do something 1
        assert true;
        // STEP: do something else 1
        assert true;
        // STEP: and another thing 1
        assert true;
    }

    @Test
    @TestCase(
            summary = "This is another test summary",
            labels = {"Four"},
            severity = "Showstopper"
    )
    public void test2() {
        // STEP: do something 2
        assert true;
        // STEP: do something else 2
        // STEP: and another thing 2
        assert true;
    }

    @Test
    @TestCase(
            summary = "Guess what? Another test summary!",
            labels = {"Five", "Six"},
            severity = "Minor functional"
    )
    public void test3() {
        assert true;
    }

    @Target({ElementType.TYPE, ElementType.METHOD})
    public @interface TestCase {
        String summary() default "";

        String severity() default "";

        String[] components() default {};

        String[] labels() default {};

        String tradingType() default "";
    }
}

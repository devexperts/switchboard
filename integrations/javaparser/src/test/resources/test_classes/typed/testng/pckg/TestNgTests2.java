package com.devexperts.switchboard.integrations.javaparser.testng.pckg;

import com.devexperts.switchboard.integrations.javaparser.Annotations;
import org.testng.annotations.Test;

public class TestNgTests2 {

    @Test
    @Annotations.MarkerAnnotation1
    @Annotations.ValueAnnotation1("qwerty")
    @Annotations.MultiValueAnnotation(stringVal = "multival", intVal = 3, arrVal = {"a1", "a2", "a3"})
    public void test1() {
        assert true;
    }

    @Test
    @Annotations.MarkerAnnotation2
    @Annotations.ValueAnnotation2(42)
    public void test2() {
        assert true;
    }

    @Test
    @Annotations.MarkerAnnotation1
    @Annotations.MarkerAnnotation2
    @Annotations.ValueAnnotation1("abc")
    @Annotations.ValueAnnotation2(7)
    @Annotations.MultiValueAnnotation(stringVal = "multivalX", intVal = 2, arrVal = {"a1", "a2"})
    public void test3() {
        assert true;
    }
}
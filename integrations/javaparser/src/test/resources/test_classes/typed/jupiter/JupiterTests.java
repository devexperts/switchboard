package com.devexperts.switchboard.integrations.javaparser.jupiter;

import com.devexperts.switchboard.integrations.javaparser.Annotations;
import com.devexperts.switchboard.integrations.javaparser.Annotations.MarkerAnnotation1;
import com.devexperts.switchboard.integrations.javaparser.Annotations.MarkerAnnotation2;
import com.devexperts.switchboard.integrations.javaparser.Annotations.MultiValueAnnotation;
import com.devexperts.switchboard.integrations.javaparser.Annotations.ValueAnnotation1;
import com.devexperts.switchboard.integrations.javaparser.Annotations.ValueAnnotation2;
import org.junit.jupiter.api.Test;

@Annotations.ValueAnnotation1("common")
@ValueAnnotation2(0)
public class JupiterTests {

    @Test
    @MarkerAnnotation1
    @ValueAnnotation1("qwerty")
    @MultiValueAnnotation(stringVal = "multival", intVal = 3, arrVal = {"a1", "a2", "a3"})
    public void test1() {
        assert true;
    }

    @Test
    @MarkerAnnotation2
    @ValueAnnotation2(42)
    public void test2() {
        assert true;
    }

    @Test
    @MarkerAnnotation1
    @MarkerAnnotation2
    @ValueAnnotation1("abc")
    @ValueAnnotation2(7)
    @MultiValueAnnotation(stringVal = "multivalX", intVal = 2, arrVal = {"a1", "a2"})
    public void test3() {
        assert true;
    }
}

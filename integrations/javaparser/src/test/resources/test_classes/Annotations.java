package com.devexperts.switchboard.integrations.javaparser;

public class Annotations {
    public @interface MarkerAnnotation1 {}

    public @interface MarkerAnnotation2 {}

    public @interface ValueAnnotation1 {
        public String value();
    }

    public @interface ValueAnnotation2 {
        public int value();
    }

    public @interface MultiValueAnnotation {
        public String stringVal();
        public int intVal() default 10;
        public String[] arrVal();
    }
}

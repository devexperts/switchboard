package com.devexperts.switchboard.integrations.bytecodeparser;

import com.devexperts.switchboard.api.IntegrationFeatures;
import com.devexperts.switchboard.entities.Attributes;
import com.devexperts.switchboard.entities.Test;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.function.Function;

import static com.machinezoo.noexception.Exceptions.sneak;

/**
 * Implementation of {@link IntegrationFeatures} providing access to {@link ClassPath} functionality
 */
public class BytecodeParserIntegrationFeatures implements IntegrationFeatures {
    public static final String LOCATION_PACKAGE_KEY = "package";
    public static final String LOCATION_CLASS_KEY = "class";
    public static final String LOCATION_METHOD_KEY = "method";
    public static final Function<Test, String> TEST_TO_RUNNABLE_STRING = test -> formatTestPath(test.getAttributes());

    BytecodeParserIntegrationFeatures() {}

    public ImmutableSet<ClassPath.ClassInfo> parse(File file) {
        ImmutableSet<ClassPath.ClassInfo> allClasses = sneak().get(() -> ClassPath.from(new URLClassLoader(new URL[] {file.toURI().toURL()}, null))
                .getAllClasses());
        if (allClasses.isEmpty()) {
            throw new IllegalStateException("Parse result for '" + file.toString() + " is empty");
        }
        return allClasses;
    }

    @Override
    public void close() {/*do nothing*/}

    public static String formatTestPath(Attributes attributes) {
        return String.format("%s.%s#%s",
                attributes.getSingleAttributeValue(Attributes.LOCATION_PROP, LOCATION_PACKAGE_KEY).orElse(""),
                attributes.getSingleAttributeValue(Attributes.LOCATION_PROP, LOCATION_CLASS_KEY).orElse(""),
                attributes.getSingleAttributeValue(Attributes.LOCATION_PROP, LOCATION_METHOD_KEY).orElse(""));
    }
}

package com.devexperts.switchboard.integrations.bytecodeparser;

import com.devexperts.switchboard.entities.Attributes;
import com.devexperts.switchboard.entities.Pair;
import com.devexperts.switchboard.entities.Test;
import com.devexperts.switchboard.impl.extractors.FileTestExtractor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.machinezoo.noexception.Exceptions.sneak;

/**
 * {@link com.devexperts.switchboard.api.TestExtractor} implementation for extracting tests from compiled sources.
 */
public class BytecodeTestExtractor extends FileTestExtractor<BytecodeParserIntegrationFeatures> {
    private static final String DEFAULT_FILE_PATTERN = ".*\\.jar";
    private static final String TEST_ANNOTATION_PATTERN = "(.*\\.)?Test";

    @JsonProperty(defaultValue = DEFAULT_FILE_PATTERN)
    private String filePattern = DEFAULT_FILE_PATTERN;
    @JsonProperty(defaultValue = TEST_ANNOTATION_PATTERN)
    private String testAnnotationPattern = TEST_ANNOTATION_PATTERN;
    @JsonProperty(defaultValue = "false")
    private boolean storeAnnotationsQualified = false;
    @JsonIgnore
    private BytecodeParserIntegrationFeatures integrationFeatures;

    private BytecodeTestExtractor() {
        super();
    }

    public BytecodeTestExtractor(String identifier, List<String> testLocation, String filePattern, String testAnnotationPattern,
                                 boolean storeAnnotationsQualified) {
        super(identifier, testLocation);
        this.filePattern = filePattern;
        this.testAnnotationPattern = testAnnotationPattern;
        this.storeAnnotationsQualified = storeAnnotationsQualified;
    }

    @Override
    protected String getFilePattern() {
        return filePattern;
    }

    @Override
    public void init(BytecodeParserIntegrationFeatures integrationFeatures) {
        this.integrationFeatures = integrationFeatures;
    }

    private final Map<Class<?>, Attributes> classAttributesCache = new HashMap<>();

    @Override
    protected List<Test> extractTests(File file) {
        Pattern pattern = Pattern.compile(testAnnotationPattern);
        URL[] urls = {sneak().get(() -> file.toURI().toURL())};
        try (URLClassLoader fullClassLoader = new URLClassLoader(urls, Thread.currentThread().getContextClassLoader())) {
            ImmutableSet<ClassPath.ClassInfo> allClasses = integrationFeatures.parse(file);
            return allClasses.stream()
                    .map(c -> sneak().get(() -> fullClassLoader.loadClass(c.getName())))
                    .flatMap(c -> Arrays.stream(c.getDeclaredMethods()))
                    .filter(m -> Arrays.stream(m.getAnnotations()).anyMatch(a -> pattern.matcher(storeAnnotationsQualified ? a.annotationType().getName() : a.annotationType().getSimpleName()).matches()))
                    .map(m -> {
                        Attributes attributes = classAttributesCache.computeIfAbsent(m.getDeclaringClass(), c ->
                                        Attributes.newBuilder()
                                                .putAttributes(parseAnnotationAttributes(c.getAnnotations()))
                                                .build())
                                .toBuilder()
                                .putAttributes(parseAnnotationAttributes(m.getAnnotations()))
                                .putAttribute(Attributes.LOCATION_PROP, BytecodeParserIntegrationFeatures.LOCATION_PACKAGE_KEY, m.getDeclaringClass().getPackage().getName())
                                .mergeAttribute(Attributes.LOCATION_PROP, BytecodeParserIntegrationFeatures.LOCATION_CLASS_KEY, m.getDeclaringClass().getSimpleName())
                                .mergeAttribute(Attributes.LOCATION_PROP, BytecodeParserIntegrationFeatures.LOCATION_METHOD_KEY, m.getName())
                                .build();
                        return new Test(BytecodeParserIntegrationFeatures.formatTestPath(attributes), attributes, BytecodeParserIntegrationFeatures.TEST_TO_RUNNABLE_STRING);
                    }).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    private Map<String, Map<String, Set<String>>> parseAnnotationAttributes(Annotation[] annotations) {
        return Arrays.stream(annotations).map(a ->
                Pair.of(a.annotationType().getCanonicalName(),
                        Arrays.stream(a.annotationType().getDeclaredMethods())
                                .filter(m -> Modifier.isPublic(m.getModifiers()) && !m.isSynthetic())
                                .map(m -> convertField(a, m))
                                .filter(Objects::nonNull)
                                .collect(Collectors.toMap(Pair::getKey, Pair::getValue))
                )).collect(Collectors.toMap(Pair::getKey, Pair::getValue));
    }

    private Pair<String, Set<String>> convertField(Annotation a, Method m) {
        try {
            Object o = m.invoke(a);
            if (Objects.deepEquals(m.getDefaultValue(), o)) {
                return null;
            }
            String name = "value".equals(m.getName()) ? "" : m.getName();
            Set<String> stringSet = (o != null && o.getClass().isArray()) ? Arrays.stream((Object[]) o)
                    .map(Objects::toString)
                    .collect(Collectors.toSet())
                    : Collections.singleton(Objects.toString(o));
            return Pair.of(name, stringSet);
        } catch (IllegalAccessException | InvocationTargetException e) {
            return null;
        }
    }

    @Override
    public void close() {
        classAttributesCache.clear();
    }
}

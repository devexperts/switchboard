/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.devexperts.switchboard.integrations.javaparser;

import com.devexperts.switchboard.entities.Attributes;
import com.devexperts.switchboard.entities.Test;
import com.devexperts.switchboard.entities.attributes.AttributeIsPresent;
import com.devexperts.switchboard.entities.attributes.AttributePredicate;
import com.devexperts.switchboard.impl.extractors.FileTestExtractor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * {@link com.devexperts.switchboard.api.TestExtractor} implementation utilizing {@link com.github.javaparser} functionality to process Java code for extracting tests.
 */
public class JavaTestExtractor extends FileTestExtractor<JavaParserIntegrationFeatures> {
    private static final String DEFAULT_FILE_PATTERN = ".*.java";
    private static final String TEST_ANNOTATION_PATTERN = "(.*\\.)?Test";

    @JsonProperty(defaultValue = DEFAULT_FILE_PATTERN)
    private String filePattern = DEFAULT_FILE_PATTERN;
    @JsonProperty(defaultValue = TEST_ANNOTATION_PATTERN)
    private String testAnnotationPattern = TEST_ANNOTATION_PATTERN;
    @JsonProperty(defaultValue = "false")
    private boolean storeAnnotationsQualified = false;
    @JsonProperty(defaultValue = "false")
    private boolean collectComments = false;
    @JsonProperty()
    private List<String> preconditionsAnnotationPatterns = new ArrayList<>();
    @JsonProperty()
    private List<String> postconditionsAnnotationPatterns = new ArrayList<>();


    @JsonIgnore
    private AttributePredicate testAttributeCheck;
    @JsonIgnore
    private List<AttributePredicate> preconditionsAttributeCheck;
    @JsonIgnore
    private List<AttributePredicate> postconditionsAttributeCheck;
    @JsonIgnore
    private JavaParserIntegrationFeatures integrationFeatures;

    private JavaTestExtractor() {
        super();
    }

    public JavaTestExtractor(String identifier, List<String> testLocation, String filePattern, String testAnnotationPattern,
                             boolean storeAnnotationsQualified, boolean collectComments,
                             List<String> preconditionsAnnotationPatterns, List<String> postconditionsAnnotationPatterns)
    {
        super(identifier, testLocation);
        this.filePattern = filePattern;
        this.testAnnotationPattern = testAnnotationPattern;
        this.storeAnnotationsQualified = storeAnnotationsQualified;
        this.collectComments = collectComments;
        this.preconditionsAnnotationPatterns = preconditionsAnnotationPatterns;
        this.postconditionsAnnotationPatterns = postconditionsAnnotationPatterns;
    }

    @Override
    protected String getFilePattern() {
        return filePattern;
    }

    @Override
    public void init(JavaParserIntegrationFeatures integrationFeatures) {
        this.integrationFeatures = integrationFeatures;
        testAttributeCheck = new AttributeIsPresent(testAnnotationPattern);
        preconditionsAttributeCheck = preconditionsAnnotationPatterns.stream()
                .map(AttributeIsPresent::new)
                .collect(Collectors.toList());
        postconditionsAttributeCheck = postconditionsAnnotationPatterns.stream()
                .map(AttributeIsPresent::new)
                .collect(Collectors.toList());
    }

    @Override
    protected List<Test> extractTests(File file) {
        CompilationUnit parsedClass = integrationFeatures.parse(file);
        NodeList<ImportDeclaration> imports = parsedClass.getImports();
        List<MethodDeclaration> methods = new ArrayList<>();
        parsedClass.accept(new VoidVisitorAdapter<List<MethodDeclaration>>() {
            @Override
            public void visit(MethodDeclaration n, List<MethodDeclaration> arg) {
                arg.add(n);
            }
        }, methods);

        Map<Integer, Attributes> preconditionsAttributes = new HashMap<>();
        Map<Integer, Attributes> postconditionsAttributes = new HashMap<>();
        List<Attributes> testAttributes = new ArrayList<>();

        for (MethodDeclaration method : methods) {
            Attributes attributes = integrationFeatures.collectTestAnnotations(method, imports, storeAnnotationsQualified);

            if (testAttributeCheck.test(attributes)) {
                if (collectComments) {
                    attributes = attributes.toBuilder().mergeAttributes(collectComments(method, Attributes.COMMENTS_PROP).getAttributes()).build();
                }
                testAttributes.add(attributes);
            } else if (collectComments) {
                for (int i = 0; i < preconditionsAttributeCheck.size(); i++) {
                    if (preconditionsAttributeCheck.get(i).test(attributes)) {
                        preconditionsAttributes.put(i, collectComments(method, Attributes.PRECONDITIONS_COMMENTS_PROP));
                    }
                }
                for (int i = 0; i < postconditionsAttributeCheck.size(); i++) {
                    if (postconditionsAttributeCheck.get(i).test(attributes)) {
                        postconditionsAttributes.put(i, collectComments(method, Attributes.POSTCONDITIONS_COMMENTS_PROP));
                    }
                }
            }
        }
        Attributes mergedPreconditionsAttributes = mergeConditionsAttributes(preconditionsAttributes);
        Attributes mergedPostconditionsAttributes = mergeConditionsAttributes(postconditionsAttributes);

        return testAttributes.stream()
                .map(t -> Attributes.newBuilder().mergeAttributes(t.getAttributes()))
                .map(b -> b.mergeAttributes(mergedPreconditionsAttributes))
                .map(b -> b.mergeAttributes(mergedPostconditionsAttributes))
                .map(Attributes.Builder::build)
                .map(a -> new Test(JavaParserIntegrationFeatures.formatTestPath(a), a, JavaParserIntegrationFeatures.TEST_TO_RUNNABLE_STRING))
                .collect(Collectors.toList());
    }

    private Attributes collectComments(MethodDeclaration method, String attributeKey) {
        Attributes.Builder builder = Attributes.newBuilder();
        List<String> comments = integrationFeatures.collectTestComments(method);
        if (!comments.isEmpty()) {
            builder.mergeAttribute(attributeKey, Attributes.COMMENTS_PROP, String.join("\n", comments));
        }
        String javadoc = integrationFeatures.collectJavadoc(method);
        if (javadoc != null) {
            builder.mergeAttribute(attributeKey, Attributes.JAVADOC_PROP, javadoc);
        }
        return builder.build();
    }

    private static Attributes mergeConditionsAttributes(Map<Integer, Attributes> conditionsAttributes) {
        Attributes.Builder builder = Attributes.newBuilder();
        conditionsAttributes.keySet().stream().sorted()
                .map(conditionsAttributes::get)
                .forEach(a -> builder.mergeAttributes(a.getAttributes()));
        return builder.build();
    }

    @Override
    public void close() {/*do nothing*/}
}
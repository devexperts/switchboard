/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.devexperts.switchboard.integrations.javaparser;

import com.devexperts.switchboard.api.IntegrationFeatures;
import com.devexperts.switchboard.entities.Attributes;
import com.devexperts.switchboard.entities.Test;
import com.github.javaparser.JavaParser;
import com.github.javaparser.Range;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.description.JavadocDescription;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Implementation of {@link IntegrationFeatures} providing access to {@link JavaParser} functionality
 */
public class JavaParserIntegrationFeatures implements IntegrationFeatures {
    public static final String LOCATION_PACKAGE_KEY = "package";
    public static final String LOCATION_CLASS_KEY = "class";
    public static final String LOCATION_METHOD_KEY = "method";
    public static final Function<Test, String> TEST_TO_RUNNABLE_STRING = test -> formatTestPath(test.getAttributes());


    private final JavaParser javaParser = new JavaParser();
    private final AnnotationParser annotationParser = new AnnotationParser();

    JavaParserIntegrationFeatures() {}

    public CompilationUnit parse(File file) {
        try {
            return javaParser.parse(file)
                    .getResult()
                    .orElseThrow(() -> new IllegalStateException("Parse result for '" + file.toString() + " is empty"));
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Failed to parse file " + file, e);
        }
    }

    public Attributes collectTestAnnotations(MethodDeclaration method, NodeList<ImportDeclaration> imports, boolean storeAnnotationsQualified) {
        return annotationParser.collectTestAnnotations(method, imports, storeAnnotationsQualified);
    }

    public List<String> collectTestComments(MethodDeclaration method) {
        return method.getAllContainedComments().stream()
                .sorted(JavaParserIntegrationFeatures::compareCommentLines)
                .map(Comment::getContent)
                .collect(Collectors.toList());
    }

    private static int compareCommentLines(Comment comment1, Comment comment2) {
        return comment1.getRange().orElse(Range.range(0, 0, 0, 0)).begin
                .compareTo(comment2.getRange().orElse(Range.range(0, 0, 0, 0)).begin);
    }

    public String collectJavadoc(MethodDeclaration method) {
        return method.getComment()
                .filter(Comment::isJavadocComment)
                .map(Comment::asJavadocComment)
                .map(JavadocComment::parse)
                .map(Javadoc::getDescription)
                .map(JavadocDescription::toText)
                .orElse(null);
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

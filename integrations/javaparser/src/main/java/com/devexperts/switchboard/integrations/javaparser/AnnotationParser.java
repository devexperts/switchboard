/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.devexperts.switchboard.integrations.javaparser;

import com.devexperts.switchboard.entities.Attributes;
import com.devexperts.switchboard.entities.Pair;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.ArrayInitializerExpr;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.ClassExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.LiteralStringValueExpr;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class AnnotationParser {

    private static final List<ExpressionParser<? extends AnnotationExpr, Map<String, Set<String>>>> ANNOTATION_PARSERS = Arrays.asList(
            ExpressionParser.create(MarkerAnnotationExpr.class, a -> Collections.emptyMap()),
            ExpressionParser.create(SingleMemberAnnotationExpr.class, a -> {
                Map<String, Set<String>> result = new HashMap<>();
                result.put("", new HashSet<>(parseExpression(a.getMemberValue())));
                return result;
            }),
            ExpressionParser.create(NormalAnnotationExpr.class, a -> a.getPairs().stream()
                    .collect(Collectors.toMap(MemberValuePair::getNameAsString, p -> new HashSet<>(parseExpression(p.getValue()))))));

    private static final List<ExpressionParser<? extends Expression, List<String>>> VALUE_PARSERS = Arrays.asList(
            ExpressionParser.create(StringLiteralExpr.class, e -> Collections.singletonList(e.getValue())),
            ExpressionParser.create(LiteralStringValueExpr.class, e -> Collections.singletonList(e.getValue())),
            ExpressionParser.create(BooleanLiteralExpr.class, e -> Collections.singletonList(String.valueOf(e.getValue()))),
            ExpressionParser.create(NameExpr.class, e -> Collections.singletonList(e.getNameAsString())),
            ExpressionParser.create(ClassExpr.class, e -> Collections.singletonList(e.getType().asString())),
            ExpressionParser.create(BinaryExpr.class, e -> Collections.singletonList(e.toString())),
            ExpressionParser.create(FieldAccessExpr.class, e ->
                    parseExpression(e.getScope()).stream()
                            .map(s -> Stream.of(s, e.getNameAsString())
                                    .filter(Objects::nonNull)
                                    .map(String::trim)
                                    .filter(p -> !p.isEmpty())
                                    .collect(Collectors.joining(".")))
                            .collect(Collectors.toList())),
            ExpressionParser.create(ArrayInitializerExpr.class, e -> e.getValues().stream()
                    .map(AnnotationParser::parseExpression)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList())));

    private final Map<TypeDeclaration<?>, Attributes> classAttributesCache = new HashMap<>();

    AnnotationParser() {}

    Attributes collectTestAnnotations(MethodDeclaration method, NodeList<ImportDeclaration> imports, boolean storeAnnotationsQualified) {
        Attributes methodAttributes = AnnotationParser.parseAnnotations(method.getAnnotations(), imports, storeAnnotationsQualified);
        TypeDeclaration<?> clazz = method.getParentNode()
                .map(n -> {
                    if (!(n instanceof TypeDeclaration)) {
                        throw new IllegalStateException("Parent node for method is " + n.getClass() +
                                ", expected: " + TypeDeclaration.class);
                    }
                    return (TypeDeclaration) n;
                })
                .orElseThrow(() -> new IllegalStateException(String.format("Parent class for method %s not found", method.getNameAsString())));
        String pckg = clazz.getFullyQualifiedName().map(n -> n.replace("." + clazz.getNameAsString(), "")).orElse("");

        return classAttributesCache.computeIfAbsent(clazz,
                c -> AnnotationParser.parseAnnotations(c.getAnnotations(), imports, storeAnnotationsQualified))
                .toBuilder()
                .putAttributes(methodAttributes)
                .putAttribute(Attributes.LOCATION_PROP, JavaParserIntegrationFeatures.LOCATION_PACKAGE_KEY, pckg)
                .mergeAttribute(Attributes.LOCATION_PROP, JavaParserIntegrationFeatures.LOCATION_CLASS_KEY, clazz.getNameAsString())
                .mergeAttribute(Attributes.LOCATION_PROP, JavaParserIntegrationFeatures.LOCATION_METHOD_KEY, method.getNameAsString())
                .build();
    }

    private static Attributes parseAnnotations(NodeList<AnnotationExpr> annotations, NodeList<ImportDeclaration> imports,
                                               boolean storeAnnotationsQualified)
    {
        Attributes.Builder builder = Attributes.newBuilder();
        annotations.stream()
                .map(a -> parseAnnotation(a, imports, storeAnnotationsQualified))
                .forEach(p -> builder.mergeAttribute(p.getKey(), p.getValue()));
        return builder.build();
    }

    private static Pair<String, Map<String, Set<String>>> parseAnnotation(AnnotationExpr annotation, NodeList<ImportDeclaration> imports,
                                                                          boolean storeAnnotationsQualified)
    {
        ExpressionParser<? extends AnnotationExpr, Map<String, Set<String>>> parser = ANNOTATION_PARSERS.stream()
                .filter(e -> e.isParsable(annotation))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        String.format("No parsers available for annotation of type %s: %s", annotation.getClass(), annotation)));
        return Pair.of(storeAnnotationsQualified ? checkQualified(annotation.getNameAsString(), imports) : annotation.getNameAsString(),
                parser.parse(annotation));
    }

    private static String checkQualified(String annotation, NodeList<ImportDeclaration> imports) {
        final Optional<ImportDeclaration> importDeclaration = imports.stream()
                .filter(i -> i.getName().getIdentifier().equals(annotation.split("\\.")[0]))
                .findFirst();
        return importDeclaration.map(i -> i.getName()
                        .getQualifier()
                        .orElseThrow(() -> new IllegalStateException(String.format("No Qualifier for import %s", i)))
                        .asString()
                        .concat(".")
                        .concat(annotation))
                .orElse(annotation);
    }

    private static List<String> parseExpression(Expression expression) {
        try {
            ExpressionParser<? extends Expression, List<String>> parser = VALUE_PARSERS.stream()
                    .filter(e -> e.isParsable(expression))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            String.format("No parsers available for expression of type %s: %s", expression.getClass(), expression)));
            return parser.parse(expression);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse expression " + expression, e);
        }
    }

    private static final class ExpressionParser<T extends Expression, U> {
        private final Class<T> expressionClass;
        private final Function<T, U> parser;

        private ExpressionParser(Class<T> expressionClass, Function<T, U> parser) {
            this.expressionClass = expressionClass;
            this.parser = parser;
        }

        public static <T extends Expression, U> ExpressionParser<T, U> create(Class<T> expressionClass, Function<T, U> extractor) {
            return new ExpressionParser<>(expressionClass, extractor);
        }

        public boolean isParsable(Expression expression) {
            return expressionClass.isAssignableFrom(expression.getClass());
        }

        @SuppressWarnings("unchecked")
        public U parse(Expression expression) {
            return parser.apply((T) expression);
        }
    }
}

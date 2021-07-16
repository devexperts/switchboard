package com.devexperts.switchboard.integrations.bytecodeparser

import com.devexperts.switchboard.entities.Attributes
import org.junit.Test

class BytecodeTestExtractorTest {

    private static final List<String> PATH = ["src/test/resources/test_classes"] as List<String>

    private static final String JUNIT_PATTERN = "org.junit.Test"

    private static final String JUPITER_PATTERN = "org.junit.jupiter.api.Test"

    private static final String TESTNG_PATTERN = "org.testng.annotations.Test"

    private static final BytecodeParserIntegrationFeatures features = new BytecodeParserIntegrationFeatures()


    private static final Map<String, Map<String, Map<String, Set<String>>>> TESTS = [
            '%s.%s.%sTests#test1'      : [
                    "%s.Annotations.MarkerAnnotation1"   : [:],
                    "%s.Annotations.ValueAnnotation1"    : ["": ["qwerty"] as Set],
                    "%s.Annotations.ValueAnnotation2"    : ["": ["0"] as Set],
                    "%s.Annotations.MultiValueAnnotation": [
                            "arrVal"   : ["a1", "a2", "a3"] as Set,
                            "intVal"   : ["3"] as Set,
                            "stringVal": ["multival"] as Set]
            ],
            '%s.%s.%sTests#test2'      : [
                    "%s.Annotations.ValueAnnotation1" : ["": ["common"] as Set],
                    "%s.Annotations.MarkerAnnotation2": [:],
                    "%s.Annotations.ValueAnnotation2" : ["": ["42"] as Set]
            ],
            '%s.%s.%sTests#test3'      : [
                    "%s.Annotations.MarkerAnnotation1"   : [:],
                    "%s.Annotations.MarkerAnnotation2"   : [:],
                    "%s.Annotations.ValueAnnotation1"    : ["": ["abc"] as Set],
                    "%s.Annotations.ValueAnnotation2"    : ["": ["7"] as Set],
                    "%s.Annotations.MultiValueAnnotation": [
                            "arrVal"   : ["a1", "a2"] as Set,
                            "intVal"   : ["2"] as Set,
                            "stringVal": ["multivalX"] as Set]
            ],
            '%s.%s.pckg.%sTests1#test1': [
                    "%s.Annotations.MarkerAnnotation1"   : [:],
                    "%s.Annotations.ValueAnnotation1"    : ["": ["qwerty"] as Set],
                    "%s.Annotations.MultiValueAnnotation": [
                            "arrVal"   : ["a1", "a2", "a3"] as Set,
                            "intVal"   : ["3"] as Set,
                            "stringVal": ["multival"] as Set]
            ],
            '%s.%s.pckg.%sTests1#test2': [
                    "%s.Annotations.MarkerAnnotation2": [:],
                    "%s.Annotations.ValueAnnotation2": ["": ["42"] as Set]
            ],
            '%s.%s.pckg.%sTests1#test3': [
                    "%s.Annotations.MarkerAnnotation1"   : [:],
                    "%s.Annotations.MarkerAnnotation2"   : [:],
                    "%s.Annotations.ValueAnnotation1"    : ["": ["abc"] as Set],
                    "%s.Annotations.ValueAnnotation2"    : ["": ["7"] as Set],
                    "%s.Annotations.MultiValueAnnotation": [
                            "arrVal"   : ["a1", "a2"] as Set,
                            "intVal"   : ["2"] as Set,
                            "stringVal": ["multivalX"] as Set]
            ],
            '%s.%s.pckg.%sTests2#test1': [
                    "%s.Annotations.MarkerAnnotation1"   : [:],
                    "%s.Annotations.ValueAnnotation1"    : ["": ["qwerty"] as Set],
                    "%s.Annotations.MultiValueAnnotation": [
                            "arrVal"   : ["a1", "a2", "a3"] as Set,
                            "intVal"   : ["3"] as Set,
                            "stringVal": ["multival"] as Set]
            ],
            '%s.%s.pckg.%sTests2#test2': [
                    "%s.Annotations.MarkerAnnotation2": [:],
                    "%s.Annotations.ValueAnnotation2": ["": ["42"] as Set]
            ],
            '%s.%s.pckg.%sTests2#test3': [
                    "%s.Annotations.MarkerAnnotation1"   : [:],
                    "%s.Annotations.MarkerAnnotation2"   : [:],
                    "%s.Annotations.ValueAnnotation1"    : ["": ["abc"] as Set],
                    "%s.Annotations.ValueAnnotation2"    : ["": ["7"] as Set],
                    "%s.Annotations.MultiValueAnnotation": [
                            "arrVal"   : ["a1", "a2"] as Set,
                            "intVal"   : ["2"] as Set,
                            "stringVal": ["multivalX"] as Set]
            ]
    ]

    @Test
    void extractJUnitTests() {
        def extractor = new BytecodeTestExtractor("junitExtractor", PATH, "**/junit.jar", JUNIT_PATTERN, true)
        extractor.init(features)
        doTestExtract(extractor, getExpectedMap("junit", "JUnit", JUNIT_PATTERN))
    }

    @Test
    void extractJupiterTests() {
        def extractor = new BytecodeTestExtractor("jupiterExtractor", PATH, "**/jupiter.jar", JUPITER_PATTERN, true)
        extractor.init(features)
        doTestExtract(extractor, getExpectedMap("jupiter", "Jupiter", JUPITER_PATTERN))
    }

    @Test
    void extractTestNgTests() {
        def extractor = new BytecodeTestExtractor("testNgExtractor", PATH, "**/testng.jar", TESTNG_PATTERN, true)
        extractor.init(features)
        doTestExtract(extractor, getExpectedMap("testng", "TestNg", TESTNG_PATTERN))
    }

    @Test
    void extractAllTests() {
        def extractor = new BytecodeTestExtractor("allExtractor", PATH, "**/*.jar", "(.*\\.)?Test", true)
        extractor.init(features)
        Map<String, Map<String, Map<String, Set<String>>>> map = new HashMap<>()
        map.putAll(getExpectedMap("junit", "JUnit", JUNIT_PATTERN))
        map.putAll(getExpectedMap("jupiter", "Jupiter", JUPITER_PATTERN))
        map.putAll(getExpectedMap("testng", "TestNg", TESTNG_PATTERN))
        doTestExtract(extractor, map)
    }

    private static List<com.devexperts.switchboard.entities.Test> doTestExtract(BytecodeTestExtractor extractor, Map<String, Map<String, Map<String, Set<String>>>> expected) {
        List<com.devexperts.switchboard.entities.Test> tests = extractor.get()
        assert tests.size() == expected.size()
        expected.entrySet().each { entry ->
            com.devexperts.switchboard.entities.Test test = tests.find { it.identifier == entry.key }
            assert test != null
            assert test.getAttributes().attributes.size() == entry.value.size()

            for (def expAttribute : entry.value.entrySet()) {
                def attributeO = test.attributes.getAttribute(expAttribute.key)
                assert attributeO.isPresent()
                assert attributeO.get() == expAttribute.value
            }
            assert test.getAttributes().attributes == entry.value
        }
        return tests
    }



    private static Map<String, Map<String, Map<String, Set<String>>>> getExpectedMap(String testPckgPart, String classPart, String annotation) {
        String path = "com.devexperts.dxci.integrations.javaparser"
        Map<String, Map<String, Map<String, Set<String>>>> expected = new HashMap<>()
        for (Map.Entry<String, Map<String, Map<String, Set<String>>>> entry1 : TESTS.entrySet()) {
            String key1 = String.format(entry1.key, path, testPckgPart, classPart)
            Map<String, Map<String, Set<String>>> value1 = new HashMap<>()
            for (Map.Entry<String, Map<String, Set<String>>> entry2 : entry1.value.entrySet()) {
                String key2 = String.format(entry2.key, path)
                Map<String, Set<String>> value2 = new HashMap<>()
                for (Map.Entry<String, Set<String>> entry3 : entry2.value.entrySet()) {
                    String key3 = String.format(entry3.key, path)
                    value2.put(key3, entry3.value)
                }
                value1.put(key2, value2)
            }

            value1.put(annotation, [:])

            Map<String, Set<String>> location = new HashMap<>()
            String[] split = key1.split("[.#]")
            location.put("method", [split[split.length - 1]].toSet())
            location.put("class", [split[split.length - 2]].toSet())
            location.put("package", [String.join(".", Arrays.asList(split).subList(0, split.length - 2))].toSet())
            value1.put(Attributes.LOCATION_PROP, location)
            expected.put(key1, value1)
        }
        return expected
    }
}

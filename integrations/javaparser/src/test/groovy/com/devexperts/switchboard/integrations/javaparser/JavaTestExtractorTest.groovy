package com.devexperts.switchboard.integrations.javaparser

import com.devexperts.switchboard.entities.Attributes
import org.junit.Test

class JavaTestExtractorTest {

    private static final String PATH = "src${File.separator}test${File.separator}resources${File.separator}test_classes${File.separator}typed"

    private static final List<String> JUNIT_PATHS = [PATH + "${File.separator}junit${File.separator}JUnitTests.java", PATH + "${File.separator}junit${File.separator}pckg"]
    private static final String JUNIT_PATTERN = "org.junit.Test"

    private static final List<String> JUPITER_PATHS = [PATH + "${File.separator}jupiter${File.separator}JupiterTests.java", PATH + "${File.separator}jupiter${File.separator}pckg"]
    private static final String JUPITER_PATTERN = "org.junit.jupiter.api.Test"

    private static final List<String> TESTNG_PATHS = [PATH + "${File.separator}testng${File.separator}TestNgTests.java", PATH + "${File.separator}testng${File.separator}pckg"]
    private static final String TESTNG_PATTERN = "org.testng.annotations.Test"

    private static final JavaParserIntegrationFeatures features = new JavaParserIntegrationFeatures()


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
        def extractor = new JavaTestExtractor("junitExtractor", JUNIT_PATHS, "regex:.*.java", JUNIT_PATTERN, true, false, [], [])
        extractor.init(features)
        doTestExtract(extractor, getExpectedMap("junit", "JUnit", JUNIT_PATTERN))
    }

    @Test
    void extractJupiterTests() {
        def extractor = new JavaTestExtractor("jupiterExtractor", JUPITER_PATHS, "regex:.*.java", JUPITER_PATTERN, true, false, [], [])
        extractor.init(features)
        doTestExtract(extractor, getExpectedMap("jupiter", "Jupiter", JUPITER_PATTERN))
    }

    @Test
    void extractTestNgTests() {
        def extractor = new JavaTestExtractor("testNgExtractor", TESTNG_PATHS, "regex:.*.java", TESTNG_PATTERN, true, false, [], [])
        extractor.init(features)
        doTestExtract(extractor, getExpectedMap("testng", "TestNg", TESTNG_PATTERN))
    }

    @Test
    void extractAllTests() {
        def extractor = new JavaTestExtractor("allExtractor", [PATH], "regex:.*.java", "(.*\\.)?Test", true, false, [], [])
        extractor.init(features)
        Map<String, Map<String, Map<String, Set<String>>>> map = new HashMap<>()
        map.putAll(getExpectedMap("junit", "JUnit", JUNIT_PATTERN))
        map.putAll(getExpectedMap("jupiter", "Jupiter", JUPITER_PATTERN))
        map.putAll(getExpectedMap("testng", "TestNg", TESTNG_PATTERN))
        doTestExtract(extractor, map)
    }

    private static List<com.devexperts.switchboard.entities.Test> doTestExtract(JavaTestExtractor extractor, Map<String, Map<String, Map<String, Set<String>>>> expected) {
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

    @Test
    void extractJUnitTestsWithComments() {
        def extractor = new JavaTestExtractor("junitExtractor", [PATH + "${File.separator}junit${File.separator}JUnitTests.java"], "regex:.*.java", JUNIT_PATTERN, true, true, [], [])
        extractor.init(features)
        List<com.devexperts.switchboard.entities.Test> tests = extractor.get()
        assert tests.size() == 3
        for (int i = 0; i < 3; i++) {
            com.devexperts.switchboard.entities.Test test = tests.get(i)
            assert test.attributes.attributes.containsKey(Attributes.COMMENTS_PROP)
            assert test.attributes.getAttributeValue(Attributes.COMMENTS_PROP, Attributes.COMMENTS_PROP).get() ==
                    [" STEP: do something ${i + 1}\n STEP: do something else ${i + 1}\n STEP: and another thing ${i + 1}".toString()] as Set
        }
    }

    @Test
    void extractJUnitJiraTestsWithComments() {
        def extractor = new JavaTestExtractor("junitExtractor", [PATH + "${File.separator}..${File.separator}JUnitJiraTests.java"],
                "regex:.*.java", "Test", false, true, [], [])
        extractor.init(features)
        List<com.devexperts.switchboard.entities.Test> tests = extractor.get()
        assert tests.size() == 3
        for (int i = 0; i < 2; i++) {
            com.devexperts.switchboard.entities.Test test = tests.get(i)
            assert test.attributes.attributes.containsKey(Attributes.COMMENTS_PROP)
            assert test.attributes.getAttributeValue(Attributes.COMMENTS_PROP, Attributes.COMMENTS_PROP).get() ==
                    [" STEP: do something ${i + 1}\n STEP: do something else ${i + 1}\n STEP: and another thing ${i + 1}".toString()] as Set
        }
        assert !tests.get(2).attributes.attributes.containsKey(Attributes.COMMENTS_PROP)
    }

    private static Map<String, Map<String, Map<String, Set<String>>>> getExpectedMap(String testPckgPart, String classPart, String annotation) {
        String path = "com.devexperts.switchboard.integrations.javaparser"
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
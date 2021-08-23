package com.devexperts.switchboard.entities.valuesupplier

import com.devexperts.switchboard.entities.Attributes
import com.devexperts.switchboard.entities.Pair
import com.devexperts.switchboard.utils.JacksonUtils
import org.junit.Test

import java.nio.file.Files
import java.nio.file.Paths

class TestDescriptionFormatterTests {

    private static com.devexperts.switchboard.entities.Test test = JacksonUtils.getMapper()
            .readValue(Thread.currentThread().getContextClassLoader().getResource("sample_test.json"), com.devexperts.switchboard.entities.Test.class)

    @Test
    void testTestDescriptionFormatter() throws IOException {
        EnumeratingTestDescriptionFormatter testDescriptionFormatter = new EnumeratingTestDescriptionFormatter(
                new AttributeValuesExtractor("comments", "comments", "\n"),
                "^\\s*Action:\\s*(.+)", "^\\s*Result:\\s*(.+)",
                "*Actions:*", "*Results:*",
                "# ", "Check result (%s)", "After step (%s) - ")


        String formatted = testDescriptionFormatter.getTestValue(test)
        String expected = getResText("sample_test_description.txt")
        assert formatted == expected
    }

    @Test
    void testFullFormatter() {
        EnumeratingTestDescriptionFormatter testDescriptionFormatter = new EnumeratingTestDescriptionFormatter(
                new AttributeValuesExtractor("comments", "comments", "\n"),
                "^\\s*Action:\\s*(.+)", "^\\s*Result:\\s*(.+)",
                "*Actions:*", "*Results:*",
                "# ", "Check result (%s)", "After step (%s) - ")

        BlockFormattingValuesExtractor blockFormatter = new BlockFormattingValuesExtractor([
                Pair.of("*Overview:*", new AttributeValuesExtractor(Attributes.COMMENTS_PROP, Attributes.JAVADOC_PROP, "\n")),
                Pair.of("*Preconditions:*", new AttributeValuesExtractor(Attributes.PRECONDITIONS_COMMENTS_PROP, Attributes.JAVADOC_PROP, "\n\n")),
                Pair.of("", testDescriptionFormatter),
                Pair.of("*Postconditions:*", new AttributeValuesExtractor(Attributes.POSTCONDITIONS_COMMENTS_PROP, Attributes.JAVADOC_PROP, "\n\n"))],
                "\n", "\n\n", false)
        String formatted = blockFormatter.getTestValue(test)
        String expected = getResText("sample_test_full.txt")
        assert formatted == expected
    }

    static String getResText(String res) {
        return new String(Files.readAllBytes(Paths.get(Thread.currentThread().getContextClassLoader().getResource(res).toURI())))
                .replaceAll("\r\n", "\n")
    }
}

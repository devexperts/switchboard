package com.devexperts.switchboard.example.pckg1;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import com.devexperts.switchboard.example.CustomAnnotations.Component;
import com.devexperts.switchboard.example.CustomAnnotations.Severity;

@BrowserTest
public class Junit3Test {

    /**
     * Quisque in nisi nec tellus ullamcorper laoreet eu non metus. Suspendisse quis mi lorem.
     * # Aliquam ullamcorper neque in purus porta, in volutpat leo porta.
     */
    @BeforeEach
    void initEach() {
        doPrepareForEachTest();
    }

    /**
     * Cras at felis eu felis aliquet volutpat a ac felis.
     * # Quisque vel metus sit amet ipsum fringilla elementum at sit amet metus.
     * # Aliquam erat volutpat.
     */
    @BeforeAll
    void initAll() {
        doPrepareForAllTests();
    }

    /**
     * Mauris non tortor aliquam, lobortis nisi sit amet, fringilla arcu. Mauris ut mattis urna, sed sodales ligula.
     * # Cras vitae gravida metus, eget feugiat purus.
     */
    @AfterEach
    void tearDownEach() {
        doClenupForEachTest();
    }

    /**
     * Aliquam euismod a nunc ut hendrerit.
     * # Pellentesque at magna rutrum velit pulvinar laoreet sed sed tortor.
     * # Aliquam a neque lorem.
     */
    @AfterAll
    void tearDownAll() {
        doClenupForAllTests();
    }


    /**
     * Vivamus sagittis lectus sed odio rhoncus, vitae rhoncus lorem aliquet.
     * In ipsum erat, tristique vel tortor sit amet, luctus rhoncus diam.
     */
    @Test
    @DisplayName("Donec tempus quis orci ut varius.")
    @TestDetails(
            labels = {"Label1", "Label2"},
            severity = Severity.MINOR_FUNCTIONAL,
            uniqueID = "QA-0000131"
    )
    public void test1() {
        //Action: Etiam at risus finibus, commodo orci quis, molestie erat.
        System.out.println("1");

        //Action: Nunc aliquet iaculis ex, eu pharetra lacus.
        // Maecenas nec sem in quam commodo venenatis eu eu velit.
        // In lorem ipsum, maximus sed enim id, euismod lobortis felis.
        System.out.println("2");

        //Action: Quisque quis finibus arcu, sed lacinia mauris. Vivamus laoreet nisi sit amet laoreet porta.
        // Morbi pulvinar tristique ipsum ut dictum.
        System.out.println("3");
        //Result: In tincidunt efficitur ligula eu accumsan. Mauris et ipsum maximus, aliquet tellus vel, accumsan arcu.
        // Aenean tincidunt accumsan posuere.
        // Aliquam scelerisque sollicitudin maximus.
        assert true;

        //Action: Duis ornare elit scelerisque urna sagittis, eget pellentesque risus sagittis.
        System.out.println("4");
        //Result: Sed lobortis enim vitae orci fermentum, fermentum molestie tellus condimentum.
        assert true;
    }

    /**
     * Donec sodales nibh at ultricies maximus.
     * Etiam nibh nisl, rutrum sed magna a, ullamcorper suscipit arcu.
     * Duis malesuada justo sit amet turpis fringilla, dignissim ultrices sem interdum. Nunc nec interdum enim.
     */
    @ParameterizedTest
    @DisplayName("Cras ac dapibus felis.")
    @EnumSource(names = {"VAL1", "VAL2", "VAL3"})
    @TestDetails(
            labels = {"Label1", "Label3"},
            severity = Severity.FUNCTIONAL,
            uniqueID = "QA-0000132"
    )
    public void parametrizedTest1(String val) {
        //Action: Nullam in efficitur nisl. In hac habitasse platea dictumst.
        System.out.println("1");

        //Action: Etiam convallis quis quam id ultrices.
        System.out.println("2");

        //Action: Praesent dapibus purus ante, et commodo nibh ullamcorper nec.
        System.out.println("3");
        //Result: Sed varius ullamcorper enim, a mattis mauris tristique eu.
        assert true;

        //Action: Aliquam tincidunt libero eget vestibulum dapibus.

        //Action: Aenean mi quam, consectetur placerat mi quis, sollicitudin sollicitudin tellus.
        System.out.println("5");
        //Result: Sed semper, lorem ut sodales blandit, lacus neque pellentesque urna, vitae luctus turpis mi et velit.
        assert true;
    }
}

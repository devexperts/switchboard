package com.devexperts.switchboard.example.pckg1;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import com.devexperts.switchboard.example.CustomAnnotations.Component;
import com.devexperts.switchboard.example.CustomAnnotations.Severity;

@BrowserTest
@Component(Components.BACKEND)
public class Junit1Test {

    /**
     * Lorem ipsum dolor sit amet, consectetur adipiscing elit.
     */
    @BeforeEach
    void initEach() {
        doPrepareForEachTest();
    }

    /**
     * Vestibulum tellus dolor, sagittis in risus in, venenatis vehicula lacus.
     */
    @BeforeAll
    void initAll() {
        doPrepareForAllTests();
    }

    /**
     * Quisque in nisi nec tellus ullamcorper laoreet eu non metus. Suspendisse quis mi lorem.
     */
    @AfterEach
    void tearDownEach() {
        doClenupForEachTest();
    }

    /**
     * Aliquam ullamcorper neque in purus porta, in volutpat leo porta.
     * Cras at felis eu felis aliquet volutpat a ac felis. Quisque vel metus sit amet ipsum fringilla elementum at sit amet metus.
     * Aliquam erat volutpat. Mauris non tortor aliquam, lobortis nisi sit amet, fringilla arcu. Mauris ut mattis urna, sed sodales ligula.
     * Cras vitae gravida metus, eget feugiat purus.
     */
    @AfterAll
    void tearDownAll() {
        doClenupForAllTests();
    }


    /**
     * Suspendisse ac ipsum et orci dictum faucibus et quis lacus. Curabitur eu ultricies lorem.
     */
    @Test
    @DisplayName("Etiam suscipit neque vitae accumsan commodo.")
    @TestDetails(
            labels = {"Label2", "Label3"},
            severity = "Severity.GLITCH",
            uniqueID = "QA-0000121"
    )
    public void test1() {
        //Action: Proin euismod velit tincidunt libero fermentum, ac suscipit ligula molestie.
        System.out.println("1");

        //Action: Curabitur dictum odio id erat blandit, a tempus magna feugiat.
        System.out.println("2");

        //Action: Aliquam pretium ex vel nunc vehicula condimentum. Maecenas eget mauris posuere, lacinia ante quis, ornare neque.
        System.out.println("3");
        //Result: Sed quam massa, tristique nec iaculis sed, suscipit sed neque. Proin ullamcorper, lacus vel suscipit interdum, sapien libero ullamcorper nibh, ut lobortis nibh velit ac nisi.
        assert true;

        //Action: Sed nulla est, rutrum vel dolor quis, lobortis condimentum sapien.
        System.out.println("4");
        //Result: Sed in accumsan nunc, nec vulputate sapien. Nunc laoreet sollicitudin maximus.
        assert true;
    }

    /**
     * Cras imperdiet pretium finibus. Pellentesque auctor, turpis et faucibus suscipit, orci turpis venenatis sem, at pulvinar dui diam vitae tortor. Donec scelerisque vitae justo a dignissim.
     * Curabitur ultricies sollicitudin finibus. Phasellus tristique magna et vehicula porta. In hac habitasse platea dictumst. Cras arcu arcu, rhoncus a placerat nec, dignissim vitae metus.
     */
    @Test
    @DisplayName("Check something using @ParameterizedTest")
    @TestDetails(
            labels = {"Label1", "Label3"},
            severity = "Severity.USABILITY",
            uniqueID = "QA-0000122"
    )
    public void test2() {
        //Action: Quisque sed libero dolor.
        System.out.println("1");

        //Result: Donec ipsum urna, suscipit dignissim elit at, sodales cursus libero. Suspendisse aliquam imperdiet massa sed finibus.
        assert true;

        //Action: Integer porta turpis sit amet placerat tristique.

        //Action: Mauris suscipit fringilla mattis.
        // Proin fringilla lacinia cursus. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
        System.out.println("3");
        //Result: Donec sodales et purus vitae dictum. Nunc aliquet varius sem, quis scelerisque tellus scelerisque porta.
        assert true;
    }
}

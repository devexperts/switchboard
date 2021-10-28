package com.devexperts.switchboard.example.pckg1;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import com.devexperts.switchboard.example.CustomAnnotations.Component;
import com.devexperts.switchboard.example.CustomAnnotations.Severity;

@Component(Components.FRONTEND)
public class Junit1Test {

    /**
     * This is @BeforeEach and it goes second
     * # Ensure that everything is configured.
     * # Data for tests is ready and required state is set.
     */
    @BeforeEach
    void initEach() {
        doPrepareForEachTest();
    }

    /**
     * This is @BeforeAll and it goes first
     * # Ensure that everything is prepared for all tests in this class.
     * # Do something
     * # And something else
     */
    @BeforeAll
    void initAll() {
        doPrepareForAllTests();
    }

    /**
     * This is @AfterEach and it goes first
     * # Ensure that everything is cleaned up.
     */
    @AfterEach
    void tearDownEach() {
        doClenupForEachTest();
    }

    /**
     * This is @AfterAll and it goes second
     * # Ensure that everything is cleaned up.
     */
    @AfterAll
    void tearDownAll() {
        doClenupForAllTests();
    }


    /**
     * This is overview which is shown in the beginning of the description.
     * You can describe shortly here what is the purpose of this test.
     */
    @DisplayName("Check something using common @Test")
    @TestDetails(
            labels = {"Label1", "Label2"},
            severity = Severity.MINOR_FUNCTIONAL,
            uniqueID = "QA-0000111"
    )
    @Test
    public void test1() {
        //Action: Perform some action here
        System.out.println("1");

        //Action: Perform another action
        System.out.println("2");
        //Result: Check that the result of second action is just as expected.
        assert true;

        //Action: Perform third action
        System.out.println("3");
        //Result: Check the result of third action too
        assert true;
    }

    /**
     * This is another overview.
     * Also good to describe shortly what this test is about
     */
    @ParameterizedTest
    @EnumSource(names = {"VAL1", "VAL2"})
    @DisplayName("Check something using @ParameterizedTest")
    @TestDetails(
            labels = {"Label1", "Label3"},
            severity = Severity.FUNCTIONAL,
            uniqueID = "QA-0000112"
    )
    public void parametrizedTest1(String val) {
        //Action: Perform some action for ParameterizedTest
        System.out.println("1");

        //Action: Perform other action for ParameterizedTest
        System.out.println("2");

        //Action: Perform third action for ParameterizedTest
        System.out.println("3");
        //Result: The result of third action goes here.
        assert true;

        //Action: Perform fourth action for ParameterizedTest

        //Action: Perform fifth action for ParameterizedTest
        System.out.println("5");
        //Result: The result of fifth action goes here.
        assert true;
    }

    /**
     * This is an overview for another parametrizedTest.
     * Nothing special, just another test here.
     */
    @ParameterizedTest
    @EnumSource(names = {"VAL10", "VAL20", "VAL30", "VAL40"})
    @DisplayName("Check something else using @ParameterizedTest")
    @TestDetails(
            labels = {"Label2"},
            severity = Severity.SHOWSTOPPER,
            uniqueID = "QA-0000113"
    )
    public void parametrizedTest2(String val) {
        //Action: Check if you can write some more actions
        System.out.println("1");

        //Action: And even more actions
        System.out.println("2");
        //Result: Yes, you can.
        assert true;

        //Action: And now for something completely different

        //Action: Check if the parrot is alive
        System.out.println("3");
        //Result: This parrot is no more
        assert true;
    }
}

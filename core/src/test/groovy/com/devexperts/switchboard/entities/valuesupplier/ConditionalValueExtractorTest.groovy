/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.devexperts.switchboard.entities.valuesupplier


import com.devexperts.switchboard.entities.Attributes
import com.devexperts.switchboard.entities.Pair
import com.devexperts.switchboard.entities.TestRun
import com.devexperts.switchboard.entities.attributes.AttributeIsPresent
import org.junit.Test

class ConditionalValueExtractorTest {
    private static final Attributes ATTRIBUTES1 = Attributes.newBuilder()
            .putAttribute("ak1", Collections.emptyMap())
            .build()
    private static final Attributes ATTRIBUTES2 = Attributes.newBuilder()
            .putAttribute("ak1", Collections.emptyMap())
            .putAttribute("ak2", "", "a-val2")
            .build()
    private static final Attributes ATTRIBUTES3 = Attributes.newBuilder()
            .putAttribute("ak1", Collections.emptyMap())
            .putAttribute("ak2", "", "a-val2")
            .putAttribute("ak3", "a-val-key3", ["a-val3-1", "a-val3-2", "a-val3-3"])
            .build()
    private static final Attributes ATTRIBUTES4 = Attributes.newBuilder()
            .putAttribute("ak1", Collections.emptyMap())
            .putAttribute("ak2", "", "a-val2")
            .putAttribute("ak3", "a-val-key3", ["a-val3-1", "a-val3-2", "a-val3-3"])
            .putAttribute("ak4", [
                    "a-val-key4-1": ["a-val4-1-1", "a-val4-1-2", "a-val4-1-3"].toSet(),
                    "a-val-key4-2": ["a-val4-2-1", "a-val4-2-2", "a-val4-2-3"].toSet()])
            .build()

    private static final com.devexperts.switchboard.entities.Test TEST1 = new com.devexperts.switchboard.entities.Test("Test1", ATTRIBUTES1, null)
    private static final com.devexperts.switchboard.entities.Test TEST2 = new com.devexperts.switchboard.entities.Test("Test2", ATTRIBUTES2, null)
    private static final com.devexperts.switchboard.entities.Test TEST3 = new com.devexperts.switchboard.entities.Test("Test3", ATTRIBUTES3, null)
    private static final TestRun TEST_RUN = TestRun.newBuilder()
            .identifier("TestRun1")
            .addTests([TEST1, TEST2, TEST3]).putAttributes(ATTRIBUTES4)
            .build()

    @Test
    void testAttributeIsPresent() {
        ConditionalValueExtractor cve = new ConditionalValueExtractor([
                Pair.of(new AttributeIsPresent("ak4"), new ConstantValuesExtractor("x-ak4")),
                Pair.of(new AttributeIsPresent("ak2"), new ConstantValuesExtractor("x-ak2")),
                Pair.of(new AttributeIsPresent("ak3"), new ConstantValuesExtractor("x-ak3"))
        ], new ConstantValuesExtractor("x-ak0"))

        assert cve.getTestValue(TEST1) == "x-ak0"
        assert cve.getTestValue(TEST2) == "x-ak2"
        assert cve.getTestValue(TEST3) == "x-ak2"
        assert cve.getRunValue(TEST_RUN) == "x-ak4"
    }
}

/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.devexperts.switchboard.impl

import com.devexperts.switchboard.entities.Attributes
import com.devexperts.switchboard.entities.TestRun
import com.devexperts.switchboard.impl.splitters.TestCountSplitter
import org.junit.Test

@SuppressWarnings("GroovyAccessibility")
class TestCountSplitterTest {
    static final String ID = "counter"
    static final Attributes ATTR_SUCCESS = Attributes.newBuilder().putAttribute("splitters", ID, "true").build()
    static final Attributes ATTR_FAIL = Attributes.newBuilder().putAttribute("splitters", ID, "false").build()

    @Test
    void split_0_1() {
        TestCountSplitter cs = new TestCountSplitter(ID, 1)
        List<com.devexperts.switchboard.entities.Test> input = createTests(0)
        assert cs.split(input) == []
    }

    @Test
    void split_1_2() {
        TestCountSplitter cs = new TestCountSplitter(ID, 2)
        List<com.devexperts.switchboard.entities.Test> input = createTests(1)
        assert cs.split(input) == [TestRun.newBuilder().identifier("#0").addTests([input.get(0)]).putAttributes(ATTR_SUCCESS).build()]
    }

    @Test
    void split_2_1() {
        TestCountSplitter cs = new TestCountSplitter(ID, 1)
        List<com.devexperts.switchboard.entities.Test> input = createTests(2)
        assert cs.split(input) == [
                TestRun.newBuilder().identifier("#0").addTests([input.get(0)]).putAttributes(ATTR_SUCCESS).build(),
                TestRun.newBuilder().identifier("#1").addTests([input.get(1)]).putAttributes(ATTR_SUCCESS).build()
        ]
    }

    @Test
    void split_10_3() {
        TestCountSplitter cs = new TestCountSplitter(ID, 3)
        List<com.devexperts.switchboard.entities.Test> input = createTests(10)
        assert cs.split(input) == [
                TestRun.newBuilder().identifier("#0").addTests(input.subList(0, 3)).putAttributes(ATTR_SUCCESS).build(),
                TestRun.newBuilder().identifier("#1").addTests(input.subList(3, 6)).putAttributes(ATTR_SUCCESS).build(),
                TestRun.newBuilder().identifier("#2").addTests(input.subList(6, 9)).putAttributes(ATTR_SUCCESS).build(),
                TestRun.newBuilder().identifier("#3").addTests(input.subList(9, 10)).putAttributes(ATTR_SUCCESS).build(),
        ]
    }

    @Test
    void no_split() {
        TestCountSplitter cs = new TestCountSplitter(ID, 0)
        List<com.devexperts.switchboard.entities.Test> input = createTests(1)
        assert cs.split(input) == [
                TestRun.newBuilder().identifier("").addTests(input).putAttributes(ATTR_FAIL).build()
        ]
    }

    private static List<com.devexperts.switchboard.entities.Test> createTests(int count) {
        List<com.devexperts.switchboard.entities.Test> result = new ArrayList<>()
        for (int i = 0; i < count; i++) {
            result.add(new com.devexperts.switchboard.entities.Test("Test #$i",
                    Attributes.newBuilder().putAttribute("id", "", "TESTID-$i").build(),
                    com.devexperts.switchboard.entities.Test::getIdentifier))
        }
        return result
    }
}

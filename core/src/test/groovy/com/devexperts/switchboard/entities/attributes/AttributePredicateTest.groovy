/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.devexperts.switchboard.entities.attributes

import com.devexperts.switchboard.entities.Attributes
import com.devexperts.switchboard.entities.attributes.AttributeValueSetContains.Modifier
import org.junit.Test

class AttributePredicateTest {
    private static final Attributes ATTRIBUTES = Attributes.newBuilder()
            .putAttribute("ak1", Collections.emptyMap())
            .putAttribute("ak2", "", "a-val2")
            .putAttribute("ak3", "a-val-key3", ["a-val3-1", "a-val3-2", "a-val3-3"])
            .putAttribute("ak4", [
                    "a-val-key4-1": ["a-val4-1-1", "a-val4-1-2", "a-val4-1-3"].toSet(),
                    "a-val-key4-2": ["a-val4-2-1", "a-val4-2-2", "a-val4-2-3"].toSet()])
            .build()


    @Test
    void testAttributeIsPresent() {
        assert new AttributeIsPresent("ak1").test(ATTRIBUTES)
        assert new AttributeIsPresent("ak2").test(ATTRIBUTES)
        assert new AttributeIsPresent("ak3").test(ATTRIBUTES)
        assert new AttributeIsPresent("ak4").test(ATTRIBUTES)

        assert new AttributeIsPresent(".k1").test(ATTRIBUTES)
        assert new AttributeIsPresent("\\w*2").test(ATTRIBUTES)
        assert new AttributeIsPresent("ak\\d").test(ATTRIBUTES)

        assert !new AttributeIsPresent("abd").test(ATTRIBUTES)
        assert !new AttributeIsPresent("\\d+").test(ATTRIBUTES)
        try {
            new AttributeIsPresent("").test(ATTRIBUTES)
            assert false: "Created attributePredicate with empty key"
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    void testAttributeValueIsPresent() {
        assert new AttributeValueKeyIsPresent("ak2", "").test(ATTRIBUTES)
        assert new AttributeValueKeyIsPresent("ak3", "a-val-key3").test(ATTRIBUTES)
        assert new AttributeValueKeyIsPresent("ak4", "a-val-key4-1").test(ATTRIBUTES)
        assert new AttributeValueKeyIsPresent("ak4", "a-val-key4-2").test(ATTRIBUTES)

        assert new AttributeValueKeyIsPresent("\\w*2", ".*").test(ATTRIBUTES)
        assert new AttributeValueKeyIsPresent("ak\\d", "a-val-key3").test(ATTRIBUTES)
        assert new AttributeValueKeyIsPresent("ak[3,4]", "a-val-key\\d-1").test(ATTRIBUTES)
        assert new AttributeValueKeyIsPresent("ak4", "a-val-key4-[2,3,4,5]").test(ATTRIBUTES)

        assert !new AttributeValueKeyIsPresent("ak1", "").test(ATTRIBUTES)
        assert !new AttributeValueKeyIsPresent("ak2", "qwe").test(ATTRIBUTES)
        assert !new AttributeValueKeyIsPresent("qwe", "a-val2").test(ATTRIBUTES)
    }

    @Test
    void testAttributeValueMatches() {
        assert new AttributeValueMatches("ak2", "", "a-val2").test(ATTRIBUTES)
        assert new AttributeValueMatches("ak3", "a-val-key3", "a-val3-2").test(ATTRIBUTES)
        assert new AttributeValueMatches("ak4", "a-val-key4-1", "a-val4-1-1").test(ATTRIBUTES)
        assert new AttributeValueMatches("ak4", "a-val-key4-2", "a-val4-2-3").test(ATTRIBUTES)

        assert new AttributeValueMatches("a.*2", ".*", "a-[val]{3}2").test(ATTRIBUTES)
        assert new AttributeValueMatches("ak\\d", "a-val-key3", "a-val\\d-2").test(ATTRIBUTES)
        assert new AttributeValueMatches("ak[3,4]", "a-val-key\\d-1", "a-val4-1-2").test(ATTRIBUTES)
        assert new AttributeValueMatches("ak4", "a-val-key4-[1,2,3]", "a-val\\d-\\d-\\d").test(ATTRIBUTES)

        assert !new AttributeValueMatches("ak1", ".*", ".*").test(ATTRIBUTES)
        assert !new AttributeValueMatches("ak2", "qwe", "a-val2").test(ATTRIBUTES)
        assert !new AttributeValueMatches("ak2", "a-val2", "asd").test(ATTRIBUTES)
    }

    @Test
    void testAttributeValueSetContains() {
        assert new AttributeValueSetContains("ak2", "", ["a-val2"].toSet(), Modifier.ANY).test(ATTRIBUTES)
        assert new AttributeValueSetContains("ak2", "", ["a-val2"].toSet(), Modifier.ALL).test(ATTRIBUTES)
        assert new AttributeValueSetContains("ak2", "", ["a-val2"].toSet(), Modifier.EXACTLY).test(ATTRIBUTES)

        assert new AttributeValueSetContains("ak3", "a-val-key3", ["a-val3-1"].toSet(), Modifier.ANY).test(ATTRIBUTES)
        assert new AttributeValueSetContains("ak3", "a-val-key3", ["a-val3-2", "a-val3-3"].toSet(), Modifier.ALL).test(ATTRIBUTES)
        assert new AttributeValueSetContains("ak3", "a-val-key3", ["a-val3-1", "a-val3-2", "a-val3-3"].toSet(), Modifier.EXACTLY).test(ATTRIBUTES)

        assert new AttributeValueSetContains("ak4", "a-val-key4-1", ["a-val4-1-1"].toSet(), Modifier.ANY).test(ATTRIBUTES)
        assert new AttributeValueSetContains("ak4", "a-val-key4-1", ["a-val4-1-2", "a-val4-1-3"].toSet(), Modifier.ALL).test(ATTRIBUTES)
        assert new AttributeValueSetContains("ak4", "a-val-key4-1", ["a-val4-1-1", "a-val4-1-2", "a-val4-1-3"].toSet(), Modifier.EXACTLY).test(ATTRIBUTES)
        assert new AttributeValueSetContains("ak4", "a-val-key4-2", ["a-val4-2-1"].toSet(), Modifier.ANY).test(ATTRIBUTES)
        assert new AttributeValueSetContains("ak4", "a-val-key4-2", ["a-val4-2-2", "a-val4-2-3"].toSet(), Modifier.ALL).test(ATTRIBUTES)
        assert new AttributeValueSetContains("ak4", "a-val-key4-2", ["a-val4-2-1", "a-val4-2-2", "a-val4-2-3"].toSet(), Modifier.EXACTLY).test(ATTRIBUTES)

        assert !new AttributeValueSetContains("ak1", ".*", [] as Collection<String>, Modifier.ANY).test(ATTRIBUTES)
        assert !new AttributeValueSetContains("ak2", "", ["a-val2", "a-val3"].toSet(), Modifier.ALL).test(ATTRIBUTES)
        assert !new AttributeValueSetContains("ak2", "", ["a-val3"].toSet(), Modifier.EXACTLY).test(ATTRIBUTES)

        assert !new AttributeValueSetContains("ak3", "a-val-key3", ["a-val3-4"].toSet(), Modifier.ANY).test(ATTRIBUTES)
        assert !new AttributeValueSetContains("ak3", "a-val-key3", ["a-val3-2", "a-val3-5"].toSet(), Modifier.ALL).test(ATTRIBUTES)
        assert !new AttributeValueSetContains("ak3", "a-val-key3", ["a-val3-1", "a-val3-2"].toSet(), Modifier.EXACTLY).test(ATTRIBUTES)
    }


    @Test
    void testLogicalPredicates() {
        assert new AndAttributeWrapper([
                new AttributeIsPresent("ak1"),
                new AttributeIsPresent("ak2"),
                new AttributeIsPresent("ak3"),
                new AttributeIsPresent("ak4")
        ]).test(ATTRIBUTES)
        assert new AndAttributeWrapper([
                new AttributeIsPresent("ak1"),
                new AttributeIsPresent("ak2"),
                new NotAttributeWrapper(new AttributeIsPresent("ak9")),
                new AttributeIsPresent("ak4")
        ]).test(ATTRIBUTES)
        assert new OrAttributeWrapper([
                new AttributeIsPresent("ak1"),
                new AttributeIsPresent("ak8")
        ]).test(ATTRIBUTES)
        assert new AndAttributeWrapper([
                new AttributeIsPresent("ak1"),
                new NotAttributeWrapper(new AttributeIsPresent("ak8"))
        ]).test(ATTRIBUTES)

        assert !new NotAttributeWrapper(new AttributeIsPresent("ak1")).test(ATTRIBUTES)
        assert !new AndAttributeWrapper([
                new AttributeIsPresent("ak1"),
                new AttributeIsPresent("ak8")
        ]).test(ATTRIBUTES)
        assert !new OrAttributeWrapper([
                new AttributeIsPresent("ak7"),
                new AttributeIsPresent("ak8")
        ]).test(ATTRIBUTES)
    }
}

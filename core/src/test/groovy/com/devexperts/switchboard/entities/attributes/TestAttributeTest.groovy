/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.devexperts.switchboard.entities.attributes

import com.devexperts.switchboard.entities.Attributes
import org.junit.Test

class TestAttributeTest {
    private static final Attributes ATTRIBUTES_A = Attributes.newBuilder()
            .putAttribute("ak1", [:])
            .putAttribute("ak2", "", "a-val2")
            .putAttribute("ak3", "a-val-key3", ["a-val3-1", "a-val3-2", "a-val3-3"])
            .putAttribute("ak4", [
                    "a-val-key4-1": ["a-val4-1-1", "a-val4-1-2", "a-val4-1-3"] as LinkedHashSet,
                    "a-val-key4-2": ["a-val4-2-1", "a-val4-2-2", "a-val4-2-3"] as LinkedHashSet])
            .putAttribute("akA", [
                    "a-val-keyA-1": ["a-valA-1-1", "a-valA-1-2"] as LinkedHashSet,
                    "a-val-keyA-2": ["a-valA-2-1", "a-valA-2-2", "a-valA-2-3"] as LinkedHashSet])
            .build()
    private static final Attributes ATTRIBUTES_B = Attributes.newBuilder()
            .putAttribute("bk1", [:])
            .putAttribute("bk2", "", "b-val2")
            .putAttribute("bk3", "b-val-key3", ["b-val3-1", "b-val3-2", "b-val3-3"])
            .putAttribute("bk4", [
                    "b-val-key4-1": ["b-val4-1-1", "b-val4-1-2", "b-val4-1-3"] as LinkedHashSet,
                    "b-val-key4-2": ["b-val4-2-1", "b-val4-2-2", "b-val4-2-3"] as LinkedHashSet])
            .putAttribute("bkB", [:])
            .build()

    @Test
    void testGetAttributes() {
        assert ATTRIBUTES_A.getAttributes() == [
                "ak1": [:],
                "ak2": ["": ["a-val2"] as LinkedHashSet],
                "ak3": ["a-val-key3": ["a-val3-1", "a-val3-2", "a-val3-3"] as LinkedHashSet],
                "ak4": [
                        "a-val-key4-1": ["a-val4-1-1", "a-val4-1-2", "a-val4-1-3"] as LinkedHashSet,
                        "a-val-key4-2": ["a-val4-2-1", "a-val4-2-2", "a-val4-2-3"] as LinkedHashSet],
                "akA": [
                        "a-val-keyA-1": ["a-valA-1-1", "a-valA-1-2"] as LinkedHashSet,
                        "a-val-keyA-2": ["a-valA-2-1", "a-valA-2-2", "a-valA-2-3"] as LinkedHashSet]
        ]
        assert ATTRIBUTES_B.getAttributes() == [
                "bk1": [:],
                "bk2": ["": ["b-val2"] as LinkedHashSet],
                "bk3": ["b-val-key3": ["b-val3-1", "b-val3-2", "b-val3-3"] as LinkedHashSet],
                "bk4": [
                        "b-val-key4-1": ["b-val4-1-1", "b-val4-1-2", "b-val4-1-3"] as LinkedHashSet,
                        "b-val-key4-2": ["b-val4-2-1", "b-val4-2-2", "b-val4-2-3"] as LinkedHashSet],
                "bkB": [:]
        ]
    }

    @Test
    void testGetAttribute() {
        assert ATTRIBUTES_A.getAttribute("ak1").isPresent()
        assert ATTRIBUTES_A.getAttribute("ak1").get() == [:]

        assert ATTRIBUTES_A.getAttribute("ak2").isPresent()
        assert ATTRIBUTES_A.getAttribute("ak2").get() == ["": ["a-val2"] as LinkedHashSet]

        assert ATTRIBUTES_A.getAttribute("ak3").isPresent()
        assert ATTRIBUTES_A.getAttribute("ak3").get() == ["a-val-key3": ["a-val3-1", "a-val3-2", "a-val3-3"] as LinkedHashSet]

        assert ATTRIBUTES_A.getAttribute("ak4").isPresent()
        assert ATTRIBUTES_A.getAttribute("ak4").get() == [
                "a-val-key4-1": ["a-val4-1-1", "a-val4-1-2", "a-val4-1-3"] as LinkedHashSet,
                "a-val-key4-2": ["a-val4-2-1", "a-val4-2-2", "a-val4-2-3"] as LinkedHashSet]

        assert !ATTRIBUTES_A.getAttribute("akX").isPresent()
    }

    @Test
    void testGetAttributeValue() {
        assert !ATTRIBUTES_A.getAttributeValue("ak1", "").isPresent()

        assert ATTRIBUTES_A.getAttributeValue("ak2", "").isPresent()
        assert ATTRIBUTES_A.getAttributeValue("ak2", "").get() == ["a-val2"] as LinkedHashSet

        assert ATTRIBUTES_A.getAttributeValue("ak3", "a-val-key3").isPresent()
        assert ATTRIBUTES_A.getAttributeValue("ak3", "a-val-key3").get() == ["a-val3-1", "a-val3-2", "a-val3-3"] as LinkedHashSet

        assert ATTRIBUTES_A.getAttributeValue("ak4", "a-val-key4-1").isPresent()
        assert ATTRIBUTES_A.getAttributeValue("ak4", "a-val-key4-1").get() == ["a-val4-1-1", "a-val4-1-2", "a-val4-1-3"] as LinkedHashSet
        assert ATTRIBUTES_A.getAttributeValue("ak4", "a-val-key4-2").isPresent()
        assert ATTRIBUTES_A.getAttributeValue("ak4", "a-val-key4-2").get() == ["a-val4-2-1", "a-val4-2-2", "a-val4-2-3"] as LinkedHashSet

        assert !ATTRIBUTES_A.getAttributeValue("", "").isPresent()
        assert !ATTRIBUTES_A.getAttributeValue("ak1", "x").isPresent()
        assert !ATTRIBUTES_A.getAttributeValue("ak2", null).isPresent()
        assert !ATTRIBUTES_A.getAttributeValue("ak2", ".*").isPresent()
        assert !ATTRIBUTES_A.getAttributeValue("ak2", "a-val-key3").isPresent()
        assert !ATTRIBUTES_A.getAttributeValue("qwerty", "a-val-key3").isPresent()
        assert !ATTRIBUTES_A.getAttributeValue("ak4", "a-val-key4-3").isPresent()
    }

    @Test
    void testGetSingleAttributeValue() {
        assert !ATTRIBUTES_A.getSingleAttributeValue("ak1", "").isPresent()

        assert ATTRIBUTES_A.getSingleAttributeValue("ak2", "").isPresent()
        assert ATTRIBUTES_A.getSingleAttributeValue("ak2", "").get() == "a-val2"


        try {
            ATTRIBUTES_A.getSingleAttributeValue("ak3", "a-val-key3").isPresent()
            assert false: "exception expected for performing getSingleAttributeValue for multivalue attribute"
        } catch (IllegalStateException ignored) {
            assert true
        }
        try {
            ATTRIBUTES_A.getSingleAttributeValue("ak3", "a-val-key3").get()
            assert false: "exception expected for performing getSingleAttributeValue for multivalue attribute"
        } catch (IllegalStateException ignored) {
            assert true
        }

        try {
            ATTRIBUTES_A.getSingleAttributeValue("ak4", "a-val-key4-1").isPresent()
            assert false: "exception expected for performing getSingleAttributeValue for multivalue attribute"
        } catch (IllegalStateException ignored) {
            assert true
        }
        try {
            ATTRIBUTES_A.getSingleAttributeValue("ak4", "a-val-key4-1").get()
            assert false: "exception expected for performing getSingleAttributeValue for multivalue attribute"
        } catch (IllegalStateException ignored) {
            assert true
        }

        try {
            ATTRIBUTES_A.getSingleAttributeValue("ak4", "a-val-key4-2").isPresent()
            assert false: "exception expected for performing getSingleAttributeValue for multivalue attribute"
        } catch (IllegalStateException ignored) {
            assert true
        }
        try {
            ATTRIBUTES_A.getSingleAttributeValue("ak4", "a-val-key4-2").get()
            assert false: "exception expected for performing getSingleAttributeValue for multivalue attribute"
        } catch (IllegalStateException ignored) {
            assert true
        }
    }

    @Test
    void testAttributeToBuilder() {
        Attributes safeCopy = ATTRIBUTES_A.toBuilder().build()
        assert safeCopy == ATTRIBUTES_A

        Attributes changedCopy = ATTRIBUTES_A.toBuilder().putAttribute("newAttr", "abc", "def") build()
        assert changedCopy.getSingleAttributeValue("newAttr", "abc").isPresent()
        assert changedCopy.getSingleAttributeValue("newAttr", "abc").get() == "def"
        assert changedCopy != ATTRIBUTES_A
        assert changedCopy != safeCopy
        assert safeCopy == ATTRIBUTES_A
        assert safeCopy.toBuilder().build() == safeCopy
    }

    @Test
    void testPutAttributes() {
        Attributes attributes1 = ATTRIBUTES_A.toBuilder()
                .putAttribute("putEmptyMapVal", [:])
                .putAttribute("putSingleKeySingleValueMapVal", ["key": ["value"] as LinkedHashSet])
                .putAttribute("putMultipleKeySingleValueMapVal", ["key1": ["value"] as LinkedHashSet, "key2": ["value"] as LinkedHashSet])
                .putAttribute("putMapVal", ["key1": ["value1", "value2"] as LinkedHashSet, "key2": ["value1", "value2"] as LinkedHashSet])

                .putAttribute("putCollection1", "key", ["value"])
                .putAttribute("putCollection2", "key", ["value1", "value2"])
                .putAttribute("putCollection3", "", ["value1", "value2"])

                .putAttribute("putSingle", "key", "value")
                .putAttribute("putSingle2", "key", "value2")

                .putAttributes(["putMap": [
                        "m-val-key4-1": ["m-val4-1-1", "m-val4-1-2", "m-val4-1-3"] as LinkedHashSet,
                        "m-val-key4-2": ["m-val4-2-1", "m-val4-2-2", "m-val4-2-3"] as LinkedHashSet]])
                .putAttributes(ATTRIBUTES_B)
                .build()
        ATTRIBUTES_A.toBuilder().build() == ATTRIBUTES_A
        Map expected1 = [
                "ak1"                            : [:],
                "ak2"                            : ["": ["a-val2"] as LinkedHashSet],
                "ak3"                            : ["a-val-key3": ["a-val3-1", "a-val3-2", "a-val3-3"] as LinkedHashSet],
                "ak4"                            : [
                        "a-val-key4-1": ["a-val4-1-1", "a-val4-1-2", "a-val4-1-3"] as LinkedHashSet,
                        "a-val-key4-2": ["a-val4-2-1", "a-val4-2-2", "a-val4-2-3"] as LinkedHashSet],
                "akA"                            : [
                        "a-val-keyA-1": ["a-valA-1-1", "a-valA-1-2"] as LinkedHashSet,
                        "a-val-keyA-2": ["a-valA-2-1", "a-valA-2-2", "a-valA-2-3"] as LinkedHashSet],

                "putEmptyMapVal"                 : [:],
                "putSingleKeySingleValueMapVal"  : ["key": ["value"] as LinkedHashSet],

                "putMultipleKeySingleValueMapVal": [
                        "key1": ["value"] as LinkedHashSet,
                        "key2": ["value"] as LinkedHashSet],

                "putMapVal"                      : [
                        "key1": ["value1", "value2"] as LinkedHashSet,
                        "key2": ["value1", "value2"] as LinkedHashSet],

                "putCollection1"                 : ["key": ["value"] as LinkedHashSet],
                "putCollection2"                 : ["key": ["value1", "value2"] as LinkedHashSet],
                "putCollection3"                 : ["": ["value1", "value2"] as LinkedHashSet],

                "putSingle"                      : ["key": ["value"] as LinkedHashSet],
                "putSingle2"                     : ["key": ["value2"] as LinkedHashSet],

                "putMap"                         : [
                        "m-val-key4-1": ["m-val4-1-1", "m-val4-1-2", "m-val4-1-3"] as LinkedHashSet,
                        "m-val-key4-2": ["m-val4-2-1", "m-val4-2-2", "m-val4-2-3"] as LinkedHashSet],

                "bk1"                            : [:],
                "bk2"                            : ["": ["b-val2"] as LinkedHashSet],
                "bk3"                            : ["b-val-key3": ["b-val3-1", "b-val3-2", "b-val3-3"] as LinkedHashSet],
                "bk4"                            : [
                        "b-val-key4-1": ["b-val4-1-1", "b-val4-1-2", "b-val4-1-3"] as LinkedHashSet,
                        "b-val-key4-2": ["b-val4-2-1", "b-val4-2-2", "b-val4-2-3"] as LinkedHashSet],
                "bkB"                            : [:],
        ]
        assert attributes1.getAttributes() == expected1

        Attributes attributes2 = attributes1.toBuilder()
                .putAttribute("ak1", ["e": ["mp", "ty"] as LinkedHashSet])
                .putAttribute("putSingleKeySingleValueMapVal", ["keyX": ["valueX"] as LinkedHashSet])
                .putAttribute("ak4", [:])
                .putAttribute("putMapVal", ["key1": ["value1x", "value2x"] as LinkedHashSet, "key2x": ["value1", "value2"] as LinkedHashSet])

                .putAttribute("putCollection1", "key", ["valueX"])

                .putAttribute("putSingle", "key1", "value1")

                .putAttributes(["putMap": [
                        "m-val-key4-1x": ["m-val4-1-1x", "m-val4-1-2x", "m-val4-1-3x"] as LinkedHashSet,
                        "m-val-key4-2x": ["m-val4-2-1x", "m-val4-2-2x", "m-val4-2-3x"] as LinkedHashSet]])
                .build()

        Map expected2 = [
                "ak1"                            : ["e": ["mp", "ty"] as LinkedHashSet],
                "ak2"                            : ["": ["a-val2"] as LinkedHashSet],
                "ak3"                            : ["a-val-key3": ["a-val3-1", "a-val3-2", "a-val3-3"] as LinkedHashSet],
                "ak4"                            : [:],
                "akA"                            : [
                        "a-val-keyA-1": ["a-valA-1-1", "a-valA-1-2"] as LinkedHashSet,
                        "a-val-keyA-2": ["a-valA-2-1", "a-valA-2-2", "a-valA-2-3"] as LinkedHashSet],

                "putEmptyMapVal"                 : [:],
                "putSingleKeySingleValueMapVal"  : ["keyX": ["valueX"] as LinkedHashSet],



                "putMultipleKeySingleValueMapVal": [
                        "key1": ["value"] as LinkedHashSet,
                        "key2": ["value"] as LinkedHashSet],

                "putMapVal"                      : [
                        "key1" : ["value1x", "value2x"] as LinkedHashSet,
                        "key2x": ["value1", "value2"] as LinkedHashSet],

                "putCollection1"                 : ["key": ["valueX"] as LinkedHashSet],
                "putCollection2"                 : ["key": ["value1", "value2"] as LinkedHashSet],
                "putCollection3"                 : ["": ["value1", "value2"] as LinkedHashSet],

                "putSingle"                      : ["key1": ["value1"] as LinkedHashSet],
                "putSingle2"                     : ["key": ["value2"] as LinkedHashSet],

                "putMap"                         : [
                        "m-val-key4-1x": ["m-val4-1-1x", "m-val4-1-2x", "m-val4-1-3x"] as LinkedHashSet,
                        "m-val-key4-2x": ["m-val4-2-1x", "m-val4-2-2x", "m-val4-2-3x"] as LinkedHashSet],

                "bk1"                            : [:],
                "bk2"                            : ["": ["b-val2"] as LinkedHashSet],
                "bk3"                            : ["b-val-key3": ["b-val3-1", "b-val3-2", "b-val3-3"] as LinkedHashSet],
                "bk4"                            : [
                        "b-val-key4-1": ["b-val4-1-1", "b-val4-1-2", "b-val4-1-3"] as LinkedHashSet,
                        "b-val-key4-2": ["b-val4-2-1", "b-val4-2-2", "b-val4-2-3"] as LinkedHashSet],
                "bkB"                            : [:],
        ]
        assert attributes2.getAttributes() == expected2
    }

    @Test
    void testMergeAttributes() {
        Attributes attributes1 = ATTRIBUTES_A.toBuilder()
                .mergeAttribute("mergeEmptyMapVal", [:])
                .mergeAttribute("mergeSingleKeySingleValueMapVal", ["key": ["value"] as LinkedHashSet])
                .mergeAttribute("mergeMultipleKeySingleValueMapVal", ["key1": ["value"] as LinkedHashSet, "key2": ["value"] as LinkedHashSet])
                .mergeAttribute("mergeMapVal", ["key1": ["value1", "value2"] as LinkedHashSet, "key2": ["value1", "value2"] as LinkedHashSet])

                .mergeAttribute("mergeCollection1", "key", ["value"])
                .mergeAttribute("mergeCollection2", "key", ["value1", "value2"])
                .mergeAttribute("mergeCollection3", "", ["value1", "value2"])

                .mergeAttribute("mergeSingle", "key", "value")
                .mergeAttribute("mergeSingle2", "key", "value2")

                .mergeAttributes(["mergeMap": [
                        "m-val-key4-1": ["m-val4-1-1", "m-val4-1-2", "m-val4-1-3"] as LinkedHashSet,
                        "m-val-key4-2": ["m-val4-2-1", "m-val4-2-2", "m-val4-2-3"] as LinkedHashSet]])
                .mergeAttributes(ATTRIBUTES_B)
                .build()

        Map expected1 = [
                "ak1"                              : [:],
                "ak2"                              : ["": ["a-val2"] as LinkedHashSet],
                "ak3"                              : ["a-val-key3": ["a-val3-1", "a-val3-2", "a-val3-3"] as LinkedHashSet],
                "ak4"                              : [
                        "a-val-key4-1": ["a-val4-1-1", "a-val4-1-2", "a-val4-1-3"] as LinkedHashSet,
                        "a-val-key4-2": ["a-val4-2-1", "a-val4-2-2", "a-val4-2-3"] as LinkedHashSet],
                "akA"                              : [
                        "a-val-keyA-1": ["a-valA-1-1", "a-valA-1-2"] as LinkedHashSet,
                        "a-val-keyA-2": ["a-valA-2-1", "a-valA-2-2", "a-valA-2-3"] as LinkedHashSet],

                "mergeEmptyMapVal"                 : [:],
                "mergeSingleKeySingleValueMapVal"  : ["key": ["value"] as LinkedHashSet],

                "mergeMultipleKeySingleValueMapVal": [
                        "key1": ["value"] as LinkedHashSet,
                        "key2": ["value"] as LinkedHashSet],

                "mergeMapVal"                      : [
                        "key1": ["value1", "value2"] as LinkedHashSet,
                        "key2": ["value1", "value2"] as LinkedHashSet],

                "mergeCollection1"                 : ["key": ["value"] as LinkedHashSet],
                "mergeCollection2"                 : ["key": ["value1", "value2"] as LinkedHashSet],
                "mergeCollection3"                 : ["": ["value1", "value2"] as LinkedHashSet],

                "mergeSingle"                      : ["key": ["value"] as LinkedHashSet],
                "mergeSingle2"                     : ["key": ["value2"] as LinkedHashSet],

                "mergeMap"                         : [
                        "m-val-key4-1": ["m-val4-1-1", "m-val4-1-2", "m-val4-1-3"] as LinkedHashSet,
                        "m-val-key4-2": ["m-val4-2-1", "m-val4-2-2", "m-val4-2-3"] as LinkedHashSet],

                "bk1"                              : [:],
                "bk2"                              : ["": ["b-val2"] as LinkedHashSet],
                "bk3"                              : ["b-val-key3": ["b-val3-1", "b-val3-2", "b-val3-3"] as LinkedHashSet],
                "bk4"                              : [
                        "b-val-key4-1": ["b-val4-1-1", "b-val4-1-2", "b-val4-1-3"] as LinkedHashSet,
                        "b-val-key4-2": ["b-val4-2-1", "b-val4-2-2", "b-val4-2-3"] as LinkedHashSet],
                "bkB"                              : [:],
        ]
        assert attributes1.getAttributes() == expected1

        Attributes attributes2 = attributes1.toBuilder()
                .mergeAttribute("ak1", ["e": ["mp", "ty"] as LinkedHashSet])
                .mergeAttribute("mergeSingleKeySingleValueMapVal", ["keyX": ["valueX"] as LinkedHashSet])
                .mergeAttribute("ak4", [:])
                .mergeAttribute("akA", "a-val-keyA-3", "a-valA-3")
                .mergeAttribute("mergeMapVal", ["key1": ["value1x", "value2x"] as LinkedHashSet, "key2x": ["value1", "value2"] as LinkedHashSet])

                .mergeAttribute("mergeCollection1", "key", ["valueX"])

                .mergeAttribute("mergeSingle", "key1", "value1")

                .mergeAttributes(["mergeMap": [
                        "m-val-key4-2" : ["m-val4-2-1x", "m-val4-2-2x", "m-val4-2-3x"] as LinkedHashSet,
                        "m-val-key4-3x": ["m-val4-3-1x", "m-val4-3-2x", "m-val4-3-3x"] as LinkedHashSet]])
                .build()

        Map expected2 = [
                "ak1"                              : ["e": ["mp", "ty"] as LinkedHashSet],
                "ak2"                              : ["": ["a-val2"] as LinkedHashSet],
                "ak3"                              : ["a-val-key3": ["a-val3-1", "a-val3-2", "a-val3-3"] as LinkedHashSet],
                "ak4"                              : [
                        "a-val-key4-1": ["a-val4-1-1", "a-val4-1-2", "a-val4-1-3"] as LinkedHashSet,
                        "a-val-key4-2": ["a-val4-2-1", "a-val4-2-2", "a-val4-2-3"] as LinkedHashSet],
                "akA"                              : [
                        "a-val-keyA-1": ["a-valA-1-1", "a-valA-1-2"] as LinkedHashSet,
                        "a-val-keyA-2": ["a-valA-2-1", "a-valA-2-2", "a-valA-2-3"] as LinkedHashSet,
                        "a-val-keyA-3": ["a-valA-3"] as LinkedHashSet],

                "mergeEmptyMapVal"                 : [:],
                "mergeSingleKeySingleValueMapVal"  : ["key": ["value"] as LinkedHashSet, "keyX": ["valueX"] as LinkedHashSet],

                "mergeMultipleKeySingleValueMapVal": [
                        "key1": ["value"] as LinkedHashSet,
                        "key2": ["value"] as LinkedHashSet],

                "mergeMapVal"                      : [
                        "key1" : ["value1", "value2", "value1x", "value2x"] as LinkedHashSet,
                        "key2" : ["value1", "value2"] as LinkedHashSet,
                        "key2x": ["value1", "value2"] as LinkedHashSet],

                "mergeCollection1"                 : ["key": ["value", "valueX"] as LinkedHashSet],
                "mergeCollection2"                 : ["key": ["value1", "value2"] as LinkedHashSet],
                "mergeCollection3"                 : ["": ["value1", "value2"] as LinkedHashSet],

                "mergeSingle"                      : [
                        "key" : ["value"] as LinkedHashSet,
                        "key1": ["value1"] as LinkedHashSet],
                "mergeSingle2"                     : ["key": ["value2"] as LinkedHashSet],

                "mergeMap"                         : [
                        "m-val-key4-1" : ["m-val4-1-1", "m-val4-1-2", "m-val4-1-3"] as LinkedHashSet,
                        "m-val-key4-2" : ["m-val4-2-1", "m-val4-2-2", "m-val4-2-3", "m-val4-2-1x", "m-val4-2-2x", "m-val4-2-3x"] as LinkedHashSet,
                        "m-val-key4-3x": ["m-val4-3-1x", "m-val4-3-2x", "m-val4-3-3x"] as LinkedHashSet],

                "bk1"                              : [:],
                "bk2"                              : ["": ["b-val2"] as LinkedHashSet],
                "bk3"                              : ["b-val-key3": ["b-val3-1", "b-val3-2", "b-val3-3"] as LinkedHashSet],
                "bk4"                              : [
                        "b-val-key4-1": ["b-val4-1-1", "b-val4-1-2", "b-val4-1-3"] as LinkedHashSet,
                        "b-val-key4-2": ["b-val4-2-1", "b-val4-2-2", "b-val4-2-3"] as LinkedHashSet],
                "bkB"                              : [:],
        ]
        assert attributes2.getAttributes().size() == expected2.size()
        for (def exp : expected2.keySet()) {
            assert attributes2.getAttributes().get(exp) == expected2.get(exp)
        }
        assert attributes2.getAttributes() == expected2
    }
}
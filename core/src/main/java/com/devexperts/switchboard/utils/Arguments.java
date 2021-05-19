/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.devexperts.switchboard.utils;

import java.util.Collection;
import java.util.Map;

/**
 * Util class providing argument check functionality
 */
public final class Arguments {
    private Arguments() {}

    /**
     * Checks that the specified object is not null
     *
     * @param t       object to check
     * @param argName name for exception
     * @param <T>     object class
     * @return the initial object if it is not not null
     * @throws IllegalArgumentException if object is null
     */
    public static <T> T checkNotNull(T t, String argName) {
        if (t == null) {
            throw new IllegalArgumentException(argName + " is null");
        }
        return t;
    }

    /**
     * Checks that the specified String is not null and nut blank
     *
     * @param s       string to check
     * @param argName name for exception
     * @return the initial string if it is not blank
     * @throws IllegalArgumentException if string is null or blank
     */
    public static String checkNotBlank(String s, String argName) {
        checkNotNull(s, argName);
        if (s.trim().isEmpty()) {
            throw new IllegalArgumentException(argName + " is blank");
        }
        return s;
    }

    /**
     * Checks that the specified Collection is not null and not empty
     *
     * @param c       collection to check
     * @param argName name for exception
     * @return the initial collection if it is not empty
     * @throws IllegalArgumentException if collection is null or empty
     */
    public static <T> Collection<T> checkNotEmpty(Collection<T> c, String argName) {
        checkNotNull(c, argName);
        if (c.isEmpty()) {
            throw new IllegalArgumentException(argName + " is empty");
        }
        return c;
    }

    /**
     * Checks that the specified Map is not null and not empty
     *
     * @param c       map to check
     * @param argName name for exception
     * @return the initial map if it is not empty
     * @throws IllegalArgumentException if collection is null or empty
     */
    public static <T, U> Map<T, U> checkNotEmpty(Map<T, U> c, String argName) {
        checkNotNull(c, argName);
        if (c.isEmpty()) {
            throw new IllegalArgumentException(argName + " is empty");
        }
        return c;
    }

}

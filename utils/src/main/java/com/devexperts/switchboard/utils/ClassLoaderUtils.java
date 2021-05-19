/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.devexperts.switchboard.utils;

import org.apache.xbean.classloader.JarFileClassLoader;

import java.net.URL;

public final class ClassLoaderUtils {
    private ClassLoaderUtils() { }

    /**
     * Provides a named, optionally child-first URLClassLoader implementation with specified parent ClassLoader
     *
     * @param name                classloader name
     * @param parent              parent classloader
     * @param inverseClassLoading if true, the created class loader logic is child-first, else conventional parent-first
     * @param urls                the URLs from which to load classes and resources
     * @return the created classloader
     */
    public static ClassLoader getClassloader(String name, ClassLoader parent, boolean inverseClassLoading, URL... urls) {
        return parent == null ?
                new JarFileClassLoader(name, urls, Thread.currentThread().getContextClassLoader(),
                        inverseClassLoading, new String[] {}, new String[] {}) :
                new JarFileClassLoader(name, urls, parent, inverseClassLoading, new String[] {}, new String[] {});
    }

    /**
     * Provides a named, optionally child-first URLClassLoader implementation with parent as currentThread ContextClassLoader
     *
     * @param name                classloader name
     * @param inverseClassLoading if true, the created class loader logic is child-first, else conventional parent-first
     * @param urls                the URLs from which to load classes and resources
     * @return the created classloader
     */
    public static ClassLoader getClassloader(String name, boolean inverseClassLoading, URL... urls) {
        return getClassloader(name, null, inverseClassLoading, urls);
    }
}

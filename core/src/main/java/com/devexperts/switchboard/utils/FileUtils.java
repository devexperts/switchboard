/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.devexperts.switchboard.utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public final class FileUtils {
    private FileUtils() {}

    /**
     * Collects recursively paths to all files within specified {@link locations} matching specified regex {@link filePattern}
     *
     * @param filePattern pattern to check collected file match. The default format is glob; regex can be used starting format value from "regex:"
     * @param locations   collection of String locations to search
     * @return a list of paths to found files matching pattern
     */
    public static List<Path> listFilePathsRecursively(String filePattern, Collection<String> locations) {
        return listMatchingFilesRecursively(filePattern, locations, t -> t);
    }

    /**
     * Collects recursively URLs to all files within specified {@link locations} matching specified regex {@link filePattern}
     *
     * @param filePattern pattern to check collected file match. The default format is glob; regex can be used starting format value from "regex:"
     * @param locations   locations collection of String locations to search
     * @return a list of URLs to found files matching pattern
     */
    public static List<URL> listFileURLsRecursively(String filePattern, Collection<String> locations) {
        return listMatchingFilesRecursively(filePattern, locations, FileUtils::toUrl);
    }

    /**
     * Transforms the specified String path to URL
     *
     * @param path to transform
     * @return URL from specified path
     */
    public static URL toUrl(String path) {
        return toUrl(Paths.get(path));
    }

    /**
     * Transforms the specified Path path to URL
     *
     * @param path to transform
     * @return URL from specified path
     */
    public static URL toUrl(Path path) {
        try {
            return path.toUri().toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException("Malformed libs path: " + path, e);
        }
    }

    private static <T> List<T> listMatchingFilesRecursively(String filePattern, Collection<String> locations, Function<Path, T> converter) {
        List<T> result = new ArrayList<>();
        String syntaxAndPattern = filePattern.startsWith("regex:") || filePattern.startsWith("glob:") ?
                filePattern : ("glob:" + filePattern);
        for (String location : locations) {
            Path path = Paths.get(location);
            PathMatcher matcher = path.getFileSystem().getPathMatcher(syntaxAndPattern);
            try (Stream<Path> walk = Files.walk(path.toAbsolutePath())) {
                walk.filter(Files::isRegularFile)
                        .filter(matcher::matches)
                        .distinct()
                        .map(converter)
                        .forEach(result::add);
            } catch (Exception e) {
                throw new RuntimeException("Failed to collect files from " + location, e);
            }
        }
        return result;
    }
}

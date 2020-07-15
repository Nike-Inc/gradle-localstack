/**
 * Copyright 2019-present, Nike, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the Apache-2.0 license found in
 * the LICENSE file in the root directory of this source tree.
 */
package com.nike.pdm.localstack.core;

/**
 * Logs messages to standard out so that they are available in the Gradle task output.
 */
public final class ConsoleLogger {

    private ConsoleLogger() {
        // Noop
    }

    /**
     * Logs the message.
     *
     * @param message message to log
     */
    public static void log(String message) {
        System.out.println(message);
    }

    /**
     * Logs the message using the supplied string replacements.
     *
     * @param message message format to log
     * @param replacements string replacements for message format
     */
    public static void log(String message, Object... replacements) {
        System.out.println(String.format(message, replacements));
    }
}

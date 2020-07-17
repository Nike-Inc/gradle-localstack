/**
 * Copyright 2020-present, Nike, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the Apache-2.0 license found in
 * the LICENSE file in the root directory of this source tree.
 */
package com.nike.pdm.localstack.core;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;

import static org.junit.Assert.*;

public class ConsoleLoggerTest {

    @Rule
    public final SystemOutRule systemOutRule = new SystemOutRule().enableLog();

    @Test
    public void shouldPrintLogMessage() {
        ConsoleLogger.log("This is a test message");
        assertEquals("This is a test message" + System.lineSeparator(), systemOutRule.getLog());
    }

    @Test
    public void shouldPrintLogMessageWithReplacements() {
        ConsoleLogger.log("This is a %s message with %s replacement", "test", 1);
        assertEquals("This is a test message with 1 replacement" + System.lineSeparator(), systemOutRule.getLog());
    }
}

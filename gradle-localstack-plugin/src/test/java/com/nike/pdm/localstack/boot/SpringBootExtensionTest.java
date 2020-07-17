/**
 * Copyright 2020-present, Nike, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the Apache-2.0 license found in
 * the LICENSE file in the root directory of this source tree.
 */
package com.nike.pdm.localstack.boot;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SpringBootExtensionTest {

    @Test
    public void shouldDefaultEnabled() {
        SpringBootExtension extension = new SpringBootExtension();
        assertTrue(extension.isEnabled());
    }

    @Test
    public void shouldDefaultDebugPort() {
        SpringBootExtension extension = new SpringBootExtension();
        assertEquals(5005, extension.getDebugPort());
    }

    @Test
    public void shouldDefaultProfiles() {
        SpringBootExtension extension = new SpringBootExtension();
        assertEquals(1, extension.getProfiles().size());
        assertTrue(extension.getProfiles().contains("local"));
    }

    @Test
    public void shouldDefaultJvmArgs() {
        SpringBootExtension extension = new SpringBootExtension();
        assertTrue(extension.getJvmArgs().isEmpty());
    }
}

/**
 * Copyright 2020-present, Nike, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the Apache-2.0 license found in
 * the LICENSE file in the root directory of this source tree.
 */
package com.nike.pdm.localstack.compose;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class LocalStackExtensionTest {

    @Test
    public void shouldNotDefaultWorkingDir() {
        LocalStackExtension extension = new LocalStackExtension();
        assertNull(extension.getWorkingDir());
    }

    @Test
    public void shouldDefaultHost() {
        LocalStackExtension extension = new LocalStackExtension();
        assertEquals("localhost", extension.getHost());
    }

    @Test
    public void shouldDefaultPort() {
        LocalStackExtension extension = new LocalStackExtension();
        assertEquals(4566, extension.getPort());
    }

    @Test
    public void shouldDefaultSigningRegion() {
        LocalStackExtension extension = new LocalStackExtension();
        assertEquals("us-east-1", extension.getSigningRegion());
    }
}

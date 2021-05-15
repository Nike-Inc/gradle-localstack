/**
 * Copyright 2020-present, Nike, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the Apache-2.0 license found in
 * the LICENSE file in the root directory of this source tree.
 */
package com.nike.pdm.localstack.core;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RequiredPluginsTest {

    @Test
    public void shouldHaveCorrectAvastDockerComposePluginName() {
        assertEquals("com.avast.gradle.docker-compose", RequiredPlugins.AVAST_DOCKER_COMPOSE_PLUGIN_ID);
    }

    @Test
    public void shouldOnlyRequireAvastDockerComposePlugin() {
        assertEquals(1, RequiredPlugins.PLUGIN_IDS.size());
        assertTrue(RequiredPlugins.PLUGIN_IDS.containsKey(RequiredPlugins.AVAST_DOCKER_COMPOSE_PLUGIN_ID));
    }
}

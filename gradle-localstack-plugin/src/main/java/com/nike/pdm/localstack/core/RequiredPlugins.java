/**
 * Copyright 2020-present, Nike, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the Apache-2.0 license found in
 * the LICENSE file in the root directory of this source tree.
 */
package com.nike.pdm.localstack.core;

import com.avast.gradle.dockercompose.DockerComposePlugin;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Utility class for getting information on plugins that are required to be applied in order to use
 * the LocalStack Gradle plugin.
 */
public class RequiredPlugins {

    /**
     * Plugin id of the Avast Docker Compose plugin.
     */
    public static final String AVAST_DOCKER_COMPOSE_PLUGIN_ID = "com.avast.gradle.docker-compose";

    /**
     * Collection of the plugin ids of all required plugin dependencies.
     */
    public static final Map<String, Class<?>> PLUGIN_IDS = Map.ofEntries(
            new AbstractMap.SimpleEntry<String, Class<?>>(AVAST_DOCKER_COMPOSE_PLUGIN_ID, DockerComposePlugin.class)
    );

    private RequiredPlugins() {
        // Noop
    }
}

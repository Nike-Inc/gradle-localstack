/**
 * Copyright 2020-present, Nike, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the Apache-2.0 license found in
 * the LICENSE file in the root directory of this source tree.
 */
package com.nike.pdm.localstack;

import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class VersionSanityCheck {
    private static final String CURRENT_LOCALSTACK_VERSION = "0.12.10";

    @Test
    public void shouldHaveCorrectLocalStackVersion() {
        InputStream composeFileStream = this.getClass().getClassLoader().getResourceAsStream("localstack-docker-compose.yml");

        Yaml yaml = new Yaml();
        Map<String, LinkedHashMap> services = yaml.load(composeFileStream);
        Map<String, String> localStackService = (Map<String, String>) services.get("services").get("localstack");
        String image = localStackService.get("image");

        assertTrue(image.endsWith(":" + CURRENT_LOCALSTACK_VERSION));
    }
}

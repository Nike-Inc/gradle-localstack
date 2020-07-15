/**
 * Copyright 2020-present, Nike, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the Apache-2.0 license found in
 * the LICENSE file in the root directory of this source tree.
 */
package com.nike.pdm.localstack.compose;

import com.nike.pdm.localstack.LocalStackPlugin;
import com.nike.pdm.localstack.core.ConsoleLogger;
import com.nike.pdm.localstack.core.LocalStackDir;
import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Paths;

/**
 * Task that initializes the project with a default LocalStack Docker Compose configuration.
 */
public class InitLocalStackTask extends DefaultTask {
    private static final String DEFAULT_LOCALSTACK_DOCKER_COMPOSE_FILE_NAME = "localstack-docker-compose.yml";

    @TaskAction
    public void run() {
        ConsoleLogger.log("Initializing LocalStack");

        final File configDest = configDestination();

        InputStream resourceStream = getClass().getClassLoader().getResourceAsStream(DEFAULT_LOCALSTACK_DOCKER_COMPOSE_FILE_NAME);
        try {
            if (configDest.exists()) {
                throw new FileExistsException(configDest);
            }

            FileUtils.copyInputStreamToFile(resourceStream, configDest);
        } catch (Exception e) {
            throw new GradleException("Unable to initialize LocalStack", e);
        }
    }

    private File configDestination() {
        LocalStackExtension ext = getProject().getExtensions().getByType(LocalStackExtension.class);
        if (ext.getWorkingDir() != null) {
            return Paths.get(ext.getWorkingDir().getAbsolutePath(), DEFAULT_LOCALSTACK_DOCKER_COMPOSE_FILE_NAME).toFile();
        } else {
            return Paths.get(getProject().getProjectDir().toString(),
                    LocalStackDir.DEFAULT_LOCALSTACK_DIR_NAME,
                    DEFAULT_LOCALSTACK_DOCKER_COMPOSE_FILE_NAME).toFile();
        }
    }

    @Internal
    @Override
    public String getGroup() {
        return LocalStackPlugin.GROUP_NAME;
    }

    @Internal
    @Override
    public String getDescription() {
        return "Initializes the project with a default LocalStack Docker Compose configuration.";
    }
}

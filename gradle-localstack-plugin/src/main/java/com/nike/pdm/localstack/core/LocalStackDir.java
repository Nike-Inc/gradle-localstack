/**
 * Copyright 2019-present, Nike, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the Apache-2.0 license found in
 * the LICENSE file in the root directory of this source tree.
 */
package com.nike.pdm.localstack.core;

import com.avast.gradle.dockercompose.ComposeExtension;
import com.nike.pdm.localstack.compose.LocalStackExtension;
import org.apache.commons.io.FileUtils;
import org.gradle.api.GradleException;
import org.gradle.api.Project;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * Utility class for locating and working with the LocalStack working directory.
 */
public class LocalStackDir {

    /**
     * Name of the Nike default localstack directory normally located at the root of a project.
     */
    public static final String DEFAULT_LOCALSTACK_DIR_NAME = "localstack";

    /**
     * Name of the sub-directory of the localstack directory where localstack data is stored by
     * the docker-compose plugin.
     */
    public static final String DATA_SUBDIRECTORY_NAME = ".localstack";

    /**
     * Gets the localstack working directory.
     *
     * @param project gradle project
     * @return the localstack working directory
     */
    public static final File getDirectory(Project project) {
        // Use the working directory of the Docker Compose Gradle Plugin
        if (project.getPluginManager().hasPlugin(RequiredPlugins.AVAST_DOCKER_COMPOSE_PLUGIN_ID)) {
            final Object extObj = project.getExtensions().findByName("dockerCompose");
            if (extObj instanceof ComposeExtension) {
                final ComposeExtension dockerCompose = (ComposeExtension) extObj;
                if (dockerCompose.getDockerComposeWorkingDirectory() != null && !dockerCompose.getDockerComposeWorkingDirectory().isEmpty()) {
                    project.getLogger().debug("Getting LocalStack directory from Docker Compose plugin: " + dockerCompose.getDockerComposeWorkingDirectory());
                    return Paths.get(dockerCompose.getDockerComposeWorkingDirectory()).toFile();
                }
            }
        }

        // Use the "localstack" folder at the project root if it exists
        File[] dirs = project.getRootDir().listFiles(pathname -> pathname.isDirectory() && pathname.getName().equals(DEFAULT_LOCALSTACK_DIR_NAME));
        if (dirs != null && dirs.length == 1) {
            project.getLogger().debug("Getting LocalStack directory from project root: " + dirs[0].toString());
            return dirs[0];
        }

        // Use the directory configured in the localstack extension
        LocalStackExtension ext = project.getExtensions().findByType(LocalStackExtension.class);
        if (ext != null) {
            if (ext.getWorkingDir() != null) {
                project.getLogger().debug("Getting LocalStack directory from extension properties: " + ext.getWorkingDir().toString());
                return ext.getWorkingDir();
            }
        }

        // This should not happen
        throw new GradleException("Cannot find localstack directory");
    }

    /**
     * Deletes the ".localstack" data sub-directory of the localstack working directory.
     *
     * @param project gradle project
     */
    public static void deleteData(Project project) {
        File localstackDir = getDirectory(project);

        File[] dirs = localstackDir.listFiles(pathname -> pathname.isDirectory() && pathname.getName().equals(DATA_SUBDIRECTORY_NAME));
        if (dirs != null && dirs.length == 1) {
            try {
                project.getLogger().info("Deleting LocalStack data sub-directory: " + dirs[0].toString());
                FileUtils.deleteDirectory(dirs[0]);
            } catch (IOException e) {
                throw new GradleException("Error deleting the LocalStack data sub-directory", e);
            }
        }
    }
}

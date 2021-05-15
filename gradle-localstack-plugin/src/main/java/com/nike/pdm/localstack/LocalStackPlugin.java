/**
 * Copyright 2020-present, Nike, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the Apache-2.0 license found in
 * the LICENSE file in the root directory of this source tree.
 */
package com.nike.pdm.localstack;

import com.nike.pdm.localstack.aws.AwsModule;
import com.nike.pdm.localstack.boot.SpringBootModule;
import com.nike.pdm.localstack.compose.LocalStackExtension;
import com.nike.pdm.localstack.compose.LocalStackModule;
import com.nike.pdm.localstack.core.RequiredPlugins;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * LocalStack Gradle Plugin
 */
public class LocalStackPlugin implements Plugin<Project> {

    /**
     * LocalStack Gradle task group name.
     */
    public static final String GROUP_NAME = "localstack";

    @Override
    public void apply(Project project) {
        applyRequiredPlugins(project);
        registerExtension(project);

        // Loading modules after evaluate because we are depending on some extension
        // properties to determine if we should register some tasks or not
        project.afterEvaluate(this::loadModules);
    }

    /**
     * Applies plugins required by this plugin with the project.
     *
     * @param project gradle project
     */
    private void applyRequiredPlugins(Project project) {
        project.getLogger().info("Loading required plugins");

        RequiredPlugins.PLUGIN_IDS.forEach((pluginId, pluginClass) -> {
            if (!project.getPluginManager().hasPlugin(pluginId)) {
                project.getPluginManager().apply(pluginClass);
            }
        });
    }

    /**
     * Registers the "localstack" extension with the project.
     *
     * @param project gradle project
     */
    private void registerExtension(Project project) {
        project.getLogger().info("Registering 'localstack' extension");

        project.getExtensions().add(LocalStackExtension.NAME, new LocalStackExtension());
    }

    /**
     * Loads the modules containing tasks and configuration for this plugin.
     *
     * @param project gradle project
     */
    private void loadModules(Project project) {
        LocalStackModule.load(project);
        AwsModule.load(project);
        SpringBootModule.load(project);
    }
}

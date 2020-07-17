/**
 * Copyright 2020-present, Nike, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the Apache-2.0 license found in
 * the LICENSE file in the root directory of this source tree.
 */
package com.nike.pdm.localstack.boot;

import com.nike.pdm.localstack.compose.LocalStackExtension;
import com.nike.pdm.localstack.compose.LocalStackModule;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.springframework.boot.gradle.tasks.bundling.BootJar;
import org.springframework.boot.gradle.tasks.run.BootRun;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Loads tasks and configuration for integrating Spring Boot with LocalStack.
 */
public class SpringBootModule {
    static final String GROUP_NAME = "Application";

    // Task Names
    public static final String BOOT_RUN_LOCAL_TASK_NAME = "bootRunLocal";
    public static final String BOOT_RUN_LOCAL_DEBUG_TASK_NAME = "bootRunLocalDebug";

    // Spring Boot
    private static final String SPRING_BOOT_PLUGIN_ID = "org.springframework.boot";
    private static final String SPRING_BOOT_PLUGIN_BOOT_RUN_TASK_NAME = "bootRun";
    private static final String SPRING_BOOT_PLUGIN_BOOT_JAR_TASK_NAME = "bootJar";
    private static final String BOOT_PROFILES_ARG_FORMAT = "-Dspring.profiles.active=%s";
    private static final String DEBUG_JVM_ARG_FORMAT = "-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=%s";

    /**
     * Loads and configures tasks for the Spring Boot plugin if it is applied to the project.
     *
     * @param project gradle project
     */
    public static void load(Project project) {
        if (isSpringBootPluginApplied(project) && isAutoConfigureEnabled(project)) {
            project.getLogger().info("Loading SpringBoot Module");

            BootRun appliedBootRun = (BootRun) project.getTasks().getByName(SPRING_BOOT_PLUGIN_BOOT_RUN_TASK_NAME);
            BootJar appliedBootJar = (BootJar) project.getTasks().getByName(SPRING_BOOT_PLUGIN_BOOT_JAR_TASK_NAME);

            configureBootRunLocal(project, appliedBootRun, appliedBootJar);
            configureBootRunLocalDebug(project, appliedBootRun, appliedBootJar);
        }
    }

    private static boolean isSpringBootPluginApplied(Project project) {
        return project.getPluginManager().hasPlugin(SPRING_BOOT_PLUGIN_ID);
    }

    private static boolean isAutoConfigureEnabled(Project project) {
        LocalStackExtension ext = project.getExtensions().findByType(LocalStackExtension.class);
        if (ext != null && ext.getSpringboot() != null) {
            return ext.getSpringboot().isEnabled();
        }

        return SpringBootExtension.DEFAULT_ENABLED;
    }

    private static void configureBootRunLocal(Project project, BootRun appliedBootRun, BootJar appliedBootJar) {
        project.getTasks().create(BOOT_RUN_LOCAL_TASK_NAME, BootRun.class, bootRun -> {
            LocalStackExtension ext = project.getExtensions().getByType(LocalStackExtension.class);

            bootRun.setGroup(GROUP_NAME);
            bootRun.setDescription("Runs the application locally and connects to mock AWS endpoints using LocalStack.");
            bootRun.getDependsOn().add(LocalStackModule.START_LOCALSTACK_TASK_NAME);
            bootRun.setMustRunAfter(Arrays.asList(LocalStackModule.START_LOCALSTACK_TASK_NAME));
            bootRun.setClasspath(javaPluginConvention(project).getSourceSets().findByName(SourceSet.MAIN_SOURCE_SET_NAME).getRuntimeClasspath());
            bootRun.conventionMapping("main", new MainClassConvention(project, appliedBootJar::getClasspath));

            List<String> jvmArgs;
            if (appliedBootRun.getJvmArgs() != null) {
                jvmArgs = new ArrayList<>(appliedBootRun.getJvmArgs());
            } else {
                jvmArgs = new ArrayList<>();
            }

            applySpringBootProfilesJvmArgument(ext, jvmArgs);

            if (ext.getSpringboot() != null) {
                jvmArgs.addAll(ext.getSpringboot().getJvmArgs());
            }

            bootRun.setJvmArgs(jvmArgs);
        });
    }

    private static void configureBootRunLocalDebug(Project project, BootRun appliedBootRun, BootJar appliedBootJar) {
        project.getTasks().create(BOOT_RUN_LOCAL_DEBUG_TASK_NAME, BootRun.class, bootRun -> {
            LocalStackExtension ext = project.getExtensions().getByType(LocalStackExtension.class);

            bootRun.setGroup(GROUP_NAME);
            bootRun.setDescription("Runs the application locally (with debugger) and connects to mock AWS endpoints using LocalStack.");
            bootRun.getDependsOn().add(LocalStackModule.START_LOCALSTACK_TASK_NAME);
            bootRun.setMustRunAfter(Arrays.asList(LocalStackModule.START_LOCALSTACK_TASK_NAME));
            bootRun.conventionMapping("main", new MainClassConvention(project, appliedBootJar::getClasspath));
            bootRun.setClasspath(javaPluginConvention(project).getSourceSets().findByName(SourceSet.MAIN_SOURCE_SET_NAME).getRuntimeClasspath());

            List<String> jvmArgs;
            if (appliedBootRun.getJvmArgs() != null) {
                jvmArgs = new ArrayList<>(appliedBootRun.getJvmArgs());
            } else {
                jvmArgs = new ArrayList<>();
            }

            applySpringBootProfilesJvmArgument(ext, jvmArgs);

            if (ext.getSpringboot() != null) {
                jvmArgs.addAll(ext.getSpringboot().getJvmArgs());
                jvmArgs.add(String.format(DEBUG_JVM_ARG_FORMAT, ext.getSpringboot().getDebugPort()));
            } else {
                jvmArgs.add(String.format(DEBUG_JVM_ARG_FORMAT, SpringBootExtension.DEFAULT_DEBUG_PORT));
            }

            bootRun.setJvmArgs(jvmArgs);
        });
    }

    private static JavaPluginConvention javaPluginConvention(Project project) {
        return project.getConvention().getPlugin(JavaPluginConvention.class);
    }

    private static void applySpringBootProfilesJvmArgument(LocalStackExtension ext, List<String> jvmArgs) {
        if (ext.getSpringboot() != null) {
            // If the profiles value is supplied and empty then we don't set the profiles jvm argument
            if (ext.getSpringboot().getProfiles() != null && !ext.getSpringboot().getProfiles().isEmpty()) {
                jvmArgs.add(String.format(BOOT_PROFILES_ARG_FORMAT, String.join(",", ext.getSpringboot().getProfiles())));
            }
        } else {
            jvmArgs.add(String.format(BOOT_PROFILES_ARG_FORMAT, SpringBootExtension.DEFAULT_PROFILES));
        }
    }
}

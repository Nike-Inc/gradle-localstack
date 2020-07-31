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
import com.nike.pdm.localstack.core.annotation.LocalStackSetupTask;
import org.gradle.api.Project;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Loads tasks and configuration for core LocalStack.
 */
public class LocalStackModule {

    // Task Names
    public static final String INIT_LOCALSTACK_TASK_NAME = "initLocalStack";
    public static final String START_LOCALSTACK_TASK_NAME = "startLocalStack";
    public static final String STOP_LOCALSTACK_TASK_NAME = "stopLocalStack";
    public static final String KILL_LOCALSTACK_TASK_NAME = "killLocalStack";
    public static final String CLEAN_LOCALSTACK_TASK_NAME = "cleanLocalStack";
    public static final String RESTART_LOCALSTACK_TASK_NAME = "restartLocalStack";

    /**
     * Loads and configures tasks in the LocalStack group.
     *
     * @param project gradle project
     */
    public static void load(Project project) {
        project.getLogger().info("Loading LocalStack Module");

        createInitLocalStackTask(project);
        createStartLocalStackTask(project);
        createStopLocalStackTask(project);
        createKillLocalStackTask(project);
        createCleanLocalStackTask(project);
        createRestartLocalStackTask(project);

        configureLocalStackSetupTasks(project);
    }

    private static void createInitLocalStackTask(Project project) {
        project.getTasks().create(INIT_LOCALSTACK_TASK_NAME, InitLocalStackTask.class);
    }

    private static void createStartLocalStackTask(Project project) {
        project.getTasks().create(START_LOCALSTACK_TASK_NAME, task -> {
            task.setGroup(LocalStackPlugin.GROUP_NAME);
            task.setDescription("Starts a new LocalStack environment.");
            task.setDependsOn(Arrays.asList("composeUp"));
            task.setMustRunAfter(Arrays.asList("composeUp"));

            task.doLast(task1 -> ConsoleLogger.log("LocalStack Started"));
        });
    }

    private static void createStopLocalStackTask(Project project) {
        project.getTasks().create(STOP_LOCALSTACK_TASK_NAME, task -> {
            task.setGroup(LocalStackPlugin.GROUP_NAME);
            task.setDescription("Stops the currently running LocalStack environment and preserves the .localstack directory contents.");
            task.setDependsOn(Arrays.asList("composeDown"));
            task.setMustRunAfter(Arrays.asList("composeDown"));

            task.doLast(task1 -> ConsoleLogger.log("LocalStack Stopped"));
        });
    }

    private static void createKillLocalStackTask(Project project) {
        project.getTasks().create(KILL_LOCALSTACK_TASK_NAME, task -> {
            task.setGroup(LocalStackPlugin.GROUP_NAME);
            task.setDescription("Kills the currently running LocalStack environment.");
            task.setDependsOn(Arrays.asList("composeDownForced", CLEAN_LOCALSTACK_TASK_NAME));
            task.setMustRunAfter(Arrays.asList("composeDownForced", CLEAN_LOCALSTACK_TASK_NAME));

            task.doLast(task1 -> ConsoleLogger.log("LocalStack Stopped"));
        });
    }

    private static void createCleanLocalStackTask(Project project) {
        project.getTasks().create(CLEAN_LOCALSTACK_TASK_NAME, task -> {
            task.setGroup(LocalStackPlugin.GROUP_NAME);
            task.setDescription("Deletes the .localstack directory.");
            task.setDependsOn(Arrays.asList("clean"));
            task.setMustRunAfter(Arrays.asList("clean"));

            task.doLast(task1 -> {
                ConsoleLogger.log("Cleaning the LocalStack data directory");
                LocalStackDir.deleteData(project);
            });
        });
    }

    private static void createRestartLocalStackTask(Project project) {
        project.getTasks().create(RESTART_LOCALSTACK_TASK_NAME, task -> {
            task.setGroup(LocalStackPlugin.GROUP_NAME);
            task.setDescription("Restarts running LocalStack environment with clean .localstack directory.");
            task.setDependsOn(Arrays.asList(KILL_LOCALSTACK_TASK_NAME, START_LOCALSTACK_TASK_NAME));

            task.doFirst(task1 -> {
                ConsoleLogger.log("Restarting LocalStack");
            });
        });
    }

    /**
     * Searches the configured tasks for tasks that are annotated with {@link LocalStackSetupTask}, when found these
     * tasks are added as a dependency of `startLocalStack` task.
     *
     * @param project gradle project
     */
    private static void configureLocalStackSetupTasks(Project project) {
        project.getLogger().info("Configuring LocalStack setup tasks:");

        final Set<String> setupTasks = new HashSet<>();
        project.getTasks().forEach(task -> {
            LocalStackSetupTask annotation = task.getClass().getAnnotation(LocalStackSetupTask.class);
            if (annotation != null) {
                project.getLogger().info("Adding '{}' to setup task dependencies", task.getName());
                setupTasks.add(task.getName());
            }
        });

        if (!setupTasks.isEmpty()) {
            project.getTasks().getByName(START_LOCALSTACK_TASK_NAME)
                    .getDependsOn().addAll(setupTasks);
        }
    }
}

/**
 * Copyright 2019-present, Nike, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the Apache-2.0 license found in
 * the LICENSE file in the root directory of this source tree.
 */
package com.nike.pdm.localstack.aws.cloudformation;

import org.gradle.api.Project;

import java.util.HashMap;
import java.util.Map;

/**
 * Loads the tasks and configuration for CloudFormation.
 */
public class CloudFormationModule {
    static final String GROUP_NAME = "LocalStack - CloudFormation";

    public static final String CREATE_CF_STACK_TASK_NAME = "createCFStack";
    public static final String DELETE_CF_STACK_TASK_NAME = "deleteCFStack";
    public static final String LIST_CF_STACKS_TASK_NAME = "listCFStacks";

    /**
     * Loads and configures all tasks in the CloudFormation group.
     *
     * @param project gradle project
     */
    public static void load(Project project) {
        final Map<String, Class> tasks = new HashMap<>();
        tasks.put(DELETE_CF_STACK_TASK_NAME, DeleteCFStackTask.class);
        tasks.put(LIST_CF_STACKS_TASK_NAME, ListCFStacksTask.class);

        tasks.forEach((name, clazz) -> {
            // Register the default tasks with the project
            project.getTasks().create(name, clazz);
        });

        project.getLogger().info("\t[AWS] CloudFormation - Loaded");
    }
}

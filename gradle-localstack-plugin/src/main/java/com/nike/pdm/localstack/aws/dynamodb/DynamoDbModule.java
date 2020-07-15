/**
 * Copyright 2020-present, Nike, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the Apache-2.0 license found in
 * the LICENSE file in the root directory of this source tree.
 */
package com.nike.pdm.localstack.aws.dynamodb;

import org.gradle.api.Project;

import java.util.HashMap;
import java.util.Map;

/**
 * Loads the tasks and configuration for DynamoDB.
 */
public class DynamoDbModule {
    static final String GROUP_NAME = "LocalStack - DynamoDB";

    public static final String CREATE_DYNAMODB_TABLE_TASK_NAME = "createDynamoDbTable";
    public static final String DELETE_DYNAMODB_TABLE_TASK_NAME = "deleteDynamoDbTable";
    public static final String LIST_DYNAMODB_TABLES_TASK_NAME = "listDynamoDbTables";

    /**
     * Loads and configures all tasks in the DynamoDB group.
     *
     * @param project gradle project
     */
    public static void load(Project project) {
        final Map<String, Class> tasks = new HashMap<>();
        tasks.put(DELETE_DYNAMODB_TABLE_TASK_NAME, DeleteDynamoDbTableTask.class);
        tasks.put(LIST_DYNAMODB_TABLES_TASK_NAME, ListDynamoDBTablesTask.class);

        tasks.forEach((name, clazz) -> {
            // Register the default tasks with the project
            project.getTasks().create(name, clazz);
        });

        project.getLogger().info("\t[AWS] DynamoDB - Loaded");
    }
}

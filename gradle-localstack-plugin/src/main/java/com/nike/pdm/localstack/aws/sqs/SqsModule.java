/**
 * Copyright 2019-present, Nike, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the Apache-2.0 license found in
 * the LICENSE file in the root directory of this source tree.
 */
package com.nike.pdm.localstack.aws.sqs;

import org.gradle.api.Project;

import java.util.HashMap;
import java.util.Map;

/**
 * Loads tasks and configuration for Sqs.
 */
public class SqsModule {
    static final String GROUP_NAME = "LocalStack - SQS";

    public static final String CREATE_SQS_QUEUES_TASK_NAME = "createSqsQueues";
    public static final String CREATE_SQS_QUEUE_WITH_DLQ_TASK_NAME = "createSqsQueueWithDlq";
    public static final String LIST_SQS_QUEUES_TASK_NAME = "listSqsQueues";
    public static final String PUBLISH_SQS_TASK_NAME = "publishSqs";
    public static final String PURGE_SQS_QUEUES_TASK_NAME = "purgeSqsQueues";

    /**
     * Loads and configures all tasks in the SQS group.
     *
     * @param project gradle project
     */
    public static void load(Project project) {
        final Map<String, Class> tasks = new HashMap<>();
        tasks.put(PURGE_SQS_QUEUES_TASK_NAME, PurgeSqsQueuesTask.class);
        tasks.put(LIST_SQS_QUEUES_TASK_NAME, ListSqsQueuesTask.class);
        tasks.put(PUBLISH_SQS_TASK_NAME, PublishSqsTask.class);

        tasks.forEach((name, clazz) -> {
            // Register default tasks with the project
            project.getTasks().create(name, clazz);
        });

        project.getLogger().info("\t[AWS] SQS - Loaded");
    }
}

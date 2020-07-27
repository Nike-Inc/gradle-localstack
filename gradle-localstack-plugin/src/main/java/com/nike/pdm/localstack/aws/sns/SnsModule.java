/**
 * Copyright 2020-present, Nike, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the Apache-2.0 license found in
 * the LICENSE file in the root directory of this source tree.
 */
package com.nike.pdm.localstack.aws.sns;

import org.gradle.api.Project;

import java.util.HashMap;
import java.util.Map;

public class SnsModule {
    static final String GROUP_NAME = "LocalStack - SNS";

    public static final String CREATE_SNS_TOPIC_WITH_QUEUE_TASK_NAME = "createSnsTopicWithSqsEndpoint";
    public static final String LIST_SNS_TOPICS_TASK_NAME = "listSnsTopics";
    public static final String LIST_SNS_TOPIC_SUBSCRIPTIONS_TASK_NAME = "listSnsTopicSubscriptions";

    /**
     * Loads and configures all tasks in the SNS group.
     *
     * @param project gradle project
     */
    public static void load(Project project) {
        final Map<String, Class> tasks = new HashMap<>();
        tasks.put(LIST_SNS_TOPICS_TASK_NAME, ListSnsTopicsTask.class);
        tasks.put(LIST_SNS_TOPIC_SUBSCRIPTIONS_TASK_NAME, ListSnsTopicSubscriptionsTask.class);

        tasks.forEach((name, clazz) -> {
            // Register default tasks with the project
            project.getTasks().create(name, clazz);
        });

        project.getLogger().info("\t[AWS] SNS - Loaded");
    }
}

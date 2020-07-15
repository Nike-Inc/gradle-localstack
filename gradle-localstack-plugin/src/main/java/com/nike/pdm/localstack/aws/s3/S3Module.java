/**
 * Copyright 2020-present, Nike, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the Apache-2.0 license found in
 * the LICENSE file in the root directory of this source tree.
 */
package com.nike.pdm.localstack.aws.s3;

import org.gradle.api.Project;

import java.util.HashMap;
import java.util.Map;

/**
 * Loads task and configuration for S3.
 */
public class S3Module {
    static final String GROUP_NAME = "LocalStack - S3";

    public static final String CREATE_S3_BUCKET_TASK_NAME = "createS3Buckets";
    public static final String DELETE_S3_BUCKET_TASK_NAME = "deleteS3Buckets";
    public static final String LIST_S3_BUCKETS_TASK_NAME = "listS3Buckets";
    public static final String PURGE_S3_BUCKETS_TASK_NAME = "purgeS3Buckets";

    /**
     * Loads and configures all tasks in the S3 group.
     *
     * @param project gradle project
     */
    public static void load(Project project) {
        final Map<String, Class> tasks = new HashMap<>();
        tasks.put(PURGE_S3_BUCKETS_TASK_NAME, PurgeS3BucketsTask.class);
        tasks.put(DELETE_S3_BUCKET_TASK_NAME, DeleteS3BucketsTask.class);
        tasks.put(LIST_S3_BUCKETS_TASK_NAME, ListS3BucketsTask.class);

        tasks.forEach((name, clazz) -> {
            // Register the default tasks with the project
            project.getTasks().create(name, clazz);
        });

        project.getLogger().info("\t[AWS] S3 - Loaded");
    }
}

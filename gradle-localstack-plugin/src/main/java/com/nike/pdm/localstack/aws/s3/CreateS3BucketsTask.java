/**
 * Copyright 2019-present, Nike, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the Apache-2.0 license found in
 * the LICENSE file in the root directory of this source tree.
 */
package com.nike.pdm.localstack.aws.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.util.StringUtils;
import com.nike.pdm.localstack.aws.AwsClientFactory;
import com.nike.pdm.localstack.core.ConsoleLogger;
import com.nike.pdm.localstack.core.Retry;
import com.nike.pdm.localstack.core.annotation.LocalStackSetupTask;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.options.Option;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Task that creates S3 buckets.
 */
@LocalStackSetupTask
public class CreateS3BucketsTask extends DefaultTask {

    @Input
    private List<String> buckets;

    @TaskAction
    public void run() {
        Retry.execute(() -> {
            final AmazonS3 amazonS3 = AwsClientFactory.getInstance().s3(getProject());

            if (buckets != null) {
                buckets.forEach(bucketName -> {
                    if (!amazonS3.doesBucketExistV2(bucketName)) {
                        ConsoleLogger.log("Creating S3 bucket: %s", bucketName);
                        amazonS3.createBucket(bucketName);
                        ConsoleLogger.log("Created S3 bucket: %s", bucketName);
                    } else {
                        ConsoleLogger.log("S3 bucket already exists: %s", bucketName);
                    }
                });
            } else {
                ConsoleLogger.log("No S3 buckets configured in the 'buckets' parameter");
            }

            return null;
        });
    }

    @Internal
    @Override
    public String getGroup() {
        return S3Module.GROUP_NAME;
    }

    @Internal
    @Override
    public String getDescription() {
        return "Creates S3 buckets.";
    }

    /**
     * Gets the names of the buckets to create.
     *
     * @return bucket names
     */
    public List<String> getBuckets() {
        return buckets;
    }

    /**
     * Sets the names of the buckets to create.
     *
     * @param buckets bucket names
     */
    public void setBuckets(List<String> buckets) {
        this.buckets = buckets;
    }

    /**
     * Sets the names of the buckets to create. This method allows the bucket names to be specified as a
     * comma-delimited string.
     *
     * @param buckets bucket names
     */
    @Option(option = "buckets", description = "Comma-delimited list of the names of the buckets to create")
    public void setBuckets(String buckets) {
        if (!StringUtils.isNullOrEmpty(buckets)) {
            setBuckets(Arrays.stream(buckets.split(","))
                    .map(String::trim)
                    .collect(Collectors.toList()));
        }
    }
}

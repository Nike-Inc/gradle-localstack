/**
 * Copyright 2020-present, Nike, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the Apache-2.0 license found in
 * the LICENSE file in the root directory of this source tree.
 */
package com.nike.pdm.localstack.aws.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.BucketVersioningConfiguration;
import com.nike.pdm.localstack.aws.AwsClientFactory;
import org.gradle.api.Project;

/**
 * Utility methods shared amongst the S3 tasks.
 */
public class S3TaskUtil {

    private final Project project;

    public S3TaskUtil(Project project) {
        this.project = project;
    }

    /**
     * Checks if the S3 bucket is versioned.
     *
     * @param bucketName s3 bucket name
     * @return <code>true</code> if the bucket is versioned; otherwise <code>false</code>
     */
    public boolean isBucketVersioned(String bucketName) {
        final AmazonS3 amazonS3 = AwsClientFactory.getInstance().s3(project);

        BucketVersioningConfiguration bucketVersioningConfiguration = amazonS3.getBucketVersioningConfiguration(bucketName);
        return bucketVersioningConfiguration.getStatus().equalsIgnoreCase(BucketVersioningConfiguration.ENABLED);
    }
}
